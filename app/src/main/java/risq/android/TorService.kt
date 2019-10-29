package risq.android

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.torproject.android.binary.TorResourceInstaller
import java.util.concurrent.Executors

const val TOR_SERVICE_ACTION_START = "risq.android.intent.action.START_TOR"

class TorService : Service() {
    private val threads = Executors.newFixedThreadPool(3)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null)
            threads.execute(runnableForIntent(intent))
        else
            Log.d(LOG_TAG, "Got null onStartCommand() intent")

        return START_STICKY
    }

    fun runnableForIntent(intent: Intent): () -> Unit {
        return when(intent.action) {
            TOR_SERVICE_ACTION_START -> ::startTor
            else -> { -> Log.e(LOG_TAG,"Unknown Action") }
        }
    }

    fun startTor() {
        Log.i(LOG_TAG, "Starting Tor process")

        val torResourceInstaller = TorResourceInstaller(this, filesDir)

        val fileTorBin = torResourceInstaller.installResources()
        val fileTorRc = torResourceInstaller.getTorrcFile()
//
//        System.loadLibrary("risq_glue")
//        Log.i("rust", hello("qdrwb"))
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.e(LOG_TAG, "onBind")
        handleIntent(intent)
        return null
    }

    private fun handleIntent(intent: Intent) {
        Log.e(LOG_TAG,intent.action ?: "NULL intent")
    }
}
