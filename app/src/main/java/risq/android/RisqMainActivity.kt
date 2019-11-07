package risq.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*



class RisqMainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent?.getItemAtPosition(position).toString()
        Log.i(LOG_TAG,"selected: $item")
        mViewModel.marketPairFilter = item
    }

    val mMarkets = mutableListOf("btc_eur")
    var mMarketsAdapter: ArrayAdapter<String>? = null
    val mOffersAdapter = OpenOfferAdapter()
    lateinit var mViewModel: OpenOffersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sendIntentToService(RISQ_SERVICE_ACTION_START)
        setContentView(R.layout.activity_main)

        mMarketsAdapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mMarkets)
        mMarketsAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        marketSelection.onItemSelectedListener = this;
        marketSelection.adapter = mMarketsAdapter

        recyclerViewOpenOffers.layoutManager = LinearLayoutManager(this)
        recyclerViewOpenOffers.adapter = mOffersAdapter

        mViewModel = ViewModelProviders.of(this).get(OpenOffersViewModel::class.java)
        mViewModel.sells.observe(this, Observer {
            mOffersAdapter.sells = it
        })
        mViewModel.buys.observe(this, Observer {
            mOffersAdapter.buys = it
        })
        mViewModel.markets.observe(this, Observer {
            Log.i(LOG_TAG,"UPDATING MARKETS")
            mMarkets.clear()
            mMarkets.addAll(it)
            mMarketsAdapter?.notifyDataSetChanged()
        })
    }

    private fun sendIntentToService(action: String) {

        val risqService = Intent(this, RisqDaemonService::class.java)
        risqService.setAction(action)
        startService(risqService)
    }
}
