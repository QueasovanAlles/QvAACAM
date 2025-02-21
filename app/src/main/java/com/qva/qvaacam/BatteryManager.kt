package com.qva.qvaacam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager as AndroidBatteryManager

import android.webkit.JavascriptInterface

class BatteryInterface {
    @JavascriptInterface
    fun getBatteryStatus(percentage: Int, isCharging: Boolean): String {
        return "{\"percentage\": $percentage, \"isCharging\": $isCharging}"
    }
}

class BatteryManager(private val context: Context) {
    fun monitorBattery(onBatteryUpdate: (Int, Boolean) -> Unit) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }

        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(AndroidBatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(AndroidBatteryManager.EXTRA_SCALE, -1)
                    val percentage = level * 100 / scale

                    val status = it.getIntExtra(AndroidBatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == AndroidBatteryManager.BATTERY_STATUS_CHARGING ||
                            status == AndroidBatteryManager.BATTERY_STATUS_FULL

                    onBatteryUpdate(percentage, isCharging)
                }
            }
        }, filter)
    }
}