package com.ble.library.service

interface BLEDiagramJson {
    var hexCode: Byte
}

interface BLEDiagram {
    var hexCode: Byte

    fun jsonToPayload(json: BLEDiagramJson): ByteArray? = ByteArray(18)

    fun payloadToJson(payload: ByteArray): BLEDiagramJson? = null
}