package com.paintbot.paintbotapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

//import java.util.ArrayList;
import java.util.Set;


public class BluetoothSetupActivity extends AppCompatActivity {
    ArrayAdapter<String> deviceListAdapter;
    ListView deviceListView;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevicesSet;
    //ArrayList<String> pairedDevices;
    //ArrayList<BluetoothDevice> btDevices;
    //IntentFilter filter;

    TextView textView1;

    //BroadcastReceiver btReceiver;

    public static String EXTRA_DEVICE_ADDRESS = "btDeviceAddress";
    //private final static int REQUEST_ENABLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_setup);
    }

    @Override
    public void onResume() {
        super.onResume();

        checkBTStatus();

        textView1 = findViewById(R.id.connecting);
        textView1.setTextSize(30);
        textView1.setText(getResources().getString(R.string.empty_string));

        //Initialize array adaptor for paired devices
        deviceListAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        //Find and set up ListView for paired devices
        deviceListView = findViewById(R.id.paired_devices);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(DeviceClickListener);

        // Get the local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices and append to 'pairedDevices'
        pairedDevicesSet = btAdapter.getBondedDevices();

        //Add previously paired devices to array
        if (pairedDevicesSet.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevicesSet) {
                deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            deviceListAdapter.add(noDevices);
        }
    }

    // Set up on-click listener for the list (nicked this - unsure)
    private OnItemClickListener DeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            textView1.setText(getResources().getString(R.string.connecting));
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
            Intent i = new Intent(BluetoothSetupActivity.this, SensorActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };

    private void checkBTStatus() {
        // Check device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(),
                    getResources().getString(R.string.bluetooth_unsupported),
                    Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.bluetooth_on),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}