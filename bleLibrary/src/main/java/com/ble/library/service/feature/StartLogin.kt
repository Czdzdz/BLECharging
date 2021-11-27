package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_LOGIN
import com.ble.library.service.DataMaker

/**
 * 登录
 */
class StartLoginData(override var hexCode: Byte, var ble_key: String) : BLEDiagramJson {

    init {
        hexCode = CMD_LOGIN
    }
}

class StartLogin(override var hexCode: Byte = CMD_LOGIN) : BLEDiagram {
    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val startLoginData = json as? StartLoginData
        return if (startLoginData == null) {
            null
        } else {
            val maker = DataMaker()
            maker.append(json.ble_key, 6)
            maker.final()
        }
    }
}