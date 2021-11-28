package com.ble.library.transform

import cn.com.heaton.blelibrary.ble.BleLog
import com.ble.library.service.ServiceFactory
import com.ble.library.utils.ByteUtils


class BLEDispatcher {
    companion object {
        private var instance: BLEDispatcher? = null
            get() {
                if (field == null) {
                    field = BLEDispatcher()
                }
                return field
            }

        fun get(): BLEDispatcher {
            return instance!!
        }
    }

    /**
     * 发送数据包
     */
    fun sendWithCode(code: Byte, payload: ByteArray) {
        BleLog.e("send", "Bluetooth send package data:${payload}")
        val mDiagramData = BLEDataPackage.get().generateWithCode(code, payload)
        val bleConnection = BLEConnection.get()
        val peripheral = bleConnection.getConnectedBLEDevice()
        if (peripheral == null || bleConnection.getBLEConnectState(peripheral) == false) {
            BleLog.e("send", "外设不存在或者外设出于非连接状态")
        } else {
            mDiagramData?.let { data ->
                if (data.size > 20) {
                    val size = data.size / 20
                    for (i in 0..size) {
                        val subBytes = ByteUtils.subBytes(data, i * 20, 20)
                        bleConnection.sendDataWithCallback(peripheral, subBytes)
                        BleLog.e("sendWithCode", "subBytes:${subBytes}")
                    }
                } else {
                    bleConnection.sendDataWithCallback(peripheral, data)
                    BleLog.e("sendWithCode", "diagramData:${mDiagramData}")
                }
            }
        }
    }

    fun onReceive(payload: ByteArray?) {
        payload?.let { data ->
            val magicByte = data[0]
            if (magicByte == 0xAB.toByte()) {
                if (checkCRC16(data) == false) {
                    BleLog.e("onReceive", "crc校验不通过")
                } else {
                    BleLog.e("onReceive", "crc校验通过")

                    val payloadData = ByteUtils.subBytes(data, 9, data.size - 9)
                    val code = data[6]
                    ServiceFactory.get().executeUp(code, payloadData)
                }
            }
        }
    }

    private fun checkCRC16(packet: ByteArray?): Boolean? {
        return packet?.let { data ->
            val header = ByteUtils.subBytes(data, 0, 7)
            val crc16Packet = ByteUtils.subBytes(data, 7, 2)
            val payload = ByteUtils.subBytes(data, 9, data.size - 9)
            val calCrc16 = BLEDataPackage.get().calculateCRC16WithHeader(header, payload)
            val result = crc16Packet[0] == calCrc16[1] && crc16Packet[1] == calCrc16[0]
            return result
        }
    }
}