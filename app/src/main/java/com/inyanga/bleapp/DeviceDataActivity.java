package com.inyanga.bleapp;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inyanga.bleapp.ble.BleManager;
import com.inyanga.bleapp.ble.ObservableDeviceCallback;

public class DeviceDataActivity extends AppCompatActivity implements ObservableDeviceCallback {

    private TextView deviceRev, softRev;
    private BleManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_data);
        BluetoothDevice device = getIntent().getParcelableExtra(MainActivity.DEVICE);
        TextView deviceName = findViewById(R.id.deviceName);
        TextView deviceAddress = findViewById(R.id.deviceAddress);
        deviceRev = findViewById(R.id.deviceRev);
        softRev = findViewById(R.id.deviceSoft);
        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());
        bleManager = BleManager.getInstance();
        bleManager.setObservableCallback(this);
        bleManager.connectDevice(device, getApplicationContext());
    }

    @Override
    public void onHwRevisionUpdate(final String rev) {
        onUiThreadUpdate(deviceRev, rev);
    }


    private void onUiThreadUpdate(final TextView view, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(value);
            }
        });
    }

    public void vibrate(View view) {
        bleManager.vibrate();
    }

    public void changeTime(View view) {
        bleManager.changeTime();
    }
}
