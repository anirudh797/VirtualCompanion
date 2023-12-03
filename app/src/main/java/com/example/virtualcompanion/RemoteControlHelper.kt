package com.example.virtualcompanion

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.virtualcompanion.BluetoothUtils.Companion.TAG

object RemoteControlHelper {

    annotation class Key {

        companion object {
            var POWER = byteArrayOf(0x00.toByte(), 0x30.toByte())
            var SLEEP = byteArrayOf(0x00.toByte(), 0x34.toByte())
            var MENU = byteArrayOf(0x00.toByte(), 0x45.toByte())
            var MENU_PICK = byteArrayOf(0x00.toByte(), 0x41.toByte())
            var MENU_UP = byteArrayOf(0x00.toByte(), 0x42.toByte())
            var MENU_DOWN = byteArrayOf(0x00.toByte(), 0x43.toByte())
            var MENU_LEFT = byteArrayOf(0x00.toByte(), 0x44.toByte())
            var MENU_RIGHT = byteArrayOf(0x00.toByte(), 0x45.toByte())
            var CAPTIONS = byteArrayOf(0x00.toByte(), 0x61.toByte())
            var VCR_TV = byteArrayOf(0x00.toByte(), 0x63.toByte())
            var RED = byteArrayOf(0x00.toByte(), 0x69.toByte())
            var GREEN = byteArrayOf(0x00.toByte(), 0x6a.toByte())
            var BLUE = byteArrayOf(0x00.toByte(), 0x6b.toByte())
            var YELLOW = byteArrayOf(0x00.toByte(), 0x6c.toByte())
            var ASSIGN_SELECTION = byteArrayOf(0x00.toByte(), 0x81.toByte())
            var MEDIA_SELECT_CD = byteArrayOf(
                0x00.toByte(),
                0x91.toByte()
            )
            var RECORD = byteArrayOf(0x00.toByte(), 0xb2.toByte())
            var MEDIA_SELECT_HOME = byteArrayOf(0x00.toByte(), 0x9a.toByte())
            var MEDIA_SELECT_SATELLITE = byteArrayOf(0x00.toByte(), 0x98.toByte())
            var MEDIA_SELECT_TV = byteArrayOf(0x00.toByte(), 0x89.toByte())
            var MEDIA_SELECT_SAP = byteArrayOf(0x00.toByte(), 0x9e.toByte())
            var MEDIA_FAST_FORWARD = byteArrayOf(0x00.toByte(), 0xb3.toByte())
            var MEDIA_REWIND = byteArrayOf(0x00.toByte(), 0xb4.toByte())
            var CHANNEL_UP = byteArrayOf(0x00.toByte(), 0x9c.toByte())
            var CHANNEL_DOWN = byteArrayOf(0x00.toByte(), 0x9d.toByte())
            var PLAY_PAUSE = byteArrayOf(0x00.toByte(), 0xcd.toByte())
            var VOLUME_INC = byteArrayOf(0x00.toByte(), 0xe9.toByte())

            var VOLUME_DEC = byteArrayOf(0x00.toByte(), 0xea.toByte())
            var MUTE = byteArrayOf(0x00.toByte(), 0xe2.toByte())
            var HOME = byteArrayOf(0x02.toByte(), 0x23.toByte())
            var BACK = byteArrayOf(0x02.toByte(), 0x24.toByte())
            var QUIT = byteArrayOf(0x00.toByte(), 0x94.toByte())

            var EPG = byteArrayOf(0x00.toByte(), 0x8d.toByte())
            var INFO = byteArrayOf(0xbd.toByte(), 0x01.toByte())
            var MAGENTA = byteArrayOf(0x00.toByte(), 0x8d.toByte())
//                var NUM1 = byteArrayOf(0x00.toByte(),0x1e.toByte())
//                var NUM2 = byteArrayOf(0x00.toByte(),0x1f.toByte())

        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    fun sendKeyDown0(byte1: Int, byte2: Int, bluetoothController: BluetoothSDKService): Boolean {
        return if (bluetoothController.bluetoothHidDevice != null && bluetoothController.isHidDeviceConnected) {
            bluetoothController.bluetoothHidDevice!!.sendReport(
                bluetoothController.mmDevice,
                0, RemoteControlReport.getReport(byte1, byte2)
            )
        } else false
    }


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    fun sendKeyDown(byte1: Int, byte2: Int, bluetoothController: BluetoothSDKService): Boolean {
        return if (bluetoothController.bluetoothHidDevice != null && bluetoothController.isHidDeviceConnected) {
            val flag  = bluetoothController.bluetoothHidDevice!!.sendReport(
                bluetoothController.mmDevice,
                Constants.ID_REMOTE_CONTROL.toInt(), RemoteControlReport.getReport(byte1, byte2)
            )
            Log.d(TAG,"sendKeyDown reportSent $flag")

//            RemoteControlReport.getReport(byte1,byte2)?.let { bluetoothController.sendMessage(it) }
            true

        } else {
            Log.d(TAG,"sendKeyDown fail")
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    fun sendKeyUp(bluetoothController: BluetoothSDKService): Boolean {
        return if (bluetoothController.bluetoothHidDevice != null && bluetoothController.isHidDeviceConnected) {
            val flag  = bluetoothController.bluetoothHidDevice!!.sendReport(
                bluetoothController.mmDevice,
                Constants.ID_REMOTE_CONTROL.toInt(), RemoteControlReport.getReport(0, 0)
            )
//            RemoteControlReport.getReport(0,0)?.let { bluetoothController.sendMessage(it) }
            Log.d(TAG,"sendKeyUp reportSent $flag")
            true
        } else {
            Log.d(TAG,"sendKeyUp fail")
            false
        }
    }

}