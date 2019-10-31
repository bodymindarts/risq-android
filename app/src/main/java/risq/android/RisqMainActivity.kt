package risq.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class RisqMainActivity : AppCompatActivity() {

    val mOffersAdapter = OpenOfferAdapter()
    lateinit var mViewModel: OpenOffersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendIntentToService(RISQ_SERVICE_ACTION_START)

        recyclerViewOpenOffers.layoutManager = LinearLayoutManager(this)
        recyclerViewOpenOffers.adapter = mOffersAdapter

        mViewModel = ViewModelProviders.of(this).get(OpenOffersViewModel::class.java)
        mViewModel.sells.observe(this, Observer {
            mOffersAdapter.offers = it
        })

        mViewModel.marketPairFilter = "btc_eur"
    }

    private fun sendIntentToService(action: String) {

        val risqService = Intent(this, RisqDaemonService::class.java)
        risqService.setAction(action)
        startService(risqService)
    }
}
