package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_UPDATE_VEHICLE_WHITELIST
import com.ble.library.service.DataMaker

/**
 * 更新车辆白名单
 */
class UpdateVehicleWhitelistData(override var hexCode: Byte) : BLEDiagramJson {
    init {
        hexCode = CMD_UPDATE_VEHICLE_WHITELIST
    }

    var vins: ArrayList<String> = arrayListOf()
}

class UpdateVehicleWhitelist(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val updateVehicleWhitelistDataJson = json as? UpdateVehicleWhitelistData
        return if (updateVehicleWhitelistDataJson == null) {
            null
        } else {
            val maker = DataMaker()
            for (vin in json.vins) {
                maker.append(vin, 17)
            }
            maker.final()
        }
    }
}