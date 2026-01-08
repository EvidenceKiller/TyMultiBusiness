package com.ty.bus.multi.core.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙发现
 */
@SuppressLint("MissingPermission")
public class BluetoothDiscover {
    public static final String TAG = BluetoothDiscover.class.getSimpleName();

    private volatile static BluetoothDiscover _instance;

    private BluetoothDiscover() {
    }

    public synchronized static BluetoothDiscover getInstance() {
        if (_instance == null) {
            _instance = new BluetoothDiscover();
        }
        return _instance;
    }

    //  默认扫描时间：30s
    private static final long SCAN_TIME = 30 * 1000;
    private String mAddress;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mTargetDevice;
    private ScanCallback mScanCallback;
    private volatile boolean mScanning;

    private final Handler handler = new Handler();

    public BluetoothDevice getTargetDevice() {
        return mTargetDevice;
    }

    //  开始扫描
    public void startScan(String address, FxyDiscoverCallback callback) {
        startScan(address, SCAN_TIME, callback);
    }

    private final Set<String> tempAddressSet = new HashSet<>();

    public synchronized void startScan(String address, long time, FxyDiscoverCallback callback) {
        if (!isBluetoothEnable()) return;
        if (mScanning) return;

        mAddress = address;

        Log.d(TAG, "mAddress: " + mAddress);

        if (mTargetDevice != null && mTargetDevice.getAddress().equals(mAddress)) {
            Log.d(TAG, "mTargetDevice: " + mTargetDevice.getName() + ",address:" + mTargetDevice.getAddress());
            if (callback != null) {
                callback.onScanResult(mTargetDevice);
            }
            return;
        }

        tempAddressSet.clear();
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (device == null) return;

                int rssi = result.getRssi();
                String name = device.getName();
                String address = device.getAddress();
                //  名字过滤
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) return;
                Log.d(TAG, "扫描到设备: " + name + ",address:" + address + ",rssi:" + rssi);

                //  地址过滤
                if (mAddress != null && !mAddress.equals(address)) return;
                //  信号过滤
                if (rssi < -120) return;

                //  同一地址只返回一个
                if (!tempAddressSet.contains(address)) {
                    tempAddressSet.add(device.getAddress());

                    Log.d(TAG, "设备过滤 >>> " + name + ",address:" + address + ",rssi:" + rssi);
                    if (callback != null && callback.onScanResult(device)) {
                        mTargetDevice = device;
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG, "onScanFailed: " + errorCode);
                if (callback != null) {
                    callback.onScanFailed(errorCode);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(TAG, "onBatchScanResults: " + results.size());
            }
        };

        // scan
        BluetoothLeScanner mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mLeScanner != null) {
            //  设置扫描超时
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> {
                stopScan();
                if (callback != null) {
                    callback.onFinish();
                }
            }, time <= 0 ? SCAN_TIME : time);

            mScanning = true;
            mLeScanner.startScan(mScanCallback);
        }
    }

    //  停止扫描
    //  startLeScan()和stopLeScan()一定要确保成对出现、顺序调用。
    //  否则会导致协议栈中mClientIf达到上限，扫描registerClient失败，再也不能扫描到设备，此时onScanFailed会发生errorCode=2。
    public synchronized void stopScan() {
        if (!mScanning) return;

        mScanning = false;
        BluetoothLeScanner mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mLeScanner != null) {
            mLeScanner.stopScan(mScanCallback);
        }
        mScanCallback = null;
        handler.removeCallbacksAndMessages(null);
    }

    public void clear() {
        mTargetDevice = null;
    }

    //  当前蓝牙是否打开
    private boolean isBluetoothEnable() {
        if (null != mBluetoothAdapter) {
            return mBluetoothAdapter.isEnabled();
        }
        return false;
    }

    public interface FxyDiscoverCallback {
        // 扫描完成回调
        void onFinish();

        //  扫描过程中设备回调
        boolean onScanResult(BluetoothDevice device);

        //  扫描错误
        void onScanFailed(int errorCode);
    }
}
