package com.indoor.ucirvine.indoor_system;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.indoor.ucirvine.indoor_system.view.Adapter_Rssi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

    Button save_button;

    private Handler mHandler;
    private Runnable mRunnable;

    double avg_d2;
    int c_d2;

    double avg_d3;
    int c_d3;

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

        save_button = (Button)findViewById(R.id.save_button);

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
//        mBluetoothAdapter.

        avg_d2 = 0;
        c_d2 = 0;

        avg_d3 = 0;
        c_d3 = 0;



        mRunnable = new Runnable() {
            @Override
            public void run() {
                String dirPath = "/sdcard";

                File file = new File(dirPath);

                int rssi_device1 = Adapter_Rssi.device_1.size();        //버튼 클릭시 사이즈 픽스
                int rssi_device2 = Adapter_Rssi.device_2.size();
                int rssi_device3 = Adapter_Rssi.device_3.size();
                int rssi_device4 = Adapter_Rssi.device_4.size();


                // 일치하는 폴더가 없으면 생성
                if( !file.exists() ) {
                    file.mkdirs();
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }

                String testStr = "";
                // txt 파일 생성
                for(int i = 0 ; i < rssi_device1; i ++){
                    testStr += Adapter_Rssi.device_1.get(i).getDeviceAddress() + " " + Adapter_Rssi.device_1.get(i).getRssi() + " " + Adapter_Rssi.device_1.get(i).getDistance()+ " "  + Adapter_Rssi.device_1.get(i).getTimeStamp()+ "" ;
                }

                testStr +="[____________";

                for(int i = 0 ; i < rssi_device2; i ++){
                    testStr += Adapter_Rssi.device_2.get(i).getDeviceAddress() + " " + Adapter_Rssi.device_2.get(i).getRssi() + " " + Adapter_Rssi.device_2.get(i).getDistance() + " " + Adapter_Rssi.device_2.get(i).getTimeStamp()+ "]" ;
                }

                testStr +="[____________";

                for(int i = 0 ; i < rssi_device3; i ++){
                    testStr += Adapter_Rssi.device_3.get(i).getDeviceAddress() + " " + Adapter_Rssi.device_3.get(i).getRssi() + " " + Adapter_Rssi.device_3.get(i).getDistance()+ " "  + Adapter_Rssi.device_3.get(i).getTimeStamp()+ "]" ;
                }

                testStr +="[____________";

                for(int i = 0 ; i < rssi_device4; i ++){
                    testStr += Adapter_Rssi.device_4.get(i).getDeviceAddress() + " " + Adapter_Rssi.device_4.get(i).getRssi() + " " + Adapter_Rssi.device_4.get(i).getDistance()+ " "  + Adapter_Rssi.device_4.get(i).getTimeStamp()+ "]" ;
                }


                int num = 0; //txt number.

                File savefile = new File(dirPath+"/"+num+ ".txt");
                while( savefile.exists() )
                {
                    num++;
                    savefile = new File(dirPath+"/"+num+ ".txt");
                    Log.e("test","test"+num);
                }

                try{
                    FileOutputStream fos = new FileOutputStream(savefile);
                    fos.write(testStr.getBytes());
                    fos.close();
                    int dataNum = 0;
                    if(rssi_device1 != 0)
                        dataNum = rssi_device1;
                    else if(rssi_device2 != 0)
                        dataNum = rssi_device2;
                    else if(rssi_device3 != 0)
                        dataNum = rssi_device3;
                    else if(rssi_device4 != 0)
                        dataNum = rssi_device4;
                    else
                        dataNum=-1;

                    Toast.makeText(MainActivity.this, "Save Success"+dirPath+"/Data Num="+dataNum, Toast.LENGTH_SHORT).show();



                    //초기화
                    Adapter_Rssi.device_1 = new ArrayList<rssiData>();
                    Adapter_Rssi.device_2 = new ArrayList<rssiData>();
                    Adapter_Rssi.device_3 = new ArrayList<rssiData>();
                    Adapter_Rssi.device_4 = new ArrayList<rssiData>();

                    ActivityCompat.finishAffinity(MainActivity.this);
                    System.runFinalizersOnExit(true);
                    System.exit(0);


                } catch(IOException e){}

                // 파일이 1개 이상이면 파일 이름 출력
                if ( file.listFiles().length > 0 )
                    for ( File f : file.listFiles() ) {
                        String str = f.getName();
                        Log.v(null,"fileName : "+str);

                        // 파일 내용 읽어오기
                        String loadPath = dirPath+"/"+str;
                        try {
                            FileInputStream fis = new FileInputStream(loadPath);
                            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fis));

                            String content="", temp="";
                            while( (temp = bufferReader.readLine()) != null ) {
                                content += temp;
                            }
                            Log.v(null,""+content);
                        } catch (Exception e) {}
                    }
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 1000000000);



        save_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(MainActivity.this.LAYOUT_INFLATER_SERVICE);
