package com.ble.library.service.ext

/**
 *  每两个字符反转一次
 */
fun String.reversalEvery2Charts(hasSpace: Boolean = false): String {
    val hex = this.addSpaceEvery2Charts()
    return hex.split(" ").reversed().joinToString(if (hasSpace) " " else "")
}

/**
 * 每两个字符添加一个空格
 */
fun String.addSpaceEvery2Charts(): String {
    val hex = this.replace(" ", "")
    val sb = StringBuilder()
    for (i in 0 until hex.length / 2) {
        sb.append(hex.substring(i * 2, i * 2 + 2))
        sb.append(" ")
    }
    return sb.toString().trim()
}

/**
 * HEX转ByteArray
 */
fun String.hex2ByteArray(): ByteArray {
    val s = this.replace(" ", "")
    val bs = ByteArray(s.length / 2)
    for (i in 0 until s.length / 2) {
        bs[i] = s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return bs
}

/**
 * ASCII转ByteArray
 */
fun String.ascii2ByteArray(hasSpace: Boolean = false): ByteArray {
    val s = if (hasSpace) this else this.replace(" ", "")
    return s.toByteArray(charset("US-ASCII"))
}

/**
 * 头部追加
 */
fun String.addFirst(s: String) = "$s$this"

/**
 * 尾部追加
 */
fun String.addLast(s: String) = "$this$s"