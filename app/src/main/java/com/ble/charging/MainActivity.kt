package com.ble.charging

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ble.library.demo.BLEDirectConnectActivity
import com.ble.library.demo.BleActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * 测试蓝牙充电
     */
    fun viewBLECharging(view: View) {
        startActivity(Intent(this, BLEDirectConnectActivity::class.java))
    }

    fun viewJumpBle(view: View) {
        startActivity(Intent(this, BleActivity::class.java))
    }
}