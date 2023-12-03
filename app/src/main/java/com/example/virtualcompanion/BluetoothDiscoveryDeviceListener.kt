package com.example.virtualcompanion

import android.bluetooth.BluetoothDevice

/**
 * Callback for handling Bluetooth events.
 *
 */
interface BluetoothDiscoveryDeviceListener {
    /**
     * Called when a new device has been found.
     *
     * @param device the device found.
     */
    fun onDeviceDiscovered(device: BluetoothDevice?)

    /**
     * Called when device discovery starts.
     */
    fun onDeviceDiscoveryStarted()

    /**
     * Called on creation to inject a [BluetoothController] component to handle Bluetooth.
     *
     * @param bluetooth the controller for the Bluetooth.
     */
//    fun setBluetoothController(bluetooth: BluetoothController?)

    /**
     * Called when discovery ends.
     */
    fun onDeviceDiscoveryEnd()

    /**
     * Called when the Bluetooth status changes.
     */
    fun onBluetoothStatusChanged()

    /**
     * Called when the Bluetooth has been enabled.
     */
    fun onBluetoothTurningOn()

    /**
     * Called when a device pairing ends.
     */
    fun onDevicePairingEnded()
}