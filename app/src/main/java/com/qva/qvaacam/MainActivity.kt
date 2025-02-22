package com.qva.qvaacam

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val webSocketManager = WebSocketManager()
    private val batteryManager = BatteryManager(this)
    private lateinit var networkDiscovery: NetworkDiscovery
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock rotation based on initial orientation
        val currentOrientation = resources.configuration.orientation
        val lockMode = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        requestedOrientation = lockMode

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupEdgeToEdge()
        setupWebView()
        setupInterfaces()
        initializeServices()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupWebView() {
        webView = WebView(this)
        setContentView(webView)
        configureWebViewSettings()
        setupWebViewClient()
        setupWebChromeClient()
        WebView.setWebContentsDebuggingEnabled(true)
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    private fun setupInterfaces() {
        networkDiscovery = NetworkDiscovery(this)
        networkDiscovery.scanIPRange(this@MainActivity, 52230) { host ->
            Log.d("NetworkScan", "Found host: $host")
        }
        webView.addJavascriptInterface(BatteryInterface(), "BatteryMonitor")
        webView.addJavascriptInterface(AndroidInterface(this, webView, networkDiscovery), "Android")
    }

    private fun initializeServices() {
        if (!checkCameraAndMicPermissions())
            requestCameraAndMicPermissions()
        batteryManager.monitorBattery { percentage, isCharging ->
            runOnUiThread {
                webView.evaluateJavascript(
                    "window.dispatchEvent(new CustomEvent('batteryUpdate', { detail: ${BatteryInterface().getBatteryStatus(percentage, isCharging)} }))",
                    null
                )
            }
        }
        webSocketManager.connect("ws://192.168.1.2:52230")
    }

    private fun configureWebViewSettings() {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
        }
    }

    private fun setupWebViewClient() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val uri = request.url
                if (uri.scheme == "file") {
                    if (uri.path?.endsWith(".js") == true) {
                        try {
                            val lastSegment = uri.lastPathSegment!!
                            val input = assets.open("www/${lastSegment}")
                            return WebResourceResponse("application/javascript", "UTF-8", input)
                        } catch (e: Exception) {
                            Log.d("WebView", "Error loading: ${uri}, ${e.message}")
                        }
                    }
                    if (uri.path?.endsWith(".png") == true) {
                        try {
                            val input = assets.open("www/${uri.path!!.removePrefix("/")}")
                            return WebResourceResponse("image/png", "UTF-8", input)
                        } catch (e: Exception) {
                            Log.d("WebView", "Error loading image: ${uri}")
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webView.evaluateJavascript("window.ready") { result ->
                    if (result == "true") {
                    }
                }
            }
        }
    }

    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.disconnect()
    }

    private fun checkCameraAndMicPermissions(): Boolean {
        return checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraAndMicPermissions() {
        requestPermissions(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ),
            123
        )
    }

}