package com.qva.qvaacam

import android.content.Context
import android.net.ConnectivityManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import android.webkit.JavascriptInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
data class MasterStatus(val ip: String, val status: String)

class NetworkDiscovery(private val context: Context) {
    @Volatile private var foundHost: String = ""
    @Volatile private var currentStatus = "not_ready"
    @Volatile private var isScanning = false

    fun detectLANRange(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val linkProperties = connectivityManager.getLinkProperties(network)

        val gateway = linkProperties?.dhcpServerAddress?.hostAddress
        return gateway?.substringBeforeLast(".") ?: "192.168.1"
    }

    fun getCurrentStatus(): MasterStatus {
        return when {
            isScanning -> MasterStatus("", "not_ready")
            foundHost.isNotEmpty() -> MasterStatus(foundHost, "ready")
            else -> MasterStatus("", "not_found")
        }
    }

    fun scanIPRange(context: Context, port: Int, onHostFound: (String) -> Unit) {
        if (isScanning) return
        isScanning = true
        foundHost = ""
        currentStatus = "not_ready"

        CoroutineScope(Dispatchers.IO).launch {
            val lanPrefix = detectLANRange(context)
            val jobs = (2..254).map { i ->
                async {
                    val host = "$lanPrefix.$i"
                    if (isPortOpen(host, port, 100)) {
                        withContext(Dispatchers.Main) {
                            currentStatus = "ready"
                            foundHost = host
                            onHostFound(host)
                        }
                    }
                }
            }
            jobs.awaitAll()
            if (foundHost.isEmpty()) {
                currentStatus = "not_found"
            }
            isScanning = false
        }
    }

    private fun isPortOpen(host: String, port: Int, timeout: Int): Boolean {
        return try {
            Socket().apply {
                connect(InetSocketAddress(host, port), timeout)
                close()
            }
            Log.d("CONSOLE", "isPortOpen : foundHost $host")
            foundHost = host
            true
        } catch (e: Exception) {
            false
        }
    }
}

