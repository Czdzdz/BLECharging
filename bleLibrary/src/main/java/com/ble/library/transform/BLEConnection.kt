package com.ble.library.transform

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.location.LocationManager
import android.os.Build
import cn.com.heaton.blelibrary.ble.Ble
import cn.com.heaton.blelibrary.ble.BleLog
import cn.com.heaton.blelibrary.ble.BleStates
import cn.com.heaton.blelibrary.ble.callback.*
import cn.com.heaton.blelibrary.ble.model.BleDevice
import cn.com.heaton.blelibrary.ble.model.EntityData
import cn.com.heaton.blelibrary.ble.utils.ByteUtils
import cn.com.heaton.blelibrary.ble.utils.Utils
import cn.com.heaton.blelibrary.ble.utils.UuidUtils
import com.ble.library.service.ext.showToast
import java.security.Permissions
import java.util.*

class BLEConnection {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: BLEConnection? = null
            get() {
                if (field == null) {

                    field = BLEConnection()
                }
                return field
            }

        fun get(): BLEConnection {
            return instance!!
        }
    }

    private var mBle: Ble<BleDevice>? = null
    private val gattServices: MutableList<BluetoothGattService> = mutableListOf()

    /**
     * 初始化蓝牙启动参数
     */
    fun initBleConnection(context: Context) {
        mBle = Ble.options()
            .setLogBleEnable(true)//设置是否输出打印蓝牙日志
            .setThrowBleException(true)//设置是否抛出蓝牙异常
            .setLogTAG("BLE")//设置全局蓝牙操作日志TAG
            .setAutoConnect(false)//设置是否自动连接
            .setIgnoreRepeat(false)//设置是否过滤扫描到的设备(已扫描到的不会再次扫描)
            .setConnectFailedRetryCount(3)//连接异常时（如蓝牙协议栈错误）,重新连接次数
            .setConnectTimeout(10 * 1000)//设置连接超时时长
            .setScanPeriod(12 * 1000)//设置扫描时长
            .setMaxConnectNum(7)//最大连接数量
            .setUuidService(UUID.fromString(UuidUtils.uuid16To128("ffe0")))//设置主服务的uuid
            .setUuidWriteCha(UUID.fromString(UuidUtils.uuid16To128("ffe1")))//设置可写特征的uuid
//            .setUuidReadCha(UUID.fromString(UuidUtils.uuid16To128("ffe1")))//设置可读特征的uuid （选填）
//            .setUuidNotifyCha(UUID.fromString(UuidUtils.uuid16To128("ffe1")))//设置可通知特征的uuid （选填，库中默认已匹配可通知特征的uuid）
//            .setFactory(object :BleFactory<BleDevice>(){
//                override fun create(address: String?, name: String?): BleDevice {
//                    return BleRssiDevice(address,name)  //自定义BleDevice的子类
//                }
//            })
//            .setBleWrapperCallback(MyBleWrapperCallback())
            .create(context.applicationContext, object : Ble.InitCallback {
                override fun success() {
                    BleLog.e("BleInit", "初始化成功")
                }

                override fun failed(failedCode: Int) {
                    BleLog.e("BleInit", "初始化失败：${getBleStatesCodeDesc(failedCode)}")
                }
            })
    }

    /**
     * 启动设备连接
     */
    fun startScanBLEDevice(
        context: Context,
        targetDeviceName: String,
        onMatchDeviceSuccess: (BleDevice) -> Unit
    ) {

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            context.showToast("请先打开蓝牙")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Utils.isGpsOpen(context)) {
            context.showToast("当前手机扫描蓝牙需要打开定位功能")
            return
        }

        if (mBle?.isScanning == true) {
            context.showToast("正在扫描中...")
            return
        }


        mBle?.let { ble ->
            ble.startScan(object : BleScanCallback<BleDevice>() {

                override fun onStart() {
                    super.onStart()
                    context.showToast("扫描中...")
                    BleLog.e("startScan", "onStart = 扫描开始")
                }

                override fun onStop() {
                    super.onStop()
                    BleLog.e("startScan", "onStop = 扫描结束")
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    BleLog.e(
                        "startScan",
                        "onScanFailed = 扫描失败,errorCode:${getBleStatesCodeDesc(errorCode)}"
                    )
                }

                override fun onLeScan(device: BleDevice?, rssi: Int, scanRecord: ByteArray?) {

                    if (device != null) {
                        BleLog.e("startScan", "onLeScan = 发现设备 -----> ${device.bleName}")
                    }

                    // 过滤并连接目标设备
                    if (device != null && device.bleName != null && device.bleName.isNotEmpty() && device.bleName == targetDeviceName) {
                        // 停止扫描
                        ble.stopScan()
                        // 连接设备
                        onMatchDeviceSuccess(device)
                    }
                }
            })
        }
    }

    /**
     * 连接蓝牙设备
     */
    fun startConnectBLEDevice(
        device: BleDevice,
        onBLEConnectFail: (String) -> Unit,
        onBLEConnected: (BleDevice?) -> Unit,
    ) {
        if (mBle?.isDeviceBusy(device) == true) {
            onBLEConnectFail("设备忙碌中...")
            return
        }

        mBle?.connect(device, object : BleConnectCallback<BleDevice>() {

            override fun onConnectionChanged(device: BleDevice?) {
                when (device?.connectionState) {
                    BleDevice.DISCONNECT -> {
                    }
                    BleDevice.CONNECTING -> {
                        BleLog.e("onConnectionChanged", "连接中...")
                    }
                    BleDevice.CONNECTED -> {
                        onBLEConnected(device)
                        BleLog.e("onConnectionChanged", "连接成功")
                    }
                }
            }

            override fun onConnectCancel(device: BleDevice?) {
                super.onConnectCancel(device)
                BleLog.e("onConnectCancel", "${device?.bleName}连接取消")
            }

            override fun onServicesDiscovered(device: BleDevice?, gatt: BluetoothGatt?) {
                super.onServicesDiscovered(device, gatt)

                gatt?.let {
                    gattServices.addAll(gatt.services)
                }

                BleLog.e("onServicesDiscovered", "${device?.bleName}发现服务")
            }

            override fun onConnectFailed(device: BleDevice?, errorCode: Int) {
                super.onConnectFailed(device, errorCode)
                onBLEConnectFail("连接失败")
                BleLog.e("onConnectFailed", "${device?.bleName}连接失败")
            }

            override fun onReady(device: BleDevice?) {
                super.onReady(device)
                BleLog.e("onReady", "${device?.bleName}准备完成")
            }
        })
    }

    /**
     * 断开设备连接
     */
    fun disconnectBLEDevice(device: BleDevice, onBLEDisConnected: () -> Unit) {
        mBle?.disconnect(device, object : BleConnectCallback<BleDevice>() {
            override fun onConnectionChanged(device: BleDevice?) {
                if (device?.connectionState == BleDevice.DISCONNECT) {
                    onBLEDisConnected()
                    BleLog.e("onConnectionChanged", "断开连接成功")
                }
            }
        })
    }

    /**
     * 断开所有连接
     */
    fun destroyConnect() {
        mBle?.disconnectAll()
        mBle?.released()
    }

    /**
     * 获取当前设备连接状态
     * @param bleDevice bleDevice
     * @return boolean
     */
    fun getBLEConnectState(bleDevice: BleDevice?): Boolean? {
        return bleDevice?.isConnected
    }

    /**
     * 配置通知 仅可开启一次,不能开关切换
     */
    fun setEnableNotify(
        device: BleDevice?,
        enable: Boolean,
        notifyReceive: (String) -> Unit,
        onNotifyStatusCallback: (String) -> Unit
    ) {
        mBle?.enableNotify(device, enable, object : BleNotifyCallback<BleDevice>() {
            override fun onChanged(
                device: BleDevice?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                BleLog.e("setEnableNotify", "onChanged==uuid:${characteristic?.uuid}")
                BleLog.e(
                    " setEnableNotify",
                    "onChanged==data:${ByteUtils.toHexString(characteristic?.value)}"
                )

                notifyReceive(ByteUtils.toHexString(characteristic?.value))
            }

            override fun onNotifySuccess(device: BleDevice?) {
                super.onNotifySuccess(device)
                onNotifyStatusCallback("Notify Success")
                BleLog.e("setEnableNotify", "onNotifySuccess: ${device?.bleName}")
            }

            override fun onNotifyCanceled(device: BleDevice?) {
                super.onNotifyCanceled(device)
                onNotifyStatusCallback("Notify Canceled")
                BleLog.e("setEnableNotify", "onNotifyCanceled: ${device?.bleName}")
            }

            override fun onNotifyFailed(device: BleDevice?, failedCode: Int) {
                super.onNotifyFailed(device, failedCode)
                onNotifyStatusCallback("Notify Failed")
                BleLog.e(
                    "setEnableNotify", "onNotifyFailed: ${device?.bleName} 开启通知失败,${
                        getBleStatesCodeDesc(
                            failedCode
                        )
                    }"
                )
            }
        })
    }

    /**
     * 接收数据推送
     *
     * @param bleDevice      bleDevice
     * @param onNotifyResultCallback 数据接收回调接口
     */
    fun startNotify(bleDevice: BleDevice?, onNotifyResultCallback: (ByteArray?) -> Unit) {

        mBle?.startNotify(bleDevice, object : BleNotifyCallback<BleDevice>() {

            /**
             *  取消通知
             */
            override fun onNotifyCanceled(device: BleDevice?) {
                super.onNotifyCanceled(device)
                BleLog.e("startNotify", "onNotifyCanceled = ${device?.bleName} 取消通知")
            }

            /**
             * 接收通知失败
             */
            override fun onNotifyFailed(device: BleDevice?, failedCode: Int) {
                super.onNotifyFailed(device, failedCode)
                BleLog.e(
                    "startNotify",
                    "onNotifyFailed = ${device?.bleName} 接收通知失败,failedCode:${
                        getBleStatesCodeDesc(failedCode)
                    }"
                )
            }

            override fun onNotifySuccess(device: BleDevice?) {
                super.onNotifySuccess(device)
                BleLog.e("startNotify", "onNotifySuccess = ${device?.bleName} 设置通知成功")
            }

            /**
             * 接收数据
             */
            override fun onChanged(
                device: BleDevice?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                val data = characteristic?.value
                // 数据接口外放
                onNotifyResultCallback(data)
                BleLog.e(
                    "startNotify",
                    "onChanged = ${device?.bleName}收到硬件数据 >>> ${ByteUtils.toHexString(data)}"
                )
            }
        })
    }

    /**
     *  取消通知
     */
    fun cancelNotify(device: BleDevice?) {
        mBle?.cancelNotify(device, object : BleNotifyCallback<BleDevice>() {

            override fun onNotifyCanceled(device: BleDevice?) {
                super.onNotifyCanceled(device)
                BleLog.e("cancelNotify", "onNotifyCanceled = ${device?.bleName} 取消通知")
            }

            override fun onChanged(
                device: BleDevice?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                val data = characteristic?.value
                BleLog.e(
                    "cancelNotify",
                    "onChanged = ${device?.bleName}收到硬件数据 >>> ${ByteUtils.toHexString(data)}"
                )
            }
        })
    }

    /**
     * 读取数据
     * @param device      bleDevice
     * @param onReadDataCallBack 数据接收回调接口
     */
    fun readData(device: BleDevice?, onReadDataCallBack: (ByteArray?) -> Unit) {
        mBle?.read(device, object : BleReadCallback<BleDevice>() {
            override fun onReadFailed(device: BleDevice?, failedCode: Int) {
                super.onReadFailed(device, failedCode)
                BleLog.e(
                    "readData",
                    "onReadFailed = ${device?.bleName} 读取数据失败,failedCode:${
                        getBleStatesCodeDesc(failedCode)
                    }"
                )
            }

            override fun onReadSuccess(
                dedvice: BleDevice?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onReadSuccess(dedvice, characteristic)

                val data = characteristic?.value
                // 数据接口外放
                onReadDataCallBack(data)
                BleLog.e(
                    "readData",
                    "onReadSuccess = ${device?.bleName} 收到硬件数据 >>> ${ByteUtils.toHexString(data)}"
                )
            }
        })
    }

    /**
     * 获取连接中的设备 [neta-桩码]
     *
     * @return BleDevice
     */
    fun getConnectedBLEDevice(): BleDevice? {
        mBle?.let { ble ->
            for (device in ble.connectedDevices) {
                if (device.bleName.contains("HC-08")) {
                    return device
                }
            }
        }
        return null
    }

    /**
     * 推送数据包 ---> 无回调有进度
     *
     * @param bytes     byteArray
     */
    fun sendEntityData(bytes: ByteArray?) {

        mBle?.let { ble ->
            // TODO payload init
            val payload = bytes
            val entityData = EntityData.Builder().apply {
                address = ble.connectedDevices[0].bleAddress
                data = payload
                delay = 50
            }.build()

            ble.writeEntity(entityData, object : BleWriteEntityCallback<BleDevice>() {
                override fun onWriteSuccess() {
                    BleLog.e("writeEntity", "onWriteSuccess")
                }

                override fun onWriteFailed() {
                    BleLog.e("writeEntity", "onWriteFailed")
                }

                override fun onWriteProgress(progress: Double) {
                    BleLog.e("writeEntity", "当前发送进度: ${(progress * 100).toInt()}%")
                }

                override fun onWriteCancel() {
                    BleLog.e("writeEntity", "取消发送")
                }
            })
        }
    }

    /**
     *  写入数据 结果回调
     *  @param device   BleDevice
     *  @param data     ByteArray数据包
     */
    fun sendDataWithCallback(device: BleDevice?, data: ByteArray?) {
        mBle?.write(device, data, object : BleWriteCallback<BleDevice>() {
            override fun onWriteSuccess(
                device: BleDevice?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                BleLog.e(
                    "onWriteSuccess",
                    "${device?.bleName} 写入数据成功,${ByteUtils.toHexString(characteristic?.value)}"
                )
            }

            override fun onWriteFailed(device: BleDevice?, failedCode: Int) {
                super.onWriteFailed(device, failedCode)
                BleLog.e(
                    "onWriteFailed",
                    "${device?.bleName} 写入数据失败,${getBleStatesCodeDesc(failedCode)}"
                )
            }
        })
    }

    /**
     * 获取蓝牙错误码描述
     */
    private fun getBleStatesCodeDesc(errorCode: Int): String {
        return when (errorCode) {
            BleStates.NotInit -> {
                "NotInit = 2000"
            }
            BleStates.InitAlready -> {
                "InitAlready = 2001"
            }
            BleStates.NotSupportBLE -> {
                "NotSupportBLE = 2005"
            }
            BleStates.BluetoothNotOpen -> {
                "BluetoothNotOpen = 2006"
            }
            BleStates.NotAvailable -> {
                "NotAvailable = 2007"
            }
            BleStates.BlePermissionError -> {
                "BlePermissionError = 2008"
            }
            BleStates.NotFindDevice -> {
                "NotFindDevice = 2009"
            }
            BleStates.InvalidAddress -> {
                "InvalidAddress = 2010"
            }
            BleStates.ScanAlready -> {
                "ScanAlready = 2020"
            }
            BleStates.ScanStopAlready -> {
                "ScanStopAlready = 2021"
            }
            BleStates.ScanFrequentlyError -> {
                "ScanFrequentlyError = 2022"
            }
            BleStates.ScanError -> {
                "ScanError = 2023"
            }
            BleStates.ConnectedAlready -> {
                "ConnectedAlready = 2030"
            }
            BleStates.ConnectFailed -> {
                "ConnectFailed = 2031"
            }
            BleStates.ConnectError -> {
                "ConnectError = 2032"
            }
            BleStates.ConnectException -> {
                " ConnectException = 2033"
            }
            BleStates.ConnectTimeOut -> {
                "ConnectTimeOut = 2034"
            }
            BleStates.MaxConnectNumException -> {
                "MaxConnectNumException = 2035"
            }
            BleStates.NoService -> {
                "NoService = 2040"
            }
            BleStates.DeviceNull -> {
                "DeviceNull = 2041"
            }
            BleStates.NotInitUuid -> {
                "NotInitUuid = 2045"
            }
            BleStates.CharaUuidNull -> {
                "CharaUuidNull = 2050"
            }
            else -> "$errorCode"
        }
    }
}