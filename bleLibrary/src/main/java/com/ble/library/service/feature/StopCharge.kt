package com.ble.library.service.feature

import android.util.Log
import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_END_CHARGE

/**
 * 结束充电
 */
class StopChargeData(override var hexCode: Byte) : BLEDiagramJson {

    init {
        hexCode = CMD_END_CHARGE
    }

    var orderId: String = "123456789011"
}

class StopCharge(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val stopChargeData = json as? StopChargeData
        return if (stopChargeData == null) {
            null
        } else {
            var orderNum = json.orderId
            var payload_bytes = IntArray(8)
            Log.e("fuck", "orderNum:$orderNum")
            var p = 0
            var i = 11
            while (i > 0) {
                val a = orderNum.substring(i, i + 1).toByte()
                val b = orderNum.substring(i - 1, i).toByte()
                Log.e("fuck", "a=$a,b=$b,运算:${(b * 16 + a)}")
                payload_bytes[p] = b * 16 + a
                p++
                i -= 2
            }
            for (i in payload_bytes.indices) {
                Log.e("fuck", "数组的值:" + payload_bytes[i])
            }
            val data = ByteArray(payload_bytes.size) { payload_bytes[it].toByte() }
            data
//            val maker = DataMaker()
//            maker.append(json.orderId, 6)
//            maker.final()
        }
    }

}