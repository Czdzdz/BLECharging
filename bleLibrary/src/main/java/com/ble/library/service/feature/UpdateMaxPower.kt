package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_UPDATE_CHARGING_MAX_POWER
import com.ble.library.service.DataMaker

/**
 * 更新充电过程中最大充电功率
 */
class UpdateMaxPowerData(override var hexCode: Byte) : BLEDiagramJson {
    init {
        hexCode = CMD_UPDATE_CHARGING_MAX_POWER
    }

    var power: ByteArray = ByteArray(4)
}

class UpdateMaxPower(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val updateMaxPowerDataJson = json as? UpdateMaxPowerData
        return if (updateMaxPowerDataJson == null) {
            null
        } else {
            val maker = DataMaker()
            maker.append(json.power, 4)
            maker.final()
        }
    }
}