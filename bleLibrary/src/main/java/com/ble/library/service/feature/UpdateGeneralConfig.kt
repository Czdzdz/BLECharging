package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_GENERAL_CONFIG_UPDATE
import com.ble.library.service.DataMaker

/**
 * 通用配置更新
 */
class UpdateGeneralConfigData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_GENERAL_CONFIG_UPDATE
    }

    var device_id: String = ""
    var period: Byte = 0
    var ble_key: String = "123456"
    var power: ByteArray = ByteArray(4)
}

class UpdateGeneralConfig(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val updateGeneralConfigDataJson = json as? UpdateGeneralConfigData
        return if (updateGeneralConfigDataJson == null) {
            null
        } else {
            val maker = DataMaker()
            maker.append(json.device_id, 30)
            maker.append(json.period, 1)
            maker.append(json.ble_key, 6)
            maker.append(json.power, 4)
            maker.final()
        }
    }
}