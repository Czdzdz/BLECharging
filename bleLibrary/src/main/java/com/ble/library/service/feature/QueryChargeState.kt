package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_QUERY_CHARGE_STATE
import com.ble.library.service.DataIterator
import com.ble.library.transform.BLECommonDataError

/**
 * 查询桩状态
 */
class QueryChargeStateData(override var hexCode: Byte) : BLEDiagramJson {
    init {
        hexCode = CMD_QUERY_CHARGE_STATE
    }

    /// 运行状态
    var run_status: Byte = 0

    /// 连接状态
    var connection_status: Byte = 0

    /// 充电状态信息推送触发
    var action: Byte = 0
}

class QueryChargeState(override var hexCode: Byte) : BLEDiagram {

    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.size < 3) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = QueryChargeStateData(hexCode)
            json.run_status = dataIterator.integerValueToByte(1)
            json.connection_status = dataIterator.integerValueToByte(1)
            json.action = dataIterator.integerValueToByte(1)
            json
        }
    }
}