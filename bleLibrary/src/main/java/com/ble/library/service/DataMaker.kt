package com.ble.library.service

class DataMaker {
    private var data: String = ""

    fun append(value: Any, size: Int? = 0) {
        data += if (size != null && value.toString().length >= size) {
            value.toString().substring(0, size)
        } else {
            value.toString()
        }

    }

    fun final(): ByteArray {
        return data.toByteArray()
    }
}