//                View layout = inflater.inflate(R.layout.dialog, (ViewGroup) findViewById(R.id.popup));
//                AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
//
//                final EditText txt_name = (EditText) layout.findViewById(R.id.txt_name);
//
//                aDialog.setTitle("Rssi receive"); //타이틀바 제목
//                aDialog.setView(layout); //dialog.xml 파일을 뷰로 셋팅
//                aDialog.setPositiveButton("확인",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
////                               String dirPath = "/storage/emulated/0";
//
//                            }
//                        }).setNegativeButton("취소",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                // 'No'
//                                return;
//                            }
//                        });
//                AlertDialog ad = aDialog.create();
//
//                ad.show();


            }
        });
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
    private ScanCallback mScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    Log.e("what the" , "first" + result.toString());
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                double d1 = 0, d2 = 0 , d3 = 0, d4 = 0;
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {


                        @Override
                        public void run() {

                            if (device.getAddress().equals("B8:27:EB:A6:A1:E9") || device.getAddress().equals("B8:27:EB:26:28:F4") || device.getAddress().equals("B8:27:EB:25:31:D6") || device.getAddress().equals("B8:27:EB:3A:91:F4")) {


                                Long tsLong = System.currentTimeMillis() / 1000;
                                String ts = tsLong.toString();
                                //byte txpw = scanRecord[29];
                                scanRecord.toString();


                                if (device.getAddress().equals("B8:27:EB:A6:A1:E9")) {
                                    device1.setText("" + rssi);
//                                    d1 = calculateAccuracy(-56,rssi);
                                    //근의 공식
                                    double a = -0.00285838, b = 0.08813278, c = 0.40389014, y = (-25.0)/rssi;

                                    d1 = ( -b + Math.sqrt( b*b + 4*(c-y)*a))
                                            /(2*(c-y));
                                    d2 = ( -b - Math.sqrt( b*b + 4*(c-y)*a))
                                            /(2*(c - y));
//                                    d1 = Math.sqrt( b*b + 4.0*(y-c)*a);
//                                    d2 = b*b + 4.0*(y-c)*a;
                                    device1_distance.setText("  " + d1);
                                    device2_distance.setText("  " + d2);

                                    printScanRecord(scanRecord);
                                    adapter.addItem(device.getName(), device.getAddress(), ""+ts ,"" + rssi, ""+ d1);
                                    adapter.notifyDataSetChanged();
                                }

                                if (device.getAddress().equals("B8:27:EB:26:28:F4")){
                                    avg_d2 += rssi;
                                    c_d2 ++;
                                    //d2 = calculateAccuracy(-56,rssi);


                                    if(c_d2 > 10){
                                        device2.setText("" + avg_d2/c_d2);
                                        d2 = calculateAccuracy(-56, avg_d2/c_d2);
                                        device2_distance.setText("  "+ d2);
                                        avg_d2 = 0;
                                        c_d2 = 0;
                                    }
                                    printScanRecord(scanRecord);
                                    adapter.addItem(device.getName(), device.getAddress(), ""+ ts , "" + rssi, ""+ d2);
                                    adapter.notifyDataSetChanged();
                                }
                                //test
                                if (device.getAddress().equals("B8:27:EB:3A:91:F4")) {
                                    device2.setText("" + avg_d2/c_d2);
                                    device2_distance.setText("  "+ d2);

                                    d4 = calculateAccuracy(-56, rssi);
                                    printScanRecord(scanRecord);
                                    adapter.addItem(device.getName(), device.getAddress(), ""+ ts , "" + rssi, ""+ d4);
                                    adapter.notifyDataSetChanged();

                                }

                                if (device.getAddress().equals("B8:27:EB:25:31:D6")) {
                                    avg_d3 += rssi;
                                    c_d3 ++;
                                    //d3 = calculateAccuracy(-56,rssi);

                                    if(c_d3 > 30){
                                        device3.setText("" + avg_d3/c_d3);
                                        d3 = calculateAccuracy(-56, avg_d3/c_d3);
                                        device3_distance.setText("  "+ d3);
                                        c_d3 = 0;
                                        avg_d3=0;
                                    }
                                    printScanRecord(scanRecord);
                                    adapter.addItem(device.getName(), device.getAddress(),"" + ts, "" + rssi, ""+ d3);
                                    adapter.notifyDataSetChanged();
                                }


                                double x1 = 0, y1 = 0;     // device2 addr = d2
                                double x2 = 0, y2 = 2;    // device3 addr = d3
                                double x3 = -1, y3 = -3;   // device1 addr = d1
                                double x=0, y =0; // terminal addr init

                                y = (Math.pow(d2,2)+4-Math.pow(d3,2))/4;

                                double x_t =Math.pow(d2,2)-y;
                                if(x_t <0)
                                    x_t=-1*x_t;

                                x=Math.sqrt(x_t);
                                Log.e("d1","d1 = "+d1);
                                Log.e("d2","d2 = "+d2);
                                Log.e("d3","d3 = "+d3);
                                Log.e("x","x = "+x);
                                Log.e("y","y = "+y);

                                Log.e("rssi", "rssi = " + " name : " + device.getName() + " address" + device.getAddress() + " " + rssi + "time " + ts + "txpw =-56" + " " + scanRecord);
                                Log.e("scanRecord", "scanRecord = " + scanRecord.toString());



//                                for(int i = 0 ; i < 56 ; i++){
//                                    for(int j = 10 ; j < 50 ; j++)
//                                    Log.e("distance", "i hope = "+ i +" " + j + " " + calculateAccuracy(i,j));
//                                }
                            }
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

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }
}

