package com.example.virtualcompanion

import android.bluetooth.BluetoothDevice

interface IBluetoothSDKListener {
    /**
     * from action BluetoothUtils.ACTION_DISCOVERY_STARTED
     */
    fun onDiscoveryStarted()

    /**
     * from action BluetoothUtils.ACTION_DISCOVERY_STOPPED
     */
    fun onDiscoveryStopped()

    /**
     * from action BluetoothUtils.ACTION_DEVICE_FOUND
     */
    fun onDeviceDiscovered(device: BluetoothDevice?,name:String)

    /**
     * from action BluetoothUtils.ACTION_DEVICE_CONNECTED
     */
    fun onDeviceConnected(device: BluetoothDevice?)

    /**
     * from action BluetoothUtils.ACTION_MESSAGE_RECEIVED
     */
    fun onMessageReceived(device: BluetoothDevice?, message: String?)

    /**
     * from action BluetoothUtils.ACTION_MESSAGE_SENT
     */
    fun onMessageSent(device: BluetoothDevice?)

    /**
     * from action BluetoothUtils.ACTION_CONNECTION_ERROR
     */
    fun onError(message: String?)

    /**
     * from action BluetoothDevice.PAIRING_ENDED
     */
    fun onDevicePairingEnded()

    /**
     * from action BluetoothUtils.ACTION_DEVICE_DISCONNECTED
     */
    fun onDeviceDisconnected()
}
