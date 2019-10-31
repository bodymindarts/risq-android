package risq.android

import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jrummyapps.android.shell.Shell
import org.torproject.android.binary.TorResourceInstaller
import java.io.*

class RisqDaemonService : Service() {
    private var fileTor: File? = null
    private var fileTorRc: File? = null
    private var torPidFile: File? = null
    private var appCacheHome: File? = null
    private var daemonStarted: Boolean = false

    override fun onCreate() {
        super.onCreate()

        appCacheHome =
            getDir(TOR_DATA_DIR, Application.MODE_PRIVATE)
        appCacheHome?.mkdirs()
        torPidFile = File(appCacheHome, TOR_PID_FILE)
        installTor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null)
            handleIntent(intent)
        else
            Log.d(LOG_TAG, "Got null onStartCommand() intent")

        return START_STICKY
    }

    fun handleIntent(intent: Intent){
        when(intent.action) {
            RISQ_SERVICE_ACTION_START -> if (!daemonStarted) {
                Thread {
                    startTor()
                    runDaemon(
                        getDir("risq", Application.MODE_PRIVATE).canonicalPath.toString(),
                        TOR_CONTROL_PORT,
                        TOR_SOCKS_PORT,
                        "BtcMainnet",
                        "INFO"
                    )
                }.start()
                daemonStarted = true
            }
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
            exec("$torCmdString --verify-config")
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
        stopTor()
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
        extraLines.append("DisableNetwork 0\n")


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

    fun readTorPidFile(): String? {
        try {
            return torPidFile?.readText()
        } catch(e: FileNotFoundException){
            return null
        }
    }

    fun stopTor() {
        val pid = readTorPidFile()
        if (pid != null) {
            try {
                exec("kill -s 9 $pid")
            } catch(e: java.lang.Exception) {}
        }
    }
    override fun onDestroy() {
        Log.i(LOG_TAG,"Killing TOR")
        stopTor()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.e(LOG_TAG, "onBind")
        return null
    }

    companion object {
        init {
            System.loadLibrary("risq_glue")
        }

        @JvmStatic
        external fun runDaemon(risqHome: String, tc_port: Int, socks_port: Int, btc_network: String, log_level: String)
    }
}
