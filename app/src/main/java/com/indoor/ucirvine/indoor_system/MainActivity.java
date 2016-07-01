package com.indoor.ucirvine.indoor_system;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.indoor.ucirvine.indoor_system.view.Adapter_Rssi;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.indoor.ucirvine.indoor_system.MainActivity.ByteArrayToString;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;

    ListView listview;
    Adapter_Rssi adapter;
    TextView device1;
    TextView device2;
    TextView device3;

    TextView device1_distance;
    TextView device2_distance;
    TextView device3_distance;


    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        device1 = (TextView)findViewById(R.id.device1);
        device2 = (TextView)findViewById(R.id.device2);
        device3 = (TextView)findViewById(R.id.device3);

        device1_distance  = (TextView)findViewById(R.id.device1_distance);
        device2_distance  = (TextView)findViewById(R.id.device2_distance);
        device3_distance  = (TextView)findViewById(R.id.device3_distance);

        adapter = new Adapter_Rssi();
        listview = (ListView) findViewById(R.id.item_list);
        listview.setAdapter(adapter);


        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();
                            byte txpw = scanRecord[29];
                            scanRecord.toString();
                            double d = 0;

                            if(-60 < rssi && rssi < -40){
                                d = ( (3*Math.pow(10,8))  / (4*3.14*2.4*Math.pow(10,9)) ) * Math.pow(10,0);
                            }
                            else if(-40 < rssi){
                                d = ( (3*Math.pow(10,8))  / (4*3.14*2.4*Math.pow(10,9)) ) * Math.pow(10,(rssi+40)/20);
                            }
                            else if(-60 > rssi){
                                d = ( (3*Math.pow(10,8))  / (4*3.14*2.4*Math.pow(10,9)) ) * Math.pow(10,-0.5);
                            }

                            if (device.getAddress().equals("B8:27:EB:A6:A1:E9")){
                                device1.setText(""+rssi);
                                device1_distance.setText("  "+(int)txpw + " " + scanRecord + " " + d);

                            }
//
//                            if (device.getAddress().equals("B8:27:EB:26:28:F4")){
//                                device2.setText(""+rssi);
//                                device2_distance.setText("  "+(int)txpw);
//                            }
                            //test
                            if (device.getAddress().equals("B8:27:EB:3A:91:F4")){
                                device2.setText(""+rssi);
                                device2_distance.setText("  "+(int)txpw + " " + scanRecord[29]  + " " + d);
                            }

                            if (device.getAddress().equals("B8:27:EB:25:31:D6")){
                                device3.setText(""+rssi);
                                device3_distance.setText("  "+(int)txpw  + " " + d);
                            }

                            Log.e("rssi","rssi = " +" name : " + device.getName() + " address" + device.getAddress() + " " + rssi + "time " + ts + "txpw " + txpw + " " + scanRecord);
                            Log.e("scanRecord","scanRecord = " + scanRecord.toString());
                            printScanRecord(scanRecord);
                            adapter.addItem(device.getName(),device.getAddress(), ""+rssi ,ts );
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };
    public String openFileToString(byte[] _bytes)
    {
        String file_string = "";

        for(int i = 0; i < _bytes.length; i++)
        {
            file_string += (char)_bytes[i];
        }

        return file_string;
    }

    public void printScanRecord (byte[] scanRecord) {

        // Simply print all raw bytes
        try {
            String decodedRecord = new String(scanRecord,"UTF-8");
            Log.d("DEBUG","decoded String : " + ByteArrayToString(scanRecord));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Parse data bytes into individual records
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);


        // Print individual records
        if (records.size() == 0) {
            Log.i("DEBUG", "Scan Record Empty");
        } else {
            Log.i("DEBUG", "Scan Record: " + TextUtils.join(",", records));
        }

    }

    public static String ByteArrayToString(byte[] ba)
    {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }

    public static class AdRecord {

        public AdRecord(int length, int type, byte[] data) {
            String decodedRecord = "";
            try {
                decodedRecord = new String(data,"UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.d("DEBUG", "Length: " + length + " Type : " + type + " Data : " + ByteArrayToString(data));
        }

        // ...

        public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
            List<AdRecord> records = new ArrayList<AdRecord>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];
                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];
                //Done if our record isn't a valid type
                if (type == 0) break;

                byte[] data = Arrays.copyOfRange(scanRecord, index+1, index+length);

                records.add(new AdRecord(length, type, data));
                //Advance
                index += length;
            }

            return records;
        }
    }
}

