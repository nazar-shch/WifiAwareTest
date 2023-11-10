package com.example.wifiawaretest.utils

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment

object Utils {

    fun getDeviceModel(): String = Build.MODEL

    fun getManufacturer(): String = Build.MANUFACTURER

    fun getOSVersion(): String = Build.VERSION.RELEASE

    fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

}
