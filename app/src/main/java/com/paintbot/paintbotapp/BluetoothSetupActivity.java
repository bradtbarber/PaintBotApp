package com.paintbot.paintbotapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> btDevices;
    IntentFilter filter;
    final String TAG = "BluetoothActivty";


    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!btDevices.contains(device)) {
                    btDevices.add(device);
                    deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
                    deviceListAdapter.notifyDataSetChanged();
                }
            }


            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BR: BOND_BONDED");
                    Intent sensorRead = new Intent(getApplicationContext(), SensorActivity.class);
                    sensorRead.putExtra(EXTRA_DEVICE_ADDRESS, mDevice.getAddress());
                    startActivity(sensorRead);
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BR: BOND_BONDING");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BR: BOND_NONE");
                }
            }
        }
    };

    public static String EXTRA_DEVICE_ADDRESS = "btDevice";
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        pairedDevices = new ArrayList<>();
        btDevices = new ArrayList<>();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        setContentView(R.layout.activity_bluetooth_setup);
        btDevices.addAll(btAdapter.getBondedDevices());
        initBluetooth();
        addBondedDevicesToAdapter();
        startDiscovery();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(btReceiver, filter);

    }

    private void startDiscovery() {
        btAdapter.cancelDiscovery();
        checkLocationPermissions();
        btAdapter.startDiscovery();
    }


    private void initBluetooth() {

        deviceListView = findViewById(R.id.paired_devices);
        deviceListAdapter = new ArrayAdapter<>(this,  R.layout.device_name, 0);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(this);
        //Try to get the device's bluetooth adaptor (radio) and enable it
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


        ;
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

        btAdapter.cancelDiscovery();
        BluetoothDevice selectedDevice;
        selectedDevice = btDevices.get(arg2);
        if (btAdapter.getBondedDevices().contains(selectedDevice)){
            Intent sensorRead = new Intent(getApplicationContext(), SensorActivity.class);
            sensorRead.putExtra(EXTRA_DEVICE_ADDRESS, selectedDevice.getAddress());
            startActivity(sensorRead);
        }
        else {
            selectedDevice.createBond();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btAdapter.cancelDiscovery();
        checkLocationPermissions();
        btAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }

    private void addBondedDevicesToAdapter(){
        for (BluetoothDevice device : btAdapter.getBondedDevices()){

            deviceListAdapter.add(device.getName() + "\n" + device.getAddress());

        }

    }
    private void checkLocationPermissions(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0001);
            }
        }
    }

}