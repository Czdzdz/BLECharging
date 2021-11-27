package com.ble.library.service.feature

import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_QUERY_VEHICLE_WHITELIST
import com.ble.library.service.DataIterator
import com.ble.library.transform.BLECommonDataError

/**
 * 查询车辆白名单
 */
class QueryVehicleWhitelistData(override var hexCode: Byte) : BLEDiagramJson {
    init {
        hexCode = CMD_QUERY_VEHICLE_WHITELIST
    }

    var vins: ArrayList<String> = arrayListOf()
}

class QueryVehicleWhitelist(override var hexCode: Byte) : BLEDiagram {
    override fun payloadToJson(payload: ByteArray): BLEDiagramJson? {
        return if (payload.isEmpty() || payload.size % 17 != 0) {
            throw BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR)
        } else {
            val dataIterator = DataIterator(payload)
            val json = QueryVehicleWhitelistData(hexCode)
            while (!dataIterator.isEnd) {
                json.vins.add(dataIterator.stringValue(17))
            }
            json
        }
    }
}