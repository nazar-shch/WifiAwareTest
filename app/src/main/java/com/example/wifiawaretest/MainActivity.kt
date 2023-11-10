package com.example.wifiawaretest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.*
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wifiawaretest.adapters.DeviceInfoAdapter
import com.example.wifiawaretest.data.DeviceInfo
import com.example.wifiawaretest.utils.Utils

class MainActivity : AppCompatActivity() {

    private lateinit var wifiAwareManager: WifiAwareManager
    private lateinit var wifiAwareSession: WifiAwareSession
    private lateinit var deviceListAdapter: DeviceInfoAdapter
    private val deviceList = ArrayList<DeviceInfo>()
    private var discoverySession: DiscoverySession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_WifiAwareTest)
        setContentView(R.layout.activity_main)
        initializeViews()
        checkAndRequestPermissions()
        initializeWifiAware()
    }

    private fun initializeViews() {
        val searchButton: Button = findViewById(R.id.search_button)
        val deviceListView: ListView = findViewById(R.id.device_list_view)
        val publishButton: Button = findViewById(R.id.publish_button)

        deviceListAdapter = DeviceInfoAdapter(this, deviceList)
        deviceListView.adapter = deviceListAdapter

        searchButton.setOnClickListener { startDiscovery() }
        publishButton.setOnClickListener { startPublishing() }
        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = deviceList[position]
            connectToDevice(selectedDevice)
        }
    }


    private fun connectToDevice(selectedDevice: DeviceInfo) {
        val networkSpecifier = WifiAwareNetworkSpecifier.Builder(
            selectedDevice.discoverySession,
            selectedDevice.peerHandle
        )
            .build()

        Log.w(
            "MainActivity",
            "${selectedDevice.peerHandle} ->${selectedDevice.name}-> ${selectedDevice.discoverySession}"
        )

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                showToast("Connected to ${selectedDevice.name}")
            }

            override fun onLost(network: Network) {
                showToast("Connection lost with ${selectedDevice.name}")
            }

            override fun onUnavailable() {
                showToast("Unable to connect to ${selectedDevice.name}")
            }
        }

        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }


    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }


    @SuppressLint("InlinedApi")
    private fun checkAndRequestPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.INTERNET
        )

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, 1)
        }
    }

    private fun initializeWifiAware() {
        wifiAwareManager = getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        if (wifiAwareManager.isAvailable) {
            wifiAwareManager.attach(object : AttachCallback() {
                override fun onAttached(session: WifiAwareSession) {
                    wifiAwareSession = session
                }
            }, null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startPublishing() {
        val deviceInfo = buildDeviceInfoString()
        val publishConfig = PublishConfig.Builder()
            .setServiceName("TestService")
            .setServiceSpecificInfo(deviceInfo.toByteArray(Charsets.UTF_8))
            .build()

        wifiAwareSession.publish(publishConfig, object : DiscoverySessionCallback() {
            override fun onPublishStarted(session: PublishDiscoverySession) {
                // Handle successful publish start
            }

            override fun onSessionConfigFailed() {
                super.onSessionConfigFailed()
                // Handle session configuration failure
            }
        }, null)
    }

    private fun buildDeviceInfoString(): String {
        val deviceModel = Utils.getDeviceModel()
        val osVersion = Utils.getOSVersion()
        val batteryLevel = Utils.getBatteryLevel(this)

        // Construct the device information string
        return "Model: $deviceModel, OS: $osVersion, Battery: $batteryLevel%"
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        val subscribeConfig = SubscribeConfig.Builder()
            .setServiceName("TestService")
            .build()

        wifiAwareSession.subscribe(subscribeConfig, object : DiscoverySessionCallback() {

            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                discoverySession = session
            }

            override fun onServiceDiscovered(
                peerHandle: PeerHandle,
                serviceSpecificInfo: ByteArray,
                matchFilter: MutableList<ByteArray>?
            ) {
                val infoString = String(serviceSpecificInfo, Charsets.UTF_8)
                discoverySession?.let { session ->
                    val newDeviceInfo = parseServiceInfo(infoString, peerHandle, session)
                    addDeviceInfoToList(newDeviceInfo)
                }
            }
        }, null)
    }

    private fun parseServiceInfo(
        infoString: String,
        peerHandle: PeerHandle,
        discoverySession: DiscoverySession
    ): DeviceInfo {
        val parts = infoString.split(", ").map { it.split(": ")[1] }
        val name = parts[0]
        val details = "OS: ${parts[1]}, Battery: ${parts[2]}"
        return DeviceInfo(
            name = name,
            details = details,
            peerHandle = peerHandle,
            discoverySession = discoverySession
        )
    }

    private fun addDeviceInfoToList(deviceInfo: DeviceInfo) {
        val alreadyExists =
            deviceList.any { existingDeviceInfo -> existingDeviceInfo.name == deviceInfo.name }

        if (!alreadyExists) {
            deviceList.add(deviceInfo)
            runOnUiThread { deviceListAdapter.notifyDataSetChanged() }
        }
    }

}
