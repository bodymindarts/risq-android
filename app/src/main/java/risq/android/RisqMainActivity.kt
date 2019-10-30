package risq.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RisqMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendIntentToService(RISQ_SERVICE_ACTION_START)
    }

    private fun sendIntentToService(action: String) {

        val torService = Intent(this, RisqDaemonService::class.java)
        torService.setAction(action)
        startService(torService)
    }
}
