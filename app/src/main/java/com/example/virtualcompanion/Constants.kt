package com.example.virtualcompanion

import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi

object MessageConstants {
    const val CONNECTION_SUCCESSFULL = 3
    const val MESSAGE_READ = 0
    const val CONNECTION_FAILED = 4
    const val MESSAGE_WRITE = 1
    const val MESSAGE_TOAST = 2 // ... (Add other message types here as needed.)



}

object Constants {
    // HID related UUIDs
    val HOGP_UUID = ParcelUuid.fromString("00001812-0000-1000-8000-00805f9b34fb")
    val HID_UUID = ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb")
    val DIS_UUID = ParcelUuid.fromString("0000180A-0000-1000-8000-00805F9B34FB")
    val BAS_UUID = ParcelUuid.fromString("0000180F-0000-1000-8000-00805F9B34FB")
    const val ID_KEYBOARD: Byte = 1
    const val ID_REMOTE_CONTROL: Byte = 2
    const val ENABLE_BT = 10
    private val HID_REPORT_DESC = byteArrayOf(
        0x05.toByte(),
        0x01.toByte(),
        0x09.toByte(),
        0x06.toByte(),
        0xA1.toByte(),
        0x01.toByte(),
        0x85.toByte(),
        ID_KEYBOARD,
        0x05.toByte(),
        0x07.toByte(),
        0x19.toByte(),
        0xE0.toByte(),
        0x29.toByte(),
        0xE7.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x25.toByte(),
        0x01.toByte(),
        0x75.toByte(),
        0x01.toByte(),
        0x95.toByte(),
        0x08.toByte(),
        0x81.toByte(),
        0x02.toByte(),
        0x75.toByte(),
        0x08.toByte(),
        0x95.toByte(),
        0x01.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x26.toByte(),
        0xFF.toByte(),
        0x00.toByte(),
        0x05.toByte(),
        0x07.toByte(),
        0x19.toByte(),
        0x00.toByte(),
        0x29.toByte(),
        0xFF.toByte(),
        0x81.toByte(),
        0x00.toByte(),
        0xC0.toByte(),
        0x05.toByte(),
        0x0c.toByte(),
        0x09.toByte(),
        0x01.toByte(),
        0xa1.toByte(),
        0x01.toByte(),
        0x85.toByte(),
        ID_REMOTE_CONTROL,
        0x19.toByte(),
        0x00.toByte(),
        0x2a.toByte(),
        0xff.toByte(),
        0x03.toByte(),
        0x75.toByte(),
        0x0a.toByte(),
        0x95.toByte(),
        0x01.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x26.toByte(),
        0xff.toByte(),
        0x03.toByte(),
        0x81.toByte(),
        0x00.toByte(),
        0xc0.toByte()
    )
    private const val SDP_NAME = "VirtualRemote"
    private const val SDP_DESCRIPTION = "VirtualRemote"
    private const val SDP_PROVIDER = "AAM"
    private const val QOS_TOKEN_RATE = 800 // 9 bytes * 1000000 us / 11250 us
    private const val QOS_TOKEN_BUCKET_SIZE = 9
    private const val QOS_PEAK_BANDWIDTH = 0
    private const val QOS_LATENCY = 11250
    @RequiresApi(Build.VERSION_CODES.P)
    val SDP_RECORD = BluetoothHidDeviceAppSdpSettings(
        SDP_NAME,
        SDP_DESCRIPTION,
        SDP_PROVIDER,  //                    BluetoothHidDevice.SUBCLASS1_MOUSE,
        //                    BluetoothHidDevice.SUBCLASS1_KEYBOARD,
        //                    BluetoothHidDevice.SUBCLASS1_COMBO,
//                            BluetoothHidDevice.SUBCLASS2_REMOTE_CONTROL,
        BluetoothHidDevice.SUBCLASS2_REMOTE_CONTROL,  //                    BluetoothHidDevice.SUBCLASS1_NONE,
        HID_REPORT_DESC
    )

    //                    Constants.HID_REPORT_DESC_TEST);
    @RequiresApi(Build.VERSION_CODES.P)
    val QOS_OUT = BluetoothHidDeviceAppQosSettings(
        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
        QOS_TOKEN_RATE,
        QOS_TOKEN_BUCKET_SIZE,
        QOS_PEAK_BANDWIDTH,
        QOS_LATENCY,
        BluetoothHidDeviceAppQosSettings.MAX
    )
}