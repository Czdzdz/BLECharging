package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.DataMaker

/**
 * 启动充电
 */
class StartChargeData(override var hexCode: Byte) : BLEDiagramJson {

    var orderId: String = ""
    var delayTime: ByteArray = ByteArray(4)
    var maxPower: ByteArray = ByteArray(4)
    var maxChargingTime: ByteArray = ByteArray(4)
}

class StartCharge(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val startChargeDataJson = json as? StartChargeData
        return if (startChargeDataJson == null) {
            null
        } else {
            val maker = DataMaker()
            maker.append(json.orderId, 6)
            maker.append(json.delayTime, 4)
            maker.append(json.maxChargingTime, 4)
            maker.append(json.maxPower, 4)
            maker.final()
        }
    }
}