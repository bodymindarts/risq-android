package risq.android

import android.util.Log
import androidx.lifecycle.*
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import risq.android.graphql.AllMarketsQuery
import risq.android.graphql.OpenOffersQuery
import java.lang.Exception

class OpenOffersViewModel: ViewModel() {
    private val mApolloClient = setUpApolloClient()

    private val mBuys = MutableLiveData<List<OpenOffer>>()
    val buys: LiveData<List<OpenOffer>> = mBuys

    private val mSells = MutableLiveData<List<OpenOffer>>()
    val sells: LiveData<List<OpenOffer>> = mSells

    private val mMarkets = MutableLiveData<List<String>>(ArrayList())
    val markets: LiveData<List<String>> = mMarkets

    var marketPairFilter: String = "btc_eur"
        set(value) {
            if(field != value) {
                field = value
                mBuys.value = emptyList()
                mSells.value = emptyList()
                viewModelScope.launch {
                    refreshOffers()
                }
            }
        }

    init {
        viewModelScope.launch {
            launch {
                while(mMarkets.value?.size == 0) {
                    fetchMarkets()
                    delay(1000)
                }
            }
            while(true){
                delay(20000)
                refreshOffers()
            }
        }
    }

    private suspend fun fetchMarkets() {
        withContext(Dispatchers.IO) {
            try {
                val deferred = mApolloClient.query(AllMarketsQuery()).toDeferred()
                val response = deferred.await()
                mMarkets.postValue(response.data()?.markets?.map { it.pair })
            } catch(e: Exception) {
                Log.e(LOG_TAG,"Couldn't fetch markets", e)
            }
        }

    }
    private suspend fun refreshOffers() {

        withContext(Dispatchers.IO) {
            try {
                val deferred = mApolloClient.query(OpenOffersQuery(marketPairFilter)).toDeferred()
                val response = deferred.await()
                mSells.postValue(response.data()?.buysAndSells?.buys?.map {
                    val offerParts = it.fragments.offerParts
                    OpenOffer(
                        offerParts.id,
                        offerParts.formattedPrice,
                        "SELL",
                         offerParts.formattedAmount
                    )
                }
                    ?: emptyList()

                )
                mBuys.postValue(response.data()?.buysAndSells?.sells?.map {
                    val offerParts = it.fragments.offerParts
                    OpenOffer(
                        offerParts.id,
                        offerParts.formattedPrice,
                        "BUY",
                         offerParts.formattedAmount
                    )
                }
                    ?: emptyList()
                )
            } catch(e: Exception) {
                Log.e(LOG_TAG,"Couldn't fetch open offers", e)
            }
        }
    }

    private fun setUpApolloClient(): ApolloClient {
        val okHttp = OkHttpClient
            .Builder()
        return ApolloClient.builder()
            .serverUrl(RISQ_GQL_ENDPOINT)
            .okHttpClient(okHttp.build())
            .build()
    }
}