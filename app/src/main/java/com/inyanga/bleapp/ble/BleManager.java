package com.inyanga.bleapp.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.UUID;


/**
 * Created by Pavel Shakhtarin on 07.08.2018.
 */
public class BleManager {

    private final static BleManager instance = new BleManager();

    private final static int SCAN_TIME = 60000;
    private final static UUID DEVICE_INFO_SERVICE_UUID = convertFromInteger(0x180A);
    private final static UUID HARDWARE_REV_CHR_UUID = convertFromInteger(0x2A27);
   // private final static UUID SOFTWARE_REV_CHR_UUID = convertFromInteger(0x2A28);

    private BluetoothAdapter btAdapter;
    private MainUiCallback bleManagerCallback;
    private ObservableDeviceCallback observableDeviceCallback;

    private BleManager() {
    }

    public static BleManager getInstance() {
        return instance;
    }

    public void init(BluetoothAdapter btAdapter, MainUiCallback bleManagerCallback) {
        this.btAdapter = btAdapter;
        this.bleManagerCallback = bleManagerCallback;
    }


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            bleManagerCallback.updateDeviceList(result.getDevice());
            Log.i("********", "Device has been found");
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("********", "Error occurred: " + errorCode);
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTING) {
                Log.i("CONNECTION STATUS: ", "Connecting...");
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("CONNECTION STATUS: ", "Connected");
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService gattService = gatt.getService(DEVICE_INFO_SERVICE_UUID);
            BluetoothGattCharacteristic revChar = gattService.getCharacteristic(HARDWARE_REV_CHR_UUID);

            gatt.readCharacteristic(revChar);


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic.getUuid().equals(HARDWARE_REV_CHR_UUID)) {
                observableDeviceCallback.onHwRevisionUpdate(characteristic.getStringValue(0));
            }
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
                Log.i("********", "Scan complete");
            }
        }, SCAN_TIME);
        btScanner.startScan(scanCallback);
        bleManagerCallback.updateUi(true);
    }

    public void connectDevice(BluetoothDevice device, Context context) {
        device.connectGatt(context, true, gattCallback);
    }

    public void setObservableCallback(ObservableDeviceCallback observableDeviceCallback) {
        this.observableDeviceCallback = observableDeviceCallback;
    }

    public static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }
}
