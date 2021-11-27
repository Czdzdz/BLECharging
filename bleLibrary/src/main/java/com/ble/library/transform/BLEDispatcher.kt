package com.ble.library.transform

import cn.com.heaton.blelibrary.ble.BleLog

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
    fun send(code: Byte, payload: ByteArray) {
        BleLog.e("send", "Bluetooth send package data:${payload}")
        val diagramData = BLEDataPackage.get().generate(code, payload)
        val bleConnection = BLEConnection.get()
        val peripheral = bleConnection.getConnectedBLEDevice()
        if (peripheral == null || bleConnection.getBLEConnectState(peripheral) == false) {
            BleLog.e("send", "外设不存在或者外设出于非连接状态")
        } else {
            for (dataPack in diagramData) {
                bleConnection.sendDataWithCallback(peripheral, dataPack)
            }
        }
    }
}