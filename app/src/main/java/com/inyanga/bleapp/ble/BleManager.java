package com.inyanga.bleapp.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.UUID;


/**
 * Created by Pavel Shakhtarin on 07.08.2018.
 */
public class BleManager {

    private final static BleManager instance = new BleManager();

    private final static int SCAN_TIME = 60000;
    private final static UUID DEVICE_INFO_SERVICE_UUID = convertFromInteger(0x180A);
    private final static UUID HARDWARE_REV_CHR_UUID = convertFromInteger(0x2A27);
    private final static UUID CURRENT_TIME_SERVICE_UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    private final static UUID CURRENT_TIME_CHAR_UUID = convertFromInteger(0x2A2B);
    private final static UUID ALERT_SRV_UUID = convertFromInteger(0x1811);
    private final static UUID ALERT_CHAR_UUID = convertFromInteger(0x2A46);
    private final static UUID CHARACTERISTIC_NOTIFICATION_CONFIG = convertFromInteger(0x2902);
    private final static byte[] byteTime = new byte[]{1, 2};

    private BluetoothAdapter btAdapter;
    private MainUiCallback bleManagerCallback;
    private ObservableDeviceCallback observableDeviceCallback;
    private BluetoothGattCharacteristic alertChar, timeChar;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService alertService, currentTimeService;

    private BleManager() {
    }

    public static BleManager getInstance() {
        return instance;
    }

    public void init(BluetoothAdapter btAdapter, MainUiCallback bleManagerCallback) {
        this.btAdapter = btAdapter;
        this.bleManagerCallback = bleManagerCallback;
    }

    //----------------------------------------------------------------------------------------------

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

    //----------------------------------------------------------------------------------------------

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
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("GATT: ", "onCharacteristicChanged was fired");
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            alertService = gatt.getService(ALERT_SRV_UUID);
            currentTimeService = gatt.getService(CURRENT_TIME_SERVICE_UUID);
            alertChar = alertService.getCharacteristic(ALERT_CHAR_UUID);
            timeChar = currentTimeService.getCharacteristic(CURRENT_TIME_CHAR_UUID);
            final int characteristicProperties = timeChar.getProperties();
            if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_NOTIFY)) > 0) {
                Log.i("GATT: ", "Notification is available");
                gatt.setCharacteristicNotification(timeChar, true);
                BluetoothGattDescriptor descriptor = timeChar.getDescriptor(CHARACTERISTIC_NOTIFICATION_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
            if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0) {
                Log.i("GATT: ", "WRITE is available");
            }
            alertChar.setValue(byteTime);
            timeChar.setValue(getByteTime());
            BleManager.this.bluetoothGatt = gatt;

            // gatt.readCharacteristic(alertChar);
//            List<BluetoothGattService> gattServices = gatt.getServices();
//            Log.i("GATT SRVS LIST LENGTH: ", String.valueOf(gattServices.size()));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic.getUuid().equals(HARDWARE_REV_CHR_UUID)) {
                observableDeviceCallback.onHwRevisionUpdate(characteristic.getStringValue(0));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("CHAR STATUS", "CHAR CHANGED SUCCESSFULLY ");
            } else {
                Log.i("CHAR STATUS", "SOMETHING WENT WRONG 0x" + Integer.toHexString(status));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("DESCR STATUS", "DECRIPTOR CHANGED SUCCESSFULLY ");

            } else {
                Log.i("DESCR STATUS", "SOMETHING WENT WRONG");
            }

        }
    };

    //----------------------------------------------------------------------------------------------

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
        ScanSettings.Builder scanSetBuilder = new ScanSettings.Builder();
        scanSetBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        btScanner.startScan(null, scanSetBuilder.build(), scanCallback);
        bleManagerCallback.updateUi(true);
    }

    //----------------------------------------------------------------------------------------------

    private byte[] getByteTime() {
        byte year_mso = 0;
        byte year_lso = 0;
        byte month = 0;
        byte day = 0;
        byte hour = 13;
        byte minute = 13;
        byte sec = 0;
        byte weekday = 0;
        byte quitter = 0;
        byte arjson = 1;
        return new byte[]{year_mso, year_lso, month, day, hour, minute, sec, weekday, quitter, arjson};
    }

    //----------------------------------------------------------------------------------------------

    public void vibrate() {
        alertChar = alertService.getCharacteristic(ALERT_CHAR_UUID);
        bluetoothGatt.writeCharacteristic(alertChar);

    }

    //----------------------------------------------------------------------------------------------

    public void changeTime() {
        timeChar = currentTimeService.getCharacteristic(CURRENT_TIME_CHAR_UUID);
        bluetoothGatt.writeCharacteristic(timeChar);
    }

    //----------------------------------------------------------------------------------------------

    public void connectDevice(BluetoothDevice device, Context context) {
        device.connectGatt(context, true, gattCallback);
    }

    //----------------------------------------------------------------------------------------------

    public void setObservableCallback(ObservableDeviceCallback observableDeviceCallback) {
        this.observableDeviceCallback = observableDeviceCallback;
    }

    //----------------------------------------------------------------------------------------------

    public static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }
}
