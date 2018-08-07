package com.inyanga.bleapp.ble;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by Pavel Shakhtarin on 08.08.2018.
 */
public interface BleManagerCallback {
    void updateUi(boolean isScaning);
    void updateDeviceList(BluetoothDevice device);
}
