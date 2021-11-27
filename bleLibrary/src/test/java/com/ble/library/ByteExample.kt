package com.ble.library

import com.ble.library.service.ext.*
import com.ble.library.utils.ByteUtils

class ByteExample {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val magic = "0xAB"
            val version = 1
            val askFlag = 0
            val errorFlag = 1
            val value = version shl 4 + askFlag shl 3 + errorFlag shl 2
            println("value:$value")
            ByteUtils.byteMerger(
                magic.toByteArray(),
                ByteUtils.intToByte4(value)
            ).print()
            println("-----")

            (magic + value.toString()).toByteArray().print()
            println()
            println("-----")
            val ba = byteArrayOf(0x00, 0x0a, 0xff.toByte(), 0xf6.toByte(), 0x35, 0x33, 0x38, 0x03)
            ba.print()
            println()

            println("-- byteArray.writeInt8 --")
            val byteArray = ByteArray(8)
            byteArray.writeInt8(0xAB, 0)
            byteArray.writeInt8(value, 1)
            byteArray.print()
            println(byteArray.toHexString())
            println()

            println("-- byteArray.writeInt16BE --")
            val byteArray2 = ByteArray(8)
            byteArray2.writeInt16BE(0xAB, 0)
            byteArray2.writeInt16BE(value, 1)
            byteArray2.print()

            println(byteArray2.toHexString())
            println()


            println("-- byteArray.writeInt16LE --")
            val byteArray3 = ByteArray(8)
            byteArray3.writeInt16LE(0xAB, 0)
            byteArray3.writeInt16LE(value, 1)
            byteArray3.print()

            println(byteArray3.toHexString())
            println()
            println("-----")
            println()
            println("-----")

            println("toHexString: " + ba.toHexString())
            println("-----")
            println("readInt8: " + ba.readInt8())
            println("readInt8(2): " + ba.readInt8(2))
            println("-----")
            println("readUInt8() " + ba.readUInt8())
            println("readUInt8(2) " + ba.readUInt8(2))

            println("-----")
            println("readUInt16BE: " + ba.readUInt16BE())
            println("readUInt16BE(2): " + ba.readUInt16BE(2))
            println("-----")
            println("readInt16BE(): " + ba.readInt16BE())
            println("readInt16BE(2): " + ba.readInt16BE(2))
            println("-----")
            println("readUInt16LE: " + ba.readUInt16LE())
            println("readUInt16LE(2): " + ba.readUInt16LE(2))
            println("-----")
            println("readInt16LE(): " + ba.readInt16LE())
            println("readInt16LE(2): " + ba.readInt16LE(2))
            println("-----")
            println("readUInt32BE(): " + ba.readUInt32BE())
            println("readUInt32BE(2): " + ba.readUInt32BE(2))
            println("-----")
            println("readInt32BE(): " + ba.readInt32BE())
            println("readInt32BE(2): " + ba.readInt32BE(2))
            println("-----")
            println("readUInt32LE(): " + ba.readUInt32LE())
            println("readUInt32LE(2): " + ba.readUInt32LE(2))
            println("-----")
            println("readInt32LE(): " + ba.readInt32LE())
            println("readInt32LE(2): " + ba.readInt32LE(2))
            println("-----")
            println("readStringBE(0, 4): " + ba.readStringBE(0, 4))
            println("readStringBE(4, 4, \"ascii\"): " + ba.readStringBE(4, 4, "ascii"))
            println("-----")
            println("readStringLE(0, 4): " + ba.readStringLE(0, 4))
            println("readStringLE(4, 4, \"ascii\"): " + ba.readStringLE(4, 4, "ascii"))
            println("-----")
            println("ba.readFloatBE(0): " + ba.readFloatBE(0))
            println("readFloatLE(0): " + ba.readFloatLE(0))
            println("-----")
            println("readFloatBE(3): " + ba.readFloatBE(3))
            println("readFloatLE(3): " + ba.readFloatLE(3))

            println("-----")
            val insertByteArrayBE = ba.insertByteArrayBE(byteArrayOf(0x11, 0x22, 0x33))
            println("insertByteArrayBE(byteArrayOf(0x11, 0x22, 0x33)): ")
            insertByteArrayBE.print()
            println("-----")
            val insertByteArrayLE = ba.insertByteArrayLE(byteArrayOf(0x11, 0x22, 0x33), 3)
            println("insertByteArrayLE(byteArrayOf(0x11, 0x22, 0x33)):")
            insertByteArrayLE.print()

            println("-----")
            ba.writeStringBE("11 22 33")
            println("writeStringBE(\"11 22 33\"): ")
            ba.print()
            println("-----")
            ba.writeStringLE("3.1", 3, "ascii")
            println("writeStringLE(\"3.1\", 3, \"ascii\"): ")
            ba.print()
            println("-----")
            val str = ba.toHexString()
            str.hex2ByteArray().print()
            println(str)

            val arr = byteArrayOf(0x04, 0x04, 0x00, 0x00, 0x00, 0x04)
            println(CrcUtils.getCrc16Str(arr).toHexString())
            println(CrcUtils.getCrc16Str(arr).readInt8(1))

//            val string = "0A06823030302E31343417"
//            println(isFullGlsB40Data(string.hex2ByteArray()))
        }

        private fun isFullGlsB40Data(bytes: ByteArray): Boolean {
            var i = 0
            for (j in 0 until bytes.size - 1) {
                i += bytes[j]
            }
            return ((i xor 0xff) + 1) == bytes.last().toInt()
        }

    }
}