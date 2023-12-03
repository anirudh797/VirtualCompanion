package com.example.virtualcompanion

import android.bluetooth.BluetoothDevice

class BluetoothUtils {
    companion object {
        val ACTION_DISCOVERY_STARTED = "ACTION_DISCOVERY_STARTED"
        val ACTION_DISCOVERY_STOPPED = "ACTION_DISCOVERY_STOPPED"
        val ACTION_DEVICE_FOUND = "ACTION_DEVICE_FOUND"
        val ACTION_DEVICE_CONNECTED = "ACTION_DEVICE_CONNECTED"
        val ACTION_DEVICE_DISCONNECTED = "ACTION_DEVICE_DISCONNECTED"
        val ACTION_MESSAGE_RECEIVED = "ACTION_MESSAGE_RECEIVED"
        val ACTION_BLUETOOTH_STATE_ON = "ACTION_BLUETOOTH_ON"
        val ACTION_BLUETOOTH_STATE_OFF = "ACTION_BLUETOOTH_OFF"
        val ACTION_MESSAGE_SENT = "ACTION_MESSAGE_SENT"
        val ACTION_CONNECTION_ERROR = "ACTION_CONNECTION_ERROR"
        val EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE"
        val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        val EXTRA_NAME = "android.bluetooth.device.extra.NAME"
        val TAG ="BluetoothUtil"
        val BLUETOOTH_SCAN_REQUEST_CODE = 123

        /**
         * Converts a BluetoothDevice to its String representation.
         *
         * @param device the device to convert to String.
         * @return a String representation of the device.
         */
        fun deviceToString(device: BluetoothDevice): String {
            return "[Address: " + device.address + ", Name: " + device.name + "]"
        }

    }


}