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
import risq.android.graphql.OpenOffersQuery
import java.lang.Exception

class OpenOffersViewModel: ViewModel() {
    private val mApolloClient = setUpApolloClient()

    private val mBuys = MutableLiveData<List<OpenOffer>>()
    val buys: LiveData<List<OpenOffer>> = mBuys

    private val mSells = MutableLiveData<List<OpenOffer>>()
    val sells: LiveData<List<OpenOffer>> = mSells

    var marketPairFilter: String = "btc_eur"
        set(value) {
            field = value
            viewModelScope.launch {
                refreshOffers()
            }
        }

    init {
        viewModelScope.launch {
            while(true){
                delay(20000)
                refreshOffers()
            }
        }
    }

    private suspend fun refreshOffers() {

        withContext(Dispatchers.IO) {
            try {
                val deferred = mApolloClient.query(OpenOffersQuery(marketPairFilter)).toDeferred()
                val response = deferred.await()
                mBuys.postValue(response.data()?.buysAndSells?.buys?.map {
                    val offerParts = it.fragments.offerParts
                    OpenOffer(
                        offerParts.id,
                        offerParts.formattedPrice,
                        offerParts.direction.rawValue
                    )
                }
                    ?: emptyList()

                )
                mSells.postValue(response.data()?.buysAndSells?.sells?.map {
                    val offerParts = it.fragments.offerParts
                    OpenOffer(
                        offerParts.id,
                        offerParts.formattedPrice,
                        offerParts.direction.rawValue
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