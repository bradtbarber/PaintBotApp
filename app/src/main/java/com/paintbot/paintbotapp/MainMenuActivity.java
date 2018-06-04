package com.paintbot.paintbotapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainMenuActivity extends AppCompatActivity {

    int distance;
    int i;
    long startTime;

    ListView dataReadout;
    ArrayAdapter<Integer> dataReadoutAdapter;
    ArrayList<Integer> distanceList;
    ArrayList<Integer> durationList;

    String btDeviceAddress;
    BluetoothAdapter btAdapter;
    BluetoothDevice bluetoothDevice;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    startTime = System.currentTimeMillis();
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    connectedThread.start();
                    break;

                case MESSAGE_READ:
                    byte[] readBufferB = (byte[])msg.obj;
                    int readBuffer = readBufferB[0];
                    distance = readBuffer;
                    if (distance < 0) {
                        distance = 0;
                    }
                    updateInterfaceAndDataLists(distance);
                    break;

                default:

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btDeviceAddress = getIntent().getStringExtra("btdevice");
        distanceList = new ArrayList<>();
        durationList = new ArrayList<>();
        dataReadout = (ListView) findViewById(R.id.dataReadout);
        dataReadoutAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, 0);
        dataReadout.setAdapter(dataReadoutAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice = btAdapter.getRemoteDevice(btDeviceAddress);

        i=0;

        ConnectThread connect = new ConnectThread(bluetoothDevice);
        connect.start();
    }


    private void updateInterfaceAndDataLists (int distance){
        if (distance > 0) {
            dataReadoutAdapter.add(distance);
            i++;
            dataReadoutAdapter.notifyDataSetChanged();
            dataReadout.smoothScrollToPosition(i);
        }
        distanceList.add(distance);
        durationList.add((int)(System.currentTimeMillis() - startTime));
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // tmp = temporary object that is later assigned to mmSocket,
            // mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Socket unable to connect", Toast.LENGTH_SHORT).show();
            }
            mmSocket = tmp;
        }

        public void start() {
            btAdapter.cancelDiscovery();
            try {
                // Connect to the device through the socket.
                mmSocket.connect();
            }
            catch (IOException connectException) {
                // Unable to connect; close the socket
                try {
                    mmSocket.close();
                }
                catch (IOException closeException) {
                    Toast.makeText(getApplicationContext(), "Unable to close socket", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }

        // Cancel an in-progress connection, close the socket
        public void cancel() {
            try {
                mmSocket.close();
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Unable to close socket", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Called when device is connected
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        //No need for OutStream, may use later
        //private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            //OutputStream tmpOut = null;

            // Get input/output streams, using temp since streams are final
            try {
                tmpIn = socket.getInputStream();
                //tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "ERROR finding input", Toast.LENGTH_SHORT).show();
            }
            mmInStream = tmpIn;
            //mmOutStream = tmpOut;
        }

        public void start() {
            // buffer to store data from stream.
            byte[] buffer;
            // bytes returned from read()
            int bytes;
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                //for (int i = 0; i < 20; i++){
                try {
                    // Read from the InputStream
                    // Place in buffer, will overwrite previous string.
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity (.sendToTarget)
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
                catch (IOException e)
                {
                    break;
                }
            }
        }

        // Call from the main activity to send data to the remote device
        // No need currently, may use later (?)
        /*public void write(byte[] bytes) {
            //Toast.makeText(getApplicationContext(), "Writting to device", Toast.LENGTH_SHORT).show();
            try {
                mmOutStream.write(bytes);
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Unable to send data", Toast.LENGTH_SHORT).show();
            }
        }*/

        //Call from the main activity to shutdown the connection
        public void cancel() {
            try {
                mmSocket.close();
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Unable to close socket", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
