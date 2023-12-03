package com.example.virtualcompanion

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

object HidUtils {
    const val TAG = "Anirudh"
     fun INPUT(size: Int): Byte {
        return (0x80 or size).toByte()
    }

     fun OUTPUT(size: Int): Byte {
        return (0x90 or size).toByte()
    }

     fun COLLECTION(size: Int): Byte {
        return (0xA0 or size).toByte()
    }

     fun FEATURE(size: Int): Byte {
        return (0xB0 or size).toByte()
    }

     fun END_COLLECTION(size: Int): Byte {
        return (0xC0 or size).toByte()
    }

     fun USAGE_PAGE(size: Int): Byte {
        return (0x04 or size).toByte()
    }

     fun LOGICAL_MINIMUM(size: Int): Byte {
        return (0x14 or size).toByte()
    }

     fun LOGICAL_MAXIMUM(size: Int): Byte {
        return (0x24 or size).toByte()
    }

     fun PHYSICAL_MINIMUM(size: Int): Byte {
        return (0x34 or size).toByte()
    }

     fun PHYSICAL_MAXIMUM(size: Int): Byte {
        return (0x44 or size).toByte()
    }

     fun UNIT_EXPONENT(size: Int): Byte {
        return (0x54 or size).toByte()
    }

     fun UNIT(size: Int): Byte {
        return (0x64 or size).toByte()
    }

     fun REPORT_SIZE(size: Int): Byte {
        return (0x74 or size).toByte()
    }

     fun REPORT_ID(size: Int): Byte {
        return (0x84 or size).toByte()
    }

     fun REPORT_COUNT(size: Int): Byte {
        return (0x94 or size).toByte()
    }

    /**
     * Local items
     */
     fun USAGE(size: Int): Byte {
        return (0x08 or size).toByte()
    }

     fun USAGE_MINIMUM(size: Int): Byte {
        return (0x18 or size).toByte()
    }

     fun USAGE_MAXIMUM(size: Int): Byte {
        return (0x28 or size).toByte()
    }

     fun LSB(value: Int): Byte {
        return (value and 0xff).toByte()
    }

     fun MSB(value: Int): Byte {
        return (value shr 8 and 0xff).toByte()
    }

    val REPORT_MAP = byteArrayOf(
        USAGE_PAGE(1),
        0x01,  // Generic Desktop Ctrls
        USAGE(1),
        0x06,  // Keyboard
        COLLECTION(1),
        0x01,  // Application
        USAGE_PAGE(1),
        0x07,  //   Kbrd/Keypad
        USAGE_MINIMUM(1),
        0xE0.toByte(),
        USAGE_MAXIMUM(1),
        0xE7.toByte(),
        LOGICAL_MINIMUM(1),
        0x00,
        LOGICAL_MAXIMUM(1),
        0x01,
        REPORT_SIZE(1),
        0x01,  //   1 byte (Modifier)
        REPORT_COUNT(1),
        0x08,
        INPUT(1),
        0x02,  //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position
        REPORT_COUNT(1),
        0x01,  //   1 byte (Reserved)
        REPORT_SIZE(1),
        0x08,
        INPUT(1),
        0x01,  //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
        REPORT_COUNT(1),
        0x05,  //   5 bits (Num lock, Caps lock, Scroll lock, Compose, Kana)
        REPORT_SIZE(1),
        0x01,
        USAGE_PAGE(1),
        0x08,  //   LEDs
        USAGE_MINIMUM(1),
        0x01,  //   Num Lock
        USAGE_MAXIMUM(1),
        0x05,  //   Kana
        OUTPUT(1),
        0x02,  //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
        REPORT_COUNT(1),
        0x01,  //   3 bits (Padding)
        REPORT_SIZE(1),
        0x03,
        OUTPUT(1),
        0x01,  //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
        REPORT_COUNT(1),
        0x06,  //   6 bytes (Keys)
        REPORT_SIZE(1),
        0x08,
        LOGICAL_MINIMUM(1),
        0x00,
        LOGICAL_MAXIMUM(1),
        0x65,  //   101 keys
        USAGE_PAGE(1),
        0x07,  //   Kbrd/Keypad
        USAGE_MINIMUM(1),
        0x00,
        USAGE_MAXIMUM(1),
        0x65,
        INPUT(1),
        0x00,  //   Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
        END_COLLECTION(0)
    )

    @SuppressLint("MissingPermission")
    fun isProfileSupported(device: BluetoothDevice): Boolean {
        // If a device reports itself as a HID Device, then it isn't a HID Host.
        val uuidArray = device.uuids
        if (uuidArray != null) {
            for (uuid in uuidArray) {
                if (Constants.HID_UUID.equals(uuid) || Constants.HOGP_UUID.equals(uuid)) {
                    return false
                }
            }
        }
        return true
    }

}