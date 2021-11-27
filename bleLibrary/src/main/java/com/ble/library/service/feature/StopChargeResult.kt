package com.ble.library.service.feature

import cn.com.heaton.blelibrary.ble.BleLog
import com.ble.library.service.*
import com.ble.library.transform.BLECommonDataError

/**
 * 停止充电成功的结果推送
 */
class StopChargeSuccessData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_END_CHARGE_SUCCESS_RESULT
    }

    var orderId: String = ""
    var timestamp: ByteArray = ByteArray(8)
    var stopType: Byte = 0
}

class StopChargeSuccess(override var hexCode: Byte) : BLEDiagram {

    override fun payloadToJson(payload: ByteArray): BLEDiagramJson {
        return if (payload.size < 13) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = StopChargeSuccessData(hexCode)
            json.orderId = dataIterator.stringValue(6)
            json.timestamp = dataIterator.integerValueToByteArray(6)
            json.stopType = dataIterator.integerValueToByte(1)
            json
        }
    }
}


/**
 * 停止充电失败的结果推送
 */
class StopChargeFailData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_END_CHARGE_FAILURE_RESULT
    }

    var orderId: String = ""
    var errorCode: ByteArray = ByteArray(2)
    var reason: String = ""
}

class StopChargeFail(override var hexCode: Byte) : BLEDiagram {
    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.size < 8) {
            BleLog.e("StopChargeFail", " payload.size Error")
            null
        } else {
            val dataIterator = DataIterator(payload)
            val json = StopChargeFailData(hexCode)
            json.orderId = dataIterator.stringValue(6)
            json.errorCode = dataIterator.integerValueToByteArray(2)
            json.reason = dataIterator.stringValue()
            json
        }
    }
}