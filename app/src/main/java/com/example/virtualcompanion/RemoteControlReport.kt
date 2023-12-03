package com.example.virtualcompanion

object RemoteControlReport {
    val reportData = ByteArray(2)

    fun getReport(byte1: Int, byte2: Int): ByteArray? {
        //No need to fill with zero now
//        Arrays.fill(keyboardData, (byte) 0);

        // We have to swap the bytes here, as the data are sent with MSB on right,
        // for example 0x00ea should be sent as ea 00 -> 0x680
        reportData[0] = byte2.toByte()
        reportData[1] = byte1.toByte()

//        reportData[0] = (byte) byte1;
//        reportData[1] = (byte) byte2;
        return reportData
    }
}