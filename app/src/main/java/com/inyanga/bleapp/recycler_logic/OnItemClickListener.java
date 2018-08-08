package com.inyanga.bleapp.recycler_logic;

import android.bluetooth.BluetoothDevice;

public interface OnItemClickListener {
    void onDeviceClick(BluetoothDevice device);
}
