package com.inyanga.bleapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.inyanga.bleapp.ble.BleManager;
import com.inyanga.bleapp.ble.BleManagerCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BleManagerCallback {

    private final static int BT_REQUEST_CODE = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BleManager bleManager;
    private List<BluetoothDevice> deviceList;
    private ProgressBar progressBar;
    private Button scanBtn;
    private RecyclerView recyclerView;
    private DeviceListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBt();
        deviceList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);
        scanBtn = findViewById(R.id.scanBtn);
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new DeviceListAdapter(deviceList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void initBt() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getApplicationContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        } else {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = (bluetoothManager != null) ? bluetoothManager.getAdapter() : null;
            bleManager = new BleManager(bluetoothAdapter, this);
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btEnableIntent, BT_REQUEST_CODE);
            }
        }
    }

    public void startScan(View view) {
        bleManager.scanLeDevice();
    }

    @Override
    public void updateUi(boolean isScaning) {
        if (isScaning) {
            progressBar.setVisibility(View.VISIBLE);
            scanBtn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            scanBtn.setEnabled(true);
        }
    }

    @Override
    public void updateDeviceList(BluetoothDevice device) {
        deviceList.add(device);
        Log.i("********", "Device has been added to the list");
        adapter.notifyDataSetChanged();
    }


}
