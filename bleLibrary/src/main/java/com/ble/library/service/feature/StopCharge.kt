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
            Log.e("fuck", "orderNum:$orderNum")

            val payloadBytes = ByteArray(6)
            var p = 0
                var i = 11
                while (i > 0) {
                    val a = orderNum.substring(i, i + 1).toInt()
                    val b = orderNum.substring(i - 1, i).toInt()
                    Log.e("fuck", "a=$a,b=$b,运算:${(b * 16 + a)}")
                    payloadBytes[p] = Integer.toHexString(b * 16 + a).toByte()
                    p++
                    i -= 2
                }

            for (i in payloadBytes.indices) {
                Log.e("fuck", "数组的值:" + payloadBytes[i])
            }
            val data = ByteArray(payloadBytes.size) { payloadBytes[it] }
            data
//            val maker = DataMaker()
//            maker.append(json.orderId, 6)
//            maker.final()
        }
    }

}