package com.ble.library.service.feature

import com.ble.library.service.*
import com.ble.library.transform.BLECommonDataError

/**
 * 启动充电成功的结果推送
 */
class StartChargeSuccessData(override var hexCode: Byte) : BLEDiagramJson {
    init {
        hexCode = CMD_START_CHARGE_SUCCESS_RESULT
    }

    var orderId: String = ""
    var timestamp: ByteArray = ByteArray(8)
}

class StartChargeSuccess(override var hexCode: Byte) : BLEDiagram {
    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.size < 12) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = StartChargeSuccessData(hexCode)
            json.orderId = dataIterator.stringValue(6)
            json.timestamp = dataIterator.integerValueToByteArray(6)
            json
        }
    }
}

/**
 * 启动充电失败的结果推送
 */
class StartChargeFailData(override var hexCode: Byte) : BLEDiagramJson {
    init {
        hexCode = CMD_START_CHARGE_FAILURE_RESULT
    }

    var orderId: String = ""
    var errorCode: ByteArray = ByteArray(8)
    var reason: String = ""
}

class StartChargeFail(override var hexCode: Byte) : BLEDiagram {
    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.size < 8) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = StartChargeFailData(hexCode)
            json.orderId = dataIterator.stringValue(6)
            json.errorCode = dataIterator.integerValueToByteArray(2)
            json.reason = dataIterator.stringValue()
            json
        }
    }
}