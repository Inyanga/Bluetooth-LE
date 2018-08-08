package com.inyanga.bleapp.ble;

public interface ObservableDeviceCallback {
    void onHwRevisionUpdate(final String rev);
    void onSwRevisionUpdate(final String rev);
}
