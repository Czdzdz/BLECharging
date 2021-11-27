package com.ble.library.utils;

import android.annotation.SuppressLint;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.CRC32;

public class ToolsUtils {

    /**
     * 获取精确到秒的低位在前的时间戳
     *
     * @return byteArray
     */
    public static byte[] getSecondTimestamp() {
        String timestamp = String.valueOf(new Date().getTime() / 1000);
        int time = Integer.parseInt(timestamp);
        return intToByte4(time);
    }

    /**
     * 秒变日期
     *
     * @param bytes 低位在前
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    public static String byteToSecondTime(byte[] bytes) {
        int intTime = ByteUtils.byte4ToInt(ByteUtils.byteReverse(bytes));
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis((long) intTime * 1000);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(gc.getTime());
    }

    /**
     * 将int变byte[]数组（低位在前高位在后）
     *
     * @param res int
     * @return byteArray
     */
    public static byte[] intToByte4(int res) {
        byte[] times = new byte[4];
        times[0] = (byte) (res & 0x000000ff);
        times[1] = (byte) ((res & 0x0000ff00) >> 8);
        times[2] = (byte) ((res & 0x00ff0000) >> 16);
        times[3] = (byte) ((res & 0xff000000) >> 24);
        return times;
    }

    /**
     * 将32位的long变byte[]数组（低位在前高位在后）
     * 0xffffffff
     *
     * @param res long
     * @return byteArray
     */
    public static byte[] longToByte4(long res) {
        byte[] times = new byte[4];
        times[0] = (byte) (res & 0x000000ff);
        times[1] = (byte) ((res & 0x0000ff00) >> 8);
        times[2] = (byte) ((res & 0x00ff0000) >> 16);
        times[3] = (byte) ((res & 0xff000000) >> 24);
        return times;
    }

    /**
     * 将byte[]数组转成int (低位在前)
     *
     * @param arr 低位在前
     * @return int
     */
    public static int byte4ToInt(byte[] arr) {
        if (arr == null || arr.length != 4) {
            throw new IllegalArgumentException("byte数组必须不为空,并且是4位!");
        }
        return ((arr[3] & 0xff) << 24) | ((arr[2] & 0xff) << 16) | ((arr[1] & 0xff) << 8) | ((arr[0] & 0xff));
    }

    /**
     * 多个数组合并
     *
     * @param first byte[]
     * @param rest  byte[]
     * @return byte[]
     */
    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * 截取文件块，下标从 1 开始
     *
     * @param file       完整文件
     * @param startIndex 开始下标
     * @param endIndex   结束下标
     * @return byte[]
     */
    public static byte[] getFileBlock(byte[] file, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex > file.length) {
            return new byte[0];
        }
        return Arrays.copyOfRange(file, startIndex - 1, endIndex);
    }

    /**
     * CRC32
     *
     * @param file byte[]
     * @return byte[]
     */
    public static byte[] fileCRC32(byte[] file) {
        CRC32 crc32 = new CRC32();
        crc32.update(file);
        return longToByte4(crc32.getValue());
    }

    /**
     * 适合单个byte转成16进制字符串
     *
     * @param s String
     * @return hex
     */
    public static String byteStringToHex(String s) {
        if (s.length() != 0) {
            int temp = Integer.parseInt(s) >= 0 ? Integer.parseInt(s) : (256 + Integer.parseInt(s));
            String hex = s.length() != 0 ? Integer.toHexString(temp) : "0";
            return " 0x" + (hex.length() == 1 ? "0" + hex.toUpperCase() : hex.toUpperCase());
        } else return "0x00";
    }

    @SuppressLint("DefaultLocale")
    public static String getPercentage(byte[] fileBlocks, int endIndex) {
        int fileLength = fileBlocks.length;
        float percentage = ((float) endIndex) / fileLength;
        percentage = percentage >= 1 ? 1 : percentage;
        return String.format("%.2f", 100 * percentage) + "%";
    }

    /**
     * 校验和
     *
     * @param msg    需要计算校验和的byte数组
     * @param length 校验和位数
     * @return 计算出的校验和数组
     */
    public static byte[] sumCheck(byte[] msg, int length) {
        long mSum = 0;
        byte[] mByte = new byte[length];

        // 逐Byte添加位数和
        for (byte byteMsg : msg) {
            long mNum = ((long) byteMsg >= 0) ? (long) byteMsg : ((long) byteMsg + 256);
            mSum += mNum;
        }

        // 位数和转化为Byte数组
        for (int liv_Count = 0; liv_Count < length; liv_Count++) {
            mByte[length - liv_Count - 1] = (byte) (mSum >> (liv_Count * 8) & 0xff);
        }

        return mByte;
    }
}
