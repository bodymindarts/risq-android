package risq.android

class RisqWrapper {
    companion object {
        init {
            System.loadLibrary("risq_glue")
        }

        @JvmStatic
        external fun runDaemon(risqHome: String, tc_port: Int, socks_port: Int, btc_network: String, log_level: String)
    }
}