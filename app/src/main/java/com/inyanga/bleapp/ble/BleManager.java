package com.inyanga.bleapp.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Pavel Shakhtarin on 07.08.2018.
 */
public class BleManager {

    private final static int SCAN_TIME = 60000;

    private BluetoothAdapter btAdapter;
    private BleManagerCallback bleManagerCallback;
//    private List<BluetoothDevice> deviceList;


    public BleManager(BluetoothAdapter btAdapter, BleManagerCallback bleManagerCallback) {
        this.btAdapter = btAdapter;
        this.bleManagerCallback = bleManagerCallback;
    }



    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            deviceList.add(result.getDevice());
            bleManagerCallback.updateDeviceList(result.getDevice());
            Log.i("********", "Device has been found");
            Log.i("********", result.getDevice().getName());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i("********", "Some errror occured");
        }
    };

    public void scanLeDevice() {
        final BluetoothLeScanner btScanner = btAdapter.getBluetoothLeScanner();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(scanCallback);
                bleManagerCallback.updateUi(false);
                Log.i("********", "Scaning complite");
            }
        }, SCAN_TIME);
        btScanner.startScan(scanCallback);
        bleManagerCallback.updateUi(true);
    }
}
