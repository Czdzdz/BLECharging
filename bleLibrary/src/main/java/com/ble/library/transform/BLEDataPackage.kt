package com.ble.library.transform

import com.ble.library.utils.ByteUtils
import com.ble.library.utils.CRC16

class BLEDataPackage {

    private var sequence_id = 0

    companion object {
        private var instance: BLEDataPackage? = null
            get() {
                if (field == null) {
                    field = BLEDataPackage()
                }
                return field
            }

        fun get(): BLEDataPackage {
            return instance!!
        }
    }

    internal fun generateWithCode(hexCode: Byte, payload: ByteArray?): ByteArray? {
        val headerWithoutCRC16 =
            generateHeaderWithCode(hexCode, payloadLength = (payload?.size ?: 0))
        val crc16 = calculateCRC16WithHeader(headerWithoutCRC16, payload = payload)
        return ByteUtils.byteMergerMore(headerWithoutCRC16, crc16, payload)
    }


    private fun generateHeaderWithCode(hexCode: Byte, payloadLength: Int): ByteArray? {

        var header = ByteArray(7)
        //Magic byte
        header[0] = 0xAB.toByte()
        //Version ACK_flag Err_flag Reversed
        header[1] = 1
        //Payload length
        header[2] = (payloadLength - (payloadLength shr 8 shl 8)).toByte()
        header[3] = (payloadLength shr 8).toByte()

        //Sequence id
        sequence_id++
        if (sequence_id > 65535) {
            sequence_id = 0
        }
        header[4] = (sequence_id - (sequence_id shr 8 shl 8)).toByte()
        header[5] = (sequence_id shr 8).toByte()

        //Command_ID
        header[6] = hexCode
        return ByteUtils.subBytes(header, 0, header.size)
    }

     fun calculateCRC16WithHeader(header: ByteArray?, payload: ByteArray?): ByteArray? {
        val diagramData = ByteUtils.byteMerger(header, payload)

        val crc16Ccitt = CRC16.CRC16_CCITT(diagramData)

        val crc = ByteUtils.intToByte32(crc16Ccitt)
        return ByteUtils.subBytes(crc, 0, 2)
    }

}
