package com.ble.library.transform

import com.ble.library.service.DataIterator
import com.ble.library.service.DataMaker
import com.ble.library.utils.ByteUtils
import com.ble.library.utils.CRC16
import kotlin.experimental.and

class BLECommonDataError(var type: CodeType) : Exception() {
    var code: Int = 0
    private var description: String = ""

    enum class CodeType {
        MAGIC_BYTE_NOT_SUPPORT,
        CRC_CHECK_FAIL,
        PAYLOAD_DATA_LENGTH_ERROR,
        FORMAT_ERROR
    }

    init {
        code = when (type) {
            CodeType.MAGIC_BYTE_NOT_SUPPORT -> 0x01
            CodeType.CRC_CHECK_FAIL -> 0x02
            CodeType.PAYLOAD_DATA_LENGTH_ERROR -> 0x10
            CodeType.FORMAT_ERROR -> 0x11
        }

        description = when (type) {
            CodeType.MAGIC_BYTE_NOT_SUPPORT -> "Magic byte不支持"
            CodeType.CRC_CHECK_FAIL -> "CRC校验失败"
            CodeType.PAYLOAD_DATA_LENGTH_ERROR -> "payload长度不对"
            CodeType.FORMAT_ERROR -> "data格式不对"
        }
    }

    fun mapData(): ByteArray {
        val maker = DataMaker()
        maker.append(code, 1)
        maker.append(description)
        return maker.final()
    }
}

open class BLEHeader {
    open var byteCount: Int = 0
    open fun mapData(payloadLength: Int): ByteArray {
        return ByteArray(4)
    }

    open var sequenceId: Long = 0
    open var commandId: Byte = 0

    open var isLastPart: Boolean = false

    open var errorFlag: Byte = 0
    open var magic: String = ""
}

class BLECommonHeader : BLEHeader() {
    override var magic: String = "0xAB"
    var version: Int = 1
    var askFlag: Byte = 0
    override var errorFlag: Byte = 0
    override var sequenceId: Long = 0
    override var commandId: Byte = 0
    override var byteCount: Int = 9
    override var isLastPart: Boolean = true

    override fun mapData(payloadLength: Int): ByteArray {
        val maker = DataMaker()
        //Magic byte
        maker.append(magic, 1)
        //Version ACK_flag Err_flag Reversed
        val byteMerge = version shl 4 + askFlag shl 3 + errorFlag shl 2
        val intToByte32 = ByteUtils.intToByte32(byteMerge)
        maker.append(intToByte32, 1)
        //Payload length
        maker.append(payloadLength, 4)
        //Sequence id
        maker.append(sequenceId, 2)
        //Command_ID
        maker.append(commandId, 1)
        return maker.final()
    }

    fun create(data: ByteArray): BLECommonHeader {
        if (data.size < byteCount) {
            throw BLECommonDataError(BLECommonDataError.CodeType.FORMAT_ERROR)
        }

        val dataIterator = DataIterator(data)
        val magic: Byte = dataIterator.integerValueToByte(1)
        val versionInfo: Byte = dataIterator.integerValueToByte(1)

        val bleCommonHeader = BLECommonHeader()
        bleCommonHeader.magic = magic.toString()
        bleCommonHeader.version = (versionInfo and 0b1111_0_0_00.toByte()).toInt()
        bleCommonHeader.askFlag = versionInfo and 0b0000_1_0_00
        bleCommonHeader.errorFlag = versionInfo and 0b0000_0_1_00
        bleCommonHeader.sequenceId = dataIterator.integerValueToLong(2)
        bleCommonHeader.commandId = dataIterator.integerValueToByte(2)
        return bleCommonHeader
    }
}

class BLEUnpackHeader : BLEHeader() {
    lateinit var common: BLECommonHeader
    var currentIndex: Int = 0
    var packCount: Int = 0

    override var sequenceId: Long = common.sequenceId

    override var commandId: Byte = common.commandId

