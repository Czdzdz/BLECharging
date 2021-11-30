package com.ble.library.demo;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ble.library.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleStatusCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.model.ScanRecord;
import cn.com.heaton.blelibrary.ble.utils.Utils;
import cn.com.heaton.blelibrary.ble.utils.UuidUtils;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class BleActivity extends AppCompatActivity {
    private String TAG = BleActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_LOCATION = 2;
    public static final int REQUEST_PERMISSION_WRITE = 3;
    public static final int REQUEST_GPS = 4;
    private LinearLayout llBlutoothAdapterTip;
    private TextView tvAdapterStates;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private FilterView filterView;
    private ScanAdapter adapter;
    private List<BleDevice> bleRssiDevices;
    private Ble ble;
    private ObjectAnimator animator;
    private boolean isFilter = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Log.e("debug", "打开BleActivity");
        ble = Ble.options()
                .setLogBleEnable(true)//设置是否输出打印蓝牙日志
                .setThrowBleException(true)//设置是否抛出蓝牙异常
                .setLogTAG("AndroidBLE")//设置全局蓝牙操作日志TAG
                .setAutoConnect(false)//设置是否自动连接
                .setIgnoreRepeat(false)//设置是否过滤扫描到的设备(已扫描到的不会再次扫描)
                .setConnectFailedRetryCount(3)//连接异常时（如蓝牙协议栈错误）,重新连接次数
                .setConnectTimeout(10 * 1000)//设置连接超时时长
                .setScanPeriod(3 * 1000)//设置扫描时长
                .setMaxConnectNum(7)//最大连接数量
                .setUuidService(UUID.fromString(UuidUtils.uuid16To128("fd00")))//设置主服务的uuid
                .setUuidWriteCha(UUID.fromString(UuidUtils.uuid16To128("fd01")))//设置可写特征的uuid
                .setUuidReadCha(UUID.fromString(UuidUtils.uuid16To128("fd02")))//设置可读特征的uuid （选填）
                .setUuidNotifyCha(UUID.fromString(UuidUtils.uuid16To128("fd03")))//设置可通知特征的uuid （选填，库中默认已匹配可通知特征的uuid）
                .setBleWrapperCallback(new MyBleWrapperCallback())
                .create(getApplicationContext(), new Ble.InitCallback() {
                    @Override
                    public void success() {
                        BleLog.e("MainApplication", "初始化成功");
                    }

                    @Override
                    public void failed(int failedCode) {
                        BleLog.e("MainApplication", "初始化失败：" + failedCode);
                    }
                });
        initView();
        initAdapter();
        initLinsenter();
        initBleStatus();
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEachCombined(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(permission -> {
            if (permission.granted) {
                checkBlueStatus();
            }
        });

    }

    private void initAdapter() {
        bleRssiDevices = new ArrayList<>();
        adapter = new ScanAdapter(this, bleRssiDevices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.getItemAnimator().setChangeDuration(300);
        recyclerView.getItemAnimator().setMoveDuration(300);
        recyclerView.setAdapter(adapter);
    }

    private void initView() {
        llBlutoothAdapterTip = findViewById(R.id.ll_adapter_tip);
        tvAdapterStates = findViewById(R.id.tv_adapter_states);
        recyclerView = findViewById(R.id.recyclerView);
        floatingActionButton = findViewById(R.id.floatingButton);
        filterView = findViewById(R.id.filterView);
    }

    private void initLinsenter() {
        filterView.init(new FilterView.FilterListener() {
            @Override
            public void onAddressNameChanged(String addressOrName) {
                isFilter = true;
            }

            @Override
            public void onRssiChanged(int rssi) {
                isFilter = true;
            }

            @Override
            public void onCancel() {
                isFilter = false;
            }
        });
        tvAdapterStates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Ble.REQUEST_ENABLE_BT);
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rescan();
            }
        });


    }

    //监听蓝牙开关状态
    private void initBleStatus() {
        ble.setBleStatusCallback(new BleStatusCallback() {
            @Override
            public void onBluetoothStatusChanged(boolean isOn) {
                BleLog.i(TAG, "onBluetoothStatusOn: 蓝牙是否打开>>>>:" + isOn);
                llBlutoothAdapterTip.setVisibility(isOn ? View.GONE : View.VISIBLE);
                if (isOn) {
                    checkGpsStatus();
                } else {
                    if (ble.isScanning()) {
                        ble.stopScan();
                    }
                }
            }
        });
    }

    //检查蓝牙是否支持及打开
    private void checkBlueStatus() {
        if (!ble.isSupportBle(this)) {
            finish();
        }
        if (!ble.isBleEnable()) {
            llBlutoothAdapterTip.setVisibility(View.VISIBLE);
        } else {
            checkGpsStatus();
        }
    }

    private void checkGpsStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Utils.isGpsOpen(BleActivity.this)) {
            new AlertDialog.Builder(BleActivity.this)
                    .setTitle("提示")
                    .setMessage("为了更精确的扫描到Bluetooth LE设备,请打开GPS定位")
                    .setPositiveButton("确定", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, REQUEST_GPS);
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        } else {
            ble.startScan(scanCallback);
        }
    }

    private void rescan() {
        if (ble != null && !ble.isScanning()) {
            bleRssiDevices.clear();
            adapter.notifyDataSetChanged();
            ble.startScan(scanCallback);
        }
    }

    private long startTime;
    private BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            synchronized (ble.getLocker()) {
                if (!TextUtils.isEmpty(device.getBleName())) {
                    Log.e("BLE_LOG", "扫描成功，蓝牙名称：" + device.getBleName());
                    for (int i = 0; i < bleRssiDevices.size(); i++) {
//                        BleDevice rssiDevice = bleRssiDevices.get(i);
//                        if (TextUtils.equals(rssiDevice.getBleAddress(), device.getBleAddress())) {
//                            if (rssiDevice.getRssi() != rssi && System.currentTimeMillis() - rssiDevice.getRssiUpdateTime() > 1000L) {
//                                rssiDevice.setRssiUpdateTime(System.currentTimeMillis());
//                                rssiDevice.setRssi(rssi);
//                                adapter.notifyItemChanged(i);
//                            }
//                            return;
//                        }
                    }
                    device.setScanRecord(ScanRecord.parseFromBytes(scanRecord));
//                    device.setRssi(rssi);
                    bleRssiDevices.add(device);
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            startBannerLoadingAnim();
            startTime = System.currentTimeMillis();
            Log.e("BLE_LOG", "开始扫描");
        }

        @Override
        public void onStop() {
            super.onStop();
            stopBannerLoadingAnim();
            long endTime = System.currentTimeMillis() - startTime;
            Log.e("BLE_LOG", "停止扫描:" + endTime);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: " + errorCode);
        }
    };

    public void startBannerLoadingAnim() {
        floatingActionButton.setImageResource(R.drawable.ic_loading);
        animator = ObjectAnimator.ofFloat(floatingActionButton, "rotation", 0, 360);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(800);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public void stopBannerLoadingAnim() {
        floatingActionButton.setImageResource(R.drawable.ic_bluetooth_audio_black_24dp);
        animator.cancel();
        floatingActionButton.setRotation(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == Ble.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else if (requestCode == Ble.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            ble.startScan(scanCallback);
        } else if (requestCode == REQUEST_GPS) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
