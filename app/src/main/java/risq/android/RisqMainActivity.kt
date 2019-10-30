package risq.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class RisqMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendIntentToService(TOR_SERVICE_ACTION_START)
    }

    private fun sendIntentToService(action: String) {

        val torService = Intent(this, TorService::class.java)
        torService.setAction(action)
        startService(torService)
    }
}
