package risq.android

import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jrummyapps.android.shell.Shell
import org.torproject.android.binary.TorResourceInstaller
import java.io.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

const val TOR_SERVICE_ACTION_START = "risq.android.intent.action.START_TOR"

class TorService : Service() {
    private val threads = Executors.newFixedThreadPool(3)

    private var fileTor: File? = null
    private var fileTorRc: File? = null
    private var torPidFile: File? = null
    private var appCacheHome: File? = null

    override fun onCreate() {
        super.onCreate()

        appCacheHome =
            getDir(TOR_DATA_DIR, Application.MODE_PRIVATE)
        torPidFile = File(appCacheHome, TOR_PID_FILE)
        installTor()
    }

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

        if (runTorShellCmd()) {
            Log.i(LOG_TAG, "Tor started with pid: ${readTorPidFile()}")
        } else {
            Log.e(LOG_TAG, "Couldn't start tor!")
        }
    }

    @Throws(Exception::class)
    private fun runTorShellCmd(): Boolean {
        var result = true

        //make sure Tor exists and we can execute it
        fileTor?.let { if (!it.exists() || !it.canExecute()) return false } ?: return false
        fileTorRc?.let { if (!it.exists() || !it.canRead()) return false }  ?: return false

        val fileTorrcCustom = updateTorrcCustomFile()
        fileTorrcCustom?.let { if (!it.exists() || ! it.canRead()) return false } ?: return false


        val torCmdString = (fileTor!!.canonicalPath
                + " --DataDirectory " + appCacheHome!!.canonicalPath
                + " --defaults-torrc " + fileTorRc
                + " -f " + fileTorrcCustom.getCanonicalPath())

        var exitCode = -1

        try {
            exitCode = exec("$torCmdString --verify-config")
        } catch (e: Exception) {
            Log.e(LOG_TAG,"Tor configuration did not verify: " + e.message, e)
            return false
        }

        try {
            exitCode = exec(torCmdString)
        } catch (e: Exception) {
            Log.e(LOG_TAG,"Tor was unable to start: " + e.message, e)
            return false
        }

        if (exitCode != 0) {
            Log.e(LOG_TAG,"Tor did not start. Exit:$exitCode")
            return false
        }


        return result
    }


    @Throws(Exception::class)
    private fun exec(cmd: String): Int {
        val shellResult = Shell.run(cmd)


        Log.i(LOG_TAG,"CMD: " + cmd + "; SUCCESS=" + shellResult.isSuccessful);

        if (!shellResult.isSuccessful) {
            throw Exception("Error: " + shellResult.exitCode + " ERR=" + shellResult.getStderr() + " OUT=" + shellResult.getStdout())
        }

        return shellResult.exitCode
    }

    fun installTor() {
        val torResourceInstaller = TorResourceInstaller(this, filesDir)

        fileTor = torResourceInstaller.installResources()
        fileTorRc = torResourceInstaller.getTorrcFile()
    }

    fun updateTorrcCustomFile(): File? {
        val extraLines = StringBuffer()

        extraLines.append("\n")
        extraLines.append("ControlPort $TOR_CONTROL_PORT\n")
        extraLines.append("SOCKSPort $TOR_SOCKS_PORT\n")
        extraLines.append("PidFile  ${torPidFile!!.canonicalPath}\n")

        val fileTorRcCustom = File(fileTorRc!!.absolutePath + ".custom")
        val success = updateTorConfigCustom(fileTorRcCustom, extraLines.toString())

        if (success && fileTorRcCustom.exists()) {
            return fileTorRcCustom
        } else
            return null
    }

    @Throws(IOException::class)
    fun updateTorConfigCustom(fileTorRcCustom: File, extraLines: String): Boolean {
        val fos = FileWriter(fileTorRcCustom, false)
        val ps = PrintWriter(fos)
        ps.print(extraLines)
        ps.flush()
        ps.close()
        return true
    }

    fun readTorPidFile(): String {
        return torPidFile!!.readText()
    }

    override fun onDestroy() {
        Log.i(LOG_TAG,"Killing TOR")
        exec("kill -s 9 ${readTorPidFile()}")
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.e(LOG_TAG, "onBind")
        return null
    }
}
