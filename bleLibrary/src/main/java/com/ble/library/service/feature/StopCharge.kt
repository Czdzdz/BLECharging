package com.ble.library.service.feature

import android.util.Log
import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_END_CHARGE

/**
 * 结束充电
 */
class StopChargeData(
    override var hexCode: Byte = CMD_END_CHARGE,
    var orderId: String
) : BLEDiagramJson

class StopCharge(override var hexCode: Byte) : BLEDiagram {

    override fun jsonToPayload(json: BLEDiagramJson): ByteArray? {
        val stopChargeData = json as? StopChargeData
        return if (stopChargeData == null) {
            null
        } else {
            val orderNum = json.orderId
            Log.e("StopCharge", "orderNum:$orderNum")

            val payloadBytes = ByteArray(6)
            var p = 0
            var i = 11
            while (i > 0) {
                val a = orderNum.substring(i, i + 1).toInt()
                val b = orderNum.substring(i - 1, i).toInt()
                Log.e("StopCharge", "a=$a,b=$b,运算:${(b * 16 + a)}")
                payloadBytes[p] = Integer.toHexString(b * 16 + a).toByte()
                p++
                i -= 2
            }

            for (index in payloadBytes.indices) {
                Log.e("StopCharge", "数组的值:" + payloadBytes[index])
            }

            ByteArray(payloadBytes.size) { payloadBytes[it] }
        }
    }

}