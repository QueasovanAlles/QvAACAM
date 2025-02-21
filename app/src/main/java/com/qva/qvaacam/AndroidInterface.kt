package com.qva.qvaacam

import android.util.Log
import android.webkit.JavascriptInterface

class AndroidInterface(private val networkDiscovery: NetworkDiscovery) {
    @JavascriptInterface
    fun getMasterIP(): String {
        val status = networkDiscovery.getCurrentStatus()
        Log.d("AndroidInterface", "Returning status: $status")
        return "{\"ip\": \"${status.ip}\", \"status\": \"${status.status}\"}"
    }
}