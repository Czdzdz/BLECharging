package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_CHARGE_START_RESULT
import com.ble.library.service.DataIterator
import com.ble.library.transform.BLECommonDataError
import com.ble.library.utils.ByteUtils

class ChargeStateResultData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_CHARGE_START_RESULT
    }

    var orderId: String = ""
    var chargingTemperature: ByteArray = ByteArray(0)
    var chargingDegree: ByteArray = ByteArray(4)
    var chargingVoltage: ByteArray = ByteArray(2)
    var chargingCurrent: ByteArray = ByteArray(2)
    var chargingPower: ByteArray = ByteArray(2)
    var lastTimestamp: ByteArray = ByteArray(8)
    var numberIfTimePeriods: ByteArray = ByteArray(0)
    var timePeriodInfo: ArrayList<TimePeriodInfo> = arrayListOf()

    class TimePeriodInfo {
        lateinit var beginTimestamp: ByteArray
        lateinit var endTimestamp: ByteArray
        lateinit var degree: ByteArray
    }

    class ChargeStateResult(override var hexCode: Byte) : BLEDiagram {
        init {
            hexCode = CMD_CHARGE_START_RESULT
        }

        override fun payloadToJson(payload: ByteArray): BLEDiagramJson {
            return if (payload.size < 42) {
                throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
            } else {
                val dataIterator = DataIterator(payload)
                val json = ChargeStateResultData(hexCode)
                json.orderId = dataIterator.stringValue(6)
                json.chargingTemperature = dataIterator.integerValueToByteArray(1)
                json.chargingDegree = dataIterator.integerValueToByteArray(4)
                json.chargingVoltage = dataIterator.integerValueToByteArray(2)
                json.chargingCurrent = dataIterator.integerValueToByteArray(2)
                json.chargingPower = dataIterator.integerValueToByteArray(4)
                json.lastTimestamp = dataIterator.integerValueToByteArray(6)
                json.numberIfTimePeriods = dataIterator.integerValueToByteArray(1)
                val timePeriodInfoList: ArrayList<TimePeriodInfo> = arrayListOf()
                while (!dataIterator.isEnd) {
                    val bytesValue = dataIterator.bytesValue(16)
                    val timePeriodInfo = TimePeriodInfo()
                    timePeriodInfo.beginTimestamp = ByteUtils.subBytes(bytesValue, 0, 6)
                    timePeriodInfo.endTimestamp = ByteUtils.subBytes(bytesValue, 6, 6)
                    timePeriodInfo.degree = ByteUtils.subBytes(bytesValue, 12, 4)
                    timePeriodInfoList.add(timePeriodInfo)
                }
                json.timePeriodInfo = timePeriodInfoList
                json
            }
        }
    }
}

