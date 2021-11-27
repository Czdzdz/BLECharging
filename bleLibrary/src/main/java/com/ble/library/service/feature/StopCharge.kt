package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_END_CHARGE
import com.ble.library.service.DataMaker

/**
 * 结束充电
 */
class StopChargeData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_END_CHARGE
    }

    var orderId: String = ""
}

class StopCharge(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val stopChargeData = json as? StopChargeData
        return if (stopChargeData == null) {
            null
        } else {
            val maker = DataMaker()
            maker.append(json.orderId, 6)
            maker.final()
        }
    }

}