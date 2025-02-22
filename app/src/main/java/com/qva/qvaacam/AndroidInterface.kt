package com.qva.qvaacam

import android.content.pm.ActivityInfo
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView

class AndroidInterface(private val activity: MainActivity,
                       private val webView: WebView,
                       private val networkDiscovery: NetworkDiscovery) {
    @JavascriptInterface
    fun getMasterIP(): String {
        val status = networkDiscovery.getCurrentStatus()
        Log.d("AndroidInterface", "Returning status: $status")
        return "{\"ip\": \"${status.ip}\", \"status\": \"${status.status}\"}"
    }
    @JavascriptInterface
    fun flipOrientation() {
        activity.runOnUiThread {
            val newOrientation = if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            activity.requestedOrientation = newOrientation
            webView.loadUrl(webView.url ?: "")
        }
    }
}