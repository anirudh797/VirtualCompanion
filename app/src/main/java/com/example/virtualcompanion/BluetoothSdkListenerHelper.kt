package com.example.virtualcompanion

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class BluetoothSDKListenerHelper {
    companion object {

        private var mBluetoothSDKBroadcastReceiver: BluetoothSDKBroadcastReceiver? = null

        class BluetoothSDKBroadcastReceiver : BroadcastReceiver() {
            private var mGlobalListener: IBluetoothSDKListener? = null

            public fun setBluetoothSDKListener(listener: IBluetoothSDKListener) {
                Log.d(BluetoothUtils.TAG,"listener registered")
                mGlobalListener = listener
            }

            public fun removeBluetoothSDKListener(listener: IBluetoothSDKListener): Boolean {
                if (mGlobalListener == listener) {
                    Log.d(BluetoothUtils.TAG,"removed SdkListener")
                    mGlobalListener = null
                }

                return mGlobalListener == null
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                val device =
                    intent!!.getParcelableExtra<BluetoothDevice>(BluetoothUtils.EXTRA_DEVICE)
                val message = intent.getStringExtra(BluetoothUtils.EXTRA_MESSAGE)

                when (intent.action) {
                    BluetoothUtils.ACTION_DEVICE_FOUND , BluetoothDevice.ACTION_FOUND -> {
                        Log.d(BluetoothUtils.TAG,"device ${device.toString()} name : ${message}")
                        mGlobalListener!!.onDeviceDiscovered(device,message?:"")
                    }

                    BluetoothUtils.ACTION_BLUETOOTH_STATE_ON ->{
                        Log.d(BluetoothUtils.TAG,"Bluetooth On")
                    }

                    BluetoothUtils.ACTION_BLUETOOTH_STATE_OFF ->{
                        Log.d(BluetoothUtils.TAG,"Bluetooth off")
                    }

                    BluetoothUtils.ACTION_DISCOVERY_STARTED -> {
                        mGlobalListener!!.onDiscoveryStarted()
                    }

                    BluetoothUtils.ACTION_DISCOVERY_STOPPED -> {
                        mGlobalListener!!.onDiscoveryStopped()
                    }

                    BluetoothUtils.ACTION_DEVICE_CONNECTED -> {
                        Log.d(BluetoothUtils.TAG,"on Device Connected")
                        mGlobalListener!!.onDeviceConnected(device)
                    }

                    BluetoothUtils.ACTION_MESSAGE_RECEIVED -> {
                        mGlobalListener!!.onMessageReceived(device, message)
                    }

                    BluetoothUtils.ACTION_MESSAGE_SENT -> {
                        mGlobalListener!!.onMessageSent(device)
                    }

                    BluetoothUtils.ACTION_CONNECTION_ERROR -> {
                        mGlobalListener!!.onError(message)
                    }

                    BluetoothUtils.ACTION_DEVICE_DISCONNECTED -> {
                        Log.d(BluetoothUtils.TAG,"on Device Disconnected")
                        mGlobalListener!!.onDeviceDisconnected()
                    }

                    BluetoothDevice.ACTION_BOND_STATE_CHANGED ->{
                        mGlobalListener!!.onDevicePairingEnded()
                    }
                }
            }
        }

        public fun registerBluetoothSDKListener(
            context: Context?,
            listener: IBluetoothSDKListener
        ) {
            if (mBluetoothSDKBroadcastReceiver == null) {
                mBluetoothSDKBroadcastReceiver = BluetoothSDKBroadcastReceiver()

                val intentFilter = IntentFilter().also {
                    it.addAction(BluetoothUtils.ACTION_DEVICE_FOUND)
                    it.addAction(BluetoothUtils.ACTION_BLUETOOTH_STATE_ON)
                    it.addAction(BluetoothUtils.ACTION_BLUETOOTH_STATE_OFF)
                    it.addAction(BluetoothDevice.ACTION_FOUND)
                    it.addAction(BluetoothUtils.ACTION_DISCOVERY_STARTED)
                    it.addAction(BluetoothUtils.ACTION_DISCOVERY_STOPPED)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_CONNECTED)
                    it.addAction(BluetoothUtils.ACTION_MESSAGE_RECEIVED)
                    it.addAction(BluetoothUtils.ACTION_MESSAGE_SENT)
                    it.addAction(BluetoothUtils.ACTION_CONNECTION_ERROR)
                    it.addAction(BluetoothUtils.ACTION_DEVICE_DISCONNECTED)
                    it.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                }


                LocalBroadcastManager.getInstance(context!!).registerReceiver(
                    mBluetoothSDKBroadcastReceiver!!, intentFilter
                )
            }

            mBluetoothSDKBroadcastReceiver!!.setBluetoothSDKListener(listener)
        }

        public fun unregisterBluetoothSDKListener(
            context: Context?,
            listener: IBluetoothSDKListener
        ) {

            if (mBluetoothSDKBroadcastReceiver != null) {
                val empty = mBluetoothSDKBroadcastReceiver!!.removeBluetoothSDKListener(listener)


                if (empty) {
                    LocalBroadcastManager.getInstance(context!!)
                        .unregisterReceiver(mBluetoothSDKBroadcastReceiver!!)
                    mBluetoothSDKBroadcastReceiver = null
                }
            }
        }
    }
}

