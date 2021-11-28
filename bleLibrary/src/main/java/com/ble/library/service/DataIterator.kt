package com.ble.library.service

import com.ble.library.utils.ByteUtils

class DataIterator(data: ByteArray, maxSize: Int? = 0) {
    private val data: ByteArray = data
    private val maxSize = maxSize ?: data.size
    private var offset: Int = 0

    fun stringValue(): String {
        return ""
    }

    fun stringValue(size: Int?): String {
        return dataValue(size).toString()
    }

    fun dataValue(): ByteArray {
        return dataValue(null)
    }

    fun dataValue(size: Int?): ByteArray {
        return if (size != null) {
            val result = ByteUtils.subBytes(
                data, offset, if (offset + size > maxSize) {
                    maxSize
                } else {
                    offset + size
                }
            )
            offset += size
            result
        } else {
            offset = data.size
            val result2 = ByteUtils.subBytes(data, 0, offset)
            result2
        }
    }

    fun integerValueToByte(size: Int?): Byte {
        return ByteUtils.byteArray2Int(dataValue(size)).toByte()
    }

    fun integerValueToByteArray(size: Int?): ByteArray {
        return dataValue(size)
    }

    fun integerValueToString(size: Int?): String {
       return dataValue(size).toString()
    }

    fun bytesValue(size: Int): ByteArray {
        return dataValue(size)
    }

    fun integerValueToShort(size: Int): Short {
        return ByteUtils.byte2ToShort(dataValue(size))
    }

    fun integerValueToLong(size: Int): Long {
        return ByteUtils.byteArray2Int(dataValue(size)).toLong()
    }

    fun integerValueToInt(size: Int): Int {
        return ByteUtils.byteArray2Int(dataValue(size))
    }

    var isEnd: Boolean = offset >= maxSize!!
}