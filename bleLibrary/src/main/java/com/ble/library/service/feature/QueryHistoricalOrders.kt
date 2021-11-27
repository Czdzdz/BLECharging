package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_QUERY_HISTORICAL_ORDERS
import com.ble.library.service.DataIterator
import com.ble.library.transform.BLECommonDataError
import com.ble.library.utils.ByteUtils

/**
 * 查询历史订单
 */
class QueryHistoricalOrdersData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_QUERY_HISTORICAL_ORDERS
    }

    var orderId: String = ""

    var startChargeType: Byte = 0
    var startChargeTimestamp: ByteArray = ByteArray(8)

    var stopChargeReason: Byte = 0
    var stopChargeTimestamp: ByteArray = ByteArray(8)

    var totalDegree: ByteArray = ByteArray(4)
    var lastTimestamp: ByteArray = ByteArray(8)

    var numberIfTimePeriods: Byte = 0
    var timePeriodInfo: ArrayList<TimePeriodInfo> = arrayListOf()

    class TimePeriodInfo() {
        lateinit var beginTimestamp: ByteArray
        lateinit var endTimestamp: ByteArray
        lateinit var degree: ByteArray
    }
}

class QueryHistoricalOrders(override var hexCode: Byte) : BLEDiagram {
    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.size < 47) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = QueryHistoricalOrdersData(hexCode)
            json.orderId = dataIterator.stringValue(6)
            json.startChargeType = dataIterator.integerValueToByte(1)
            json.startChargeTimestamp = dataIterator.integerValueToByteArray(6)
            json.stopChargeReason = dataIterator.integerValueToByte(1)
            json.stopChargeTimestamp = dataIterator.integerValueToByteArray(6)

            json.totalDegree = dataIterator.integerValueToByteArray(4)
            json.lastTimestamp = dataIterator.integerValueToByteArray(6)

            json.numberIfTimePeriods = dataIterator.integerValueToByte(1)
            val timePeriodInfoList: ArrayList<QueryHistoricalOrdersData.TimePeriodInfo> =
                arrayListOf()
            while (!dataIterator.isEnd) {
                val bytesValue = dataIterator.bytesValue(16)
                val timePeriodInfo = QueryHistoricalOrdersData.TimePeriodInfo()
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