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
import okhttp3.logging.HttpLoggingInterceptor
import risq.android.graphql.OpenOffersQuery

class OpenOffersViewModel: ViewModel() {
    private val mApolloClient = setUpApolloClient()
    private val mBuys = MutableLiveData<List<OpenOffersQuery.Buy>>()
    private val mSells = MutableLiveData<List<OpenOffersQuery.Sell>>()

    var marketPairFilter: String = "btc_eur"
        set(value) {
            field = value
            viewModelScope.launch {
                refreshOffers()
            }
        }

    val buys: LiveData<List<OpenOffer>> =
        Transformations.map(mBuys) {
                buys ->
            buys.map {
            val offerParts = it.fragments.offerParts
            OpenOffer(offerParts.id, offerParts.formattedPrice, offerParts.direction.rawValue) } }
    val sells: LiveData<List<OpenOffer>> =
        Transformations.map(mSells) {
                sells ->
            sells.map {
            val offerParts = it.fragments.offerParts
            OpenOffer(offerParts.id, offerParts.formattedPrice, offerParts.direction.rawValue) } }

    init {
        viewModelScope.launch {
            while(true){
                refreshOffers()
                delay(30000)
            }
        }
    }

    suspend private fun refreshOffers() {
        withContext(Dispatchers.IO) {
            val deferred = mApolloClient.query(OpenOffersQuery(marketPairFilter)).toDeferred()
            val response = deferred.await()
            mBuys.postValue(response.data()?.buysAndSells?.buys)
            mSells.postValue(response.data()?.buysAndSells?.sells)
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