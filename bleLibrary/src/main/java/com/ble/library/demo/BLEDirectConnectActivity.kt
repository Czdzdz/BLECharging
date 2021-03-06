package com.ble.library.demo

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.com.heaton.blelibrary.ble.model.BleDevice
import cn.com.heaton.blelibrary.ble.utils.ByteUtils
import com.ble.library.R
import com.ble.library.service.ServiceFactory
import com.ble.library.service.ext.showToast
import com.ble.library.service.feature.StartChargeData
import com.ble.library.service.feature.StartLoginData
import com.ble.library.service.feature.StopChargeData
import com.ble.library.transform.BLEConnection
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions

/**
 * 蓝牙连接指定设备通讯
 */
class BLEDirectConnectActivity : AppCompatActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        val bleConn = BLEConnection.get()

        const val targetDeviceName = "HC-08"    //目标设备名称
        const val orderId: String = "123456789011"  //测试订单号

    }

    private var bleDevice: BleDevice? = null
    private var mEdtSimulated: EditText? = null
    private var tvNotifyEnable: TextView? = null

    private var lineNum = 0 //行数标识
    private var dataReceiver: StringBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_direct_connect)

        // 初始化蓝牙配置
        bleConn.initBleConnection(application)

        tvNotifyEnable = findViewById(R.id.tvNotifyEnable)
        mEdtSimulated = findViewById(R.id.mEdtSimulated)

        // 申请蓝牙权限
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .subscribe { permission: Permission ->
                if (permission.granted) {

                }
            }
    }

    fun viewBleSimulated(view: View) {
        if (bleDevice == null || bleDevice?.isDisconnected == true || bleDevice?.isConnecting == true) {
            showToast("设备未连接!!!")
            return
        }

        val simulated = mEdtSimulated?.text.toString()
        if (simulated.isEmpty()) {
            showToast("未编辑数据源")
            return
        }

        if (bleDevice != null && bleDevice?.isConnected == true) {
            bleConn.sendDataWithCallback(bleDevice, simulated.toByteArray())
//            bleConn.sendEntityData(simulated.toByteArray())
        }
    }

    /**
     * 连接到指定设备
     */
    fun viewDirectConnect(view: View) {

        if (bleDevice != null && bleDevice?.isConnecting == true) {
            showToast("设备连接中...")
            return
        }

        if (bleDevice != null && bleDevice?.isConnected == true) {
            showToast("设备已连接!!!")
            return
        }

        bleConn.startScanBLEDevice(
            this,
            targetDeviceName
        ) { bleDevice ->
            startConnect(bleDevice)
        }
    }

    /**
     * 开始连接设备
     */
    private fun startConnect(device: BleDevice) {
        bleConn.startConnectBLEDevice(
            device,
            onBLEConnectFail = { message ->
                showToast(message)
            },
            onBLEConnected = { connectedDevice ->
                bleDevice = connectedDevice
                showToast("${connectedDevice?.bleName},连接成功")
            }
        )
    }

    /**
     * 断开连接设备
     */
    fun viewDisconnect(view: View) {

        if (validateDeviceEnable()) return

        bleDevice?.let { device ->
            bleConn.disconnectBLEDevice(
                device,
                onBLEDisConnected = {
                    showToast("连接断开成功")
                })
        }
    }

    /**
     * 配置通知开关
     */
    @SuppressLint("SetTextI18n")
    fun viewBleNotifyEnable(view: View) {

        if (validateDeviceEnable()) return

        val notifyFalse = resources.getString(R.string.string_ble_notify_enable_false)
        val notifyTrue = resources.getString(R.string.string_ble_notify_enable_true)
        tvNotifyEnable?.let { textview ->

            val text = textview.text.toString()
            if (text == notifyFalse) {
                bleConn.startNotify(bleDevice, onNotifyResultCallback = { data ->
                    // 接收处理通知消息
                    dataReceiver.append("\n").append("${++lineNum}")
                        .append(" ----->\n      ${ByteUtils.bytes2HexStr(data)}")
                    showBottomDialog(dataReceiver.toString())
                })
                textview.text = notifyTrue
            } else if (text == notifyTrue) {
                bleConn.cancelNotify(bleDevice)
                textview.text = notifyFalse
            }
        }
    }

    /**
     * 切换通知开关
     */
    private fun changeEnableNotify(enable: Boolean) {
        bleConn.setEnableNotify(
            bleDevice,
            enable,
            notifyReceive = { data ->
                // 接收处理通知消息
                if (enable) {
                    dataReceiver.append("\n").append("${++lineNum}").append(" ----->\n      $data")
                    showBottomDialog(dataReceiver.toString())
                }
            },
            onNotifyStatusCallback = { status ->
                runOnUiThread {
                    showToast(status)
                }
            })
    }

    private var bottomDialog: BottomDialog? = null

    private fun showBottomDialog(content: String) {
        if (bottomDialog == null) {
            bottomDialog = BottomDialog()
            bottomDialog!!.show(supportFragmentManager, "display")
            bottomDialog!!.addDialogCloseListener(object : BottomDialog.OnDialogCloseListener {
                override fun onCloseListener() {
                    // 重置数据
                    lineNum = 0
                    dataReceiver.clear()
                    bottomDialog = null
                }
            })
            val bundle = Bundle()
            bundle.putString("content", content)
            bottomDialog?.arguments = bundle
        } else {
            bottomDialog?.setNewData(content)
        }
    }

    /**
     *  设备登录
     */
    fun viewDeviceLogin(view: View) {
        if (bleDevice == null) {
            showToast("找不到设备!!!")
            return
        }

        if (bleConn.getBLEConnectState(bleDevice) == false) {
            showToast("设备未连接!!!")
            return
        }
        ServiceFactory.get().executeDown(StartLoginData(ble_key = "123456"))
    }

    /**
     * 开始充电
     */
    fun viewStartCharging(view: View) {
        if (validateDeviceEnable()) return
        ServiceFactory.get().executeDown(StartChargeData(orderId = orderId))
    }

    /**
     * 停止充电
     */
    fun viewStopCharging(view: View) {
        if (validateDeviceEnable()) return
        ServiceFactory.get().executeDown(StopChargeData(orderId = orderId))
    }

    /**
     * 查询历史订单
     */
    fun viewQueryOrders(view: View) {
        if (validateDeviceEnable()) return
    }

    /**
     * 查询充电状态
     */
    fun viewChargingStatus(view: View) {
        if (validateDeviceEnable()) return
    }

    override fun onDestroy() {
        super.onDestroy()
        bleConn.destroyConnect()
    }

    /**
     * 验证设备可用性
     */
    private fun validateDeviceEnable(): Boolean {
        if (bleDevice == null) {
            showToast("设备未连接!!!")
            return true
        }

        if (bleDevice != null && bleDevice?.isDisconnected == true) {
            showToast("设备已断开!!!")
            return true
        }
        return false
    }
}