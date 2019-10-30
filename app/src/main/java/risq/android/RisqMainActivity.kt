package risq.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class RisqMainActivity : AppCompatActivity() {

    val openOffers = ArrayList<OpenOffer>()
    val offersAdapter = OpenOfferAdapter( openOffers)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendIntentToService(RISQ_SERVICE_ACTION_START)

        openOffers.add(OpenOffer("ashtoen","0.123","BUY"))
        recyclerViewOpenOffers.layoutManager = LinearLayoutManager(this)
        recyclerViewOpenOffers.adapter = offersAdapter
    }

    private fun sendIntentToService(action: String) {

        val risqService = Intent(this, RisqDaemonService::class.java)
        risqService.setAction(action)
        startService(risqService)
    }
}
