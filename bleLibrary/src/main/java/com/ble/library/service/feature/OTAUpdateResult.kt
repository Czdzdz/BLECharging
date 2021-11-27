package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_OTA_UPDATE_RESULT
import com.ble.library.service.DataIterator
import com.ble.library.transform.BLECommonDataError

/**
 * OTA更新结果推送
 */
class OTAUpdateResultData(override var hexCode: Byte) : BLEDiagramJson {
    init {
        hexCode = CMD_OTA_UPDATE_RESULT
    }

    var updateResult: Byte = 0
    var hardwareVersion: String = ""
    var softwareVersion: String = ""
}

class OTAUpdateResult(override var hexCode: Byte) : BLEDiagram {

    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.size < 21) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = OTAUpdateResultData(hexCode)
            json.updateResult = dataIterator.integerValueToByte(1)
            json.hardwareVersion = dataIterator.stringValue(10)
            json.softwareVersion = dataIterator.stringValue(10)
            json
        }
    }
}