package com.inyanga.bleapp;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Pavel Shakhtarin on 08.08.2018.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceHolder> {

    private List<BluetoothDevice> deviceList;

    public DeviceListAdapter(List<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public DeviceListAdapter.DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_view_holder, parent, false);
        return new DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.DeviceHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceType.setText(String.format("%d", device.getType()));
        holder.deviceAddress.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class DeviceHolder extends RecyclerView.ViewHolder {

        TextView deviceName, deviceType, deviceAddress;

        DeviceHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            deviceType = itemView.findViewById(R.id.deviceType);
            deviceAddress = itemView.findViewById(R.id.deviceAddress);
        }
    }
}
