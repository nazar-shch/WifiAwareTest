package com.example.wifiawaretest.data

import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.PeerHandle

data class DeviceInfo(
    val name: String,
    val details: String,
    val peerHandle: PeerHandle,
    val discoverySession: DiscoverySession
)