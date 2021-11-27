package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_QUERY_CONFIG
import com.ble.library.service.DataIterator
import com.ble.library.transform.BLECommonDataError

/**
 * 查询配置信息
 */
class QueryConfigData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_QUERY_CONFIG
    }

    var device_id: String = ""
    var period: Byte = 0
    var ble_key: String = "123456"
    var hardwareVersion: String = ""
    var softwareVersion: String = ""
    var power: ByteArray = ByteArray(4)
}

class QueryConfig(override var hexCode: Byte) : BLEDiagram {
    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.size < 61) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = QueryConfigData(hexCode)
            json.device_id = dataIterator.stringValue(30)
            json.period = dataIterator.integerValueToByte(1)
            json.ble_key = dataIterator.stringValue(6)
            json.hardwareVersion = dataIterator.stringValue(10)
            json.softwareVersion = dataIterator.stringValue(10)
            json.power = dataIterator.integerValueToByteArray(4)
            json
        }
    }
}
