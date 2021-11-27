package com.ble.library.transform

import com.ble.library.service.DataIterator

class BLEDataPackage {
    private var sequence_id: Sentinel = Sentinel()
    var packageSize: Int = 256

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

    fun generate(hexCode: Byte, payload: ByteArray): ArrayList<ByteArray> {
        val sequenceId = sequence_id.increase()
        val bleCommonHeader = BLECommonHeader()
        bleCommonHeader.sequenceId = sequenceId
        bleCommonHeader.commandId = hexCode

        val result = arrayListOf<ByteArray>()
        val bleData = BLEData(bleCommonHeader, payload)
        if (bleData.count <= packageSize) {
             result.add(bleData.mapData())
        } else {
            bleData.headerWithoutCRC.magic = "0xAD"
            val dataIterator = DataIterator(bleData.mapData(),BLEData.payloadMaxCount)
            val temp = arrayListOf<ByteArray>()
            while (!dataIterator.isEnd) {
                temp.add(dataIterator.dataValue(packageSize))
            }
            for ((offset, element) in temp.withIndex()) {
                val header = BLEUnpackHeader()
                header.common = bleCommonHeader
                header.currentIndex = offset + 1
                header.packCount = temp.size
                header.common.magic = "0xAC"
                result.add(BLEData(header, element).mapData())
            }
        }

        return result
    }
}

class Sentinel {
    private var _value: Long = 0

    fun increase(): Long {
        val result = _value++
        return if (result > 65535) {
            _value = 0
            0
        } else {
            result
        }
    }
}