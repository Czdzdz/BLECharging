package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.DataMaker
import com.ble.library.utils.ToolsUtils

/**
 * OTA更新
 */
class OTAUpdateData(override var hexCode: Byte) : BLEDiagramJson {

    var firmwareInfo: ByteArray = ByteArray(1)
}

class OTAUpdate(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val otaJson = json as? OTAUpdateData
        val marker = DataMaker()
        if (otaJson != null) {
            marker.append(otaJson.firmwareInfo.size, 3)
            marker.append(ToolsUtils.sumCheck(otaJson.firmwareInfo, 8), 1)
            marker.append(otaJson.firmwareInfo)
        }

        return marker.final()
    }
}
