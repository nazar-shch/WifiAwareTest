package com.example.wifiawaretest.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.wifiawaretest.R
import com.example.wifiawaretest.data.DeviceInfo

class DeviceInfoAdapter(context: Context, devices: List<DeviceInfo>) :
    ArrayAdapter<DeviceInfo>(context, 0, devices) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val deviceInfo = getItem(position)!!

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.device_info_item, parent, false)

        val tvName = view.findViewById<TextView>(R.id.deviceName)
        val tvDetails = view.findViewById<TextView>(R.id.deviceDetails)

        tvName.text = deviceInfo.name
        tvDetails.text = deviceInfo.details

        return view
    }
}