    override var errorFlag: Byte
        get() {
            return common.errorFlag
        }
        set(value) {
            common.errorFlag = value
        }

    override var magic: String
        get() {
            return common.magic
        }
        set(value) {
            common.magic = value
        }

    override var byteCount: Int = common.byteCount + 4

    override fun mapData(payloadLength: Int): ByteArray {
        val maker = DataMaker()
        // payload
        maker.append(common.mapData(payloadLength))
        // currentIndex
        maker.append(currentIndex, 2)
        // packCount
        maker.append(packCount, 2)
        return maker.final()
    }

    fun create(data: ByteArray): BLEUnpackHeader {
        if (data.size < byteCount) {
            throw BLECommonDataError(BLECommonDataError.CodeType.FORMAT_ERROR)
        }
        val dataIterator = DataIterator(data)
        val bleUnpackHeader = BLEUnpackHeader()
        bleUnpackHeader.common = common.create(dataIterator.dataValue(common.byteCount))
        bleUnpackHeader.currentIndex = dataIterator.integerValueToInt(2)
        bleUnpackHeader.packCount = dataIterator.integerValueToInt(2)
        return bleUnpackHeader
    }

    override var isLastPart: Boolean = currentIndex >= packCount
}

class BLEData(header: BLEHeader, payload: ByteArray) {

    companion object {
        const val payloadMaxCount: Int = Int.MAX_VALUE
    }

    var count: Int = 0
    private val crcCount: Int = 2
    var headerWithoutCRC: BLEHeader
    private val payloadResult: ByteArray
    private var errorFlag: Byte

    init {
        if (payload.size > payloadMaxCount) {
            header.errorFlag = 1
            headerWithoutCRC = header
            payloadResult =
                BLECommonDataError(BLECommonDataError.CodeType.PAYLOAD_DATA_LENGTH_ERROR).mapData()
        } else {
            headerWithoutCRC = header
            payloadResult = payload
        }

        count = headerWithoutCRC.byteCount + crcCount + payload.size
        errorFlag = headerWithoutCRC.errorFlag
    }

    var isCommonPackage: Boolean = headerWithoutCRC is BLECommonHeader

    private var payloadData: ByteArray = payloadResult

    fun mapData(): ByteArray {
        val result = ByteArray(8)
        val header = headerWithoutCRC.mapData(payloadData.count())
        ByteUtils.byteMergerMore(header, calculateCRC16(header, payloadData), payloadData)
        return result
    }

    fun create(data: ByteArray): BLEData {
        val header: BLEHeader
        when (data[0]) {
            0xAB.toByte(), 0xAD.toByte() -> {
                val commonHeader = BLECommonHeader().create(data)
                commonHeader.magic = "0xAB"
                header = commonHeader
            }
            0xAC.toByte() -> {
                header = BLEUnpackHeader().create(data)
            }
            else -> {
                throw BLECommonDataError(BLECommonDataError.CodeType.MAGIC_BYTE_NOT_SUPPORT)
            }
        }

        val dataIterator = DataIterator(data)

        val headerWithoutCRCByteCount = header.byteCount

        val headerWithoutCRCByte = dataIterator.dataValue(headerWithoutCRCByteCount)
        val dataCRC = dataIterator.dataValue(2)
        val payload = dataIterator.dataValue()

        val crc16 = calculateCRC16(headerWithoutCRCByte, payload)
        if (!crc16.contentEquals(dataCRC)) {
            throw BLECommonDataError(BLECommonDataError.CodeType.CRC_CHECK_FAIL)
        }
        return BLEData(header, payload)
    }

    /**
     * CRC16校验
     */
    private fun calculateCRC16(header: ByteArray, payload: ByteArray): ByteArray {
        val diagramData = ByteUtils.byteMerger(header, payload)
        val crc16Ccitt = CRC16.CRC16_CCITT(diagramData)

        val crc = ByteUtils.intToByte32(crc16Ccitt)
        return ByteUtils.subBytes(crc, 0, 2)
    }
}