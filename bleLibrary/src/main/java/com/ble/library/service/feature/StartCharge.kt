package com.ble.library.service.feature

import android.util.Log
import com.ble.library.service.BLEDiagram
import com.ble.library.service.BLEDiagramJson
import com.ble.library.service.CMD_START_CHARGE

/**
 * 启动充电
 */
class StartChargeData(override var hexCode: Byte = CMD_START_CHARGE, var orderId: String) :
    BLEDiagramJson {

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
            val orderNum = json.orderId
            val payloadBytes = ByteArray(6)
            var p = 0
            var i = 11
            while (i > 0) {
                val a = orderNum.substring(i, i + 1).toInt()
                val b = orderNum.substring(i - 1, i).toInt()
                Log.e("StartCharge", "a=$a,b=$b,运算:${(b * 16 + a)}")
                payloadBytes[p] = Integer.toHexString(b * 16 + a).toByte()
                p++
                i -= 2
            }

            for (index in payloadBytes.indices) {
                Log.e("StartCharge", "数组的值:" + payloadBytes[index])
            }

            ByteArray(payloadBytes.size) { payloadBytes[it] }
        }
    }
}