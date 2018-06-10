package com.paintbot.paintbotapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;


public class BluetoothSetupActivity extends Activity implements AdapterView.OnItemClickListener{
    ArrayAdapter<String> deviceListAdapter;
    ListView deviceListView;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevicesSet;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> btDevices;
    IntentFilter filter;

    BroadcastReceiver btReceiver;

    public static String EXTRA_DEVICE = "btDevice";
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_setup);

        initBluetooth();
        getPairedDevices();
        startDiscovery();
    }

    private void startDiscovery() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    private void getPairedDevices(){
        pairedDevicesSet = btAdapter.getBondedDevices();
        if(pairedDevicesSet.size()>0){
            for(BluetoothDevice device : pairedDevicesSet){
                //pairedDevices.add(device.getName());
                btDevices.add(device);
                deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private void initBluetooth() {
        deviceListView = findViewById(R.id.deviceListView);
        deviceListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, 0);
        pairedDevices = new ArrayList<>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        btDevices = new ArrayList<>();

        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(this);

        //Try to get the device's bluetooth adaptor (radio) and enable it
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            //Device does not support bluetooth
            Toast.makeText(getApplicationContext(),
                    "Your device does not support Bluetooth.\n" +
                            "This is required to run this application.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Toast.makeText(getApplicationContext(),
                "Bluetooth ON...", Toast.LENGTH_SHORT).show();

        btReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    btDevices.add(device);
//                    String s = "";
//                    for (int a = 0; a < pairedDevices.size(); a++) {
//                        if (device.getName().equals(pairedDevices.get(a))) {
//                            s = "(Paired)";
//                            break;
//                        }
//                    }
//                    deviceListAdapter.add(device.getName() + " " + s + " " + "\n" + device.getAddress());
                    deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
        registerReceiver(btReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(),"Bluetooth MUST be ENABLED",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//        if(btAdapter.isDiscovering()){
//            btAdapter.cancelDiscovery();
//        }
//        try {
//            if (Objects.requireNonNull(deviceListAdapter.getItem(arg2)).contains("(Paired)")) {
//                Intent sensorRead = new Intent(this, MainMenuActivity.class);
//                sensorRead.putExtra(EXTRA_DEVICE, btDevices.get(arg2));
//                startActivity(sensorRead);
//            } else {
//                Toast.makeText(getApplicationContext(), "Device is not paired.", Toast.LENGTH_SHORT).show();
//            }
//        } catch (NullPointerException e) {
//            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//        }
        BluetoothDevice selectedDevice;
        try {
            selectedDevice = btDevices.get(arg2);
            Intent sensorRead = new Intent(this, SensorReadoutActivity.class);
            sensorRead.putExtra(EXTRA_DEVICE, selectedDevice);
            startActivity(sensorRead);
        } catch (IndexOutOfBoundsException e) {
            System.out.print(e.getMessage());
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        btAdapter.cancelDiscovery();
    }

    @Override
    protected void onStop() {
        super.onStop();
        btAdapter.disable();
        unregisterReceiver(btReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        initBluetooth();
        getPairedDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }
}