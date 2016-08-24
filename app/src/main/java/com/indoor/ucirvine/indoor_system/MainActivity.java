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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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

import Jama.Matrix;


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

    private Handler mHandlerToRestart;
    private Runnable mRunableToRestart;

    private Handler mHandlerToMusic;
    private Runnable mRunableToMusic;

    Uri myUri = Uri.parse("file:///sdcard/Download/Hi.mp3"); // initialize Uri here

    double avg_d2;
    int c_d2;

    double avg_d3;
    int c_d3;

    int avg1[] = new int[8];
    int avg2[] = new int[8];
    int avg3[] = new int[8];
    int avg4[] = new int[8];

    int size1 = 0;
    int size2 = 0;
    int size3 = 0;
    int size4 = 0;

    double result1 = 0;
    double result2 = 0;
    double result3 = 0;
    double result4 = 0;

    boolean full1 = false;
    boolean full2 = false;
    boolean full3 = false;
    boolean full4 = false;


    //TODO predict value
    double predict_x = 0;
    double predict_p = 0.1;
    double consitant_r = 0.1;

    //Z
    double distance;


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

//        mBluetoothAdapter.


        avg_d2 = 0;
        c_d2 = 0;

        avg_d3 = 0;
        c_d3 = 0;

        mBluetoothAdapter.startLeScan(mLeScanCallback);


        mRunableToMusic = new Runnable() {
            @Override
            public void run() {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();

            }
        };

        mRunableToRestart = new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                mHandlerToRestart.postDelayed(mRunableToRestart, 3000);
                Log.e("isGood","working");

            }
        };

        mRunnable = new Runnable() {
            @Override
            public void run() {
                String dirPath = "/sdcard";

                File file = new File(dirPath);

                int rssi_device1 = Adapter_Rssi.device_1.size();        //버튼 클릭시 사이즈 픽스
                int rssi_device2 = Adapter_Rssi.device_2.size();
                int rssi_device3 = Adapter_Rssi.device_3.size();
                int rssi_device4 = Adapter_Rssi.device_4.size();

                int rssi_result = Adapter_Rssi.result.size();
                int rssi_result2 = Adapter_Rssi.result2.size();


                // 일치하는 폴더가 없으면 생성
                if( !file.exists() ) {
                    file.mkdirs();
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }

                String testStr = "";
                // txt 파일 생성

                for(int i = 0 ; i < rssi_device1; i ++){
                    testStr += Adapter_Rssi.device_1.get(i).getDeviceAddress() + " " + Adapter_Rssi.device_1.get(i).getRssi() + " " + Adapter_Rssi.device_1.get(i).getDistance()+ "]" ;
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

//                testStr +=" result] ";

                for(int i = 0 ; i < rssi_result; i ++){
                    testStr += Adapter_Rssi.result.get(i).getDeviceAddress() + " " + Adapter_Rssi.result.get(i).getRssi() + " " + Adapter_Rssi.result.get(i).getDistance()+ " "  + Adapter_Rssi.result.get(i).getTimeStamp()+ "]" ;
                }

//                testStr +=" result2] ";

                for(int i = 0 ; i < rssi_result2; i ++){
                    testStr += Adapter_Rssi.result2.get(i).getDeviceAddress() + " " + Adapter_Rssi.result2.get(i).getRssi() + " " + Adapter_Rssi.result2.get(i).getDistance()+ " "  + Adapter_Rssi.result2.get(i).getTimeStamp()+ "]" ;
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

                    Adapter_Rssi.result = new ArrayList<rssiData>();


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


        mHandlerToRestart = new Handler();
        mHandlerToMusic = new Handler();
        mHandler.postDelayed(mRunnable, 20000000);
        mHandlerToRestart.postDelayed(mRunableToRestart, 3000);
        mHandlerToMusic.postDelayed(mRunableToMusic,3000);


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
                double x=0, y=0;
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


                                //device 1
                                if (device.getAddress().equals("B8:27:EB:A6:A1:E9")) {
                                    //double a = -22.94102589, b = -24.71862505 , y = rssi - (-25.1366666667) ;
                                    double a = -26.01613193, b = -25.60966355 , y = rssi - (-25.1716666667) ;

                                    avg1[size1++] = rssi;

                                    if(size1 > 7){
                                        full1 = true;
                                        size1 = 0;
                                    }

                                    if( full1) {
                                        result1 = (avg1[0] + avg1[1] + avg1[2] + avg1[3]
                                                + avg1[4] + avg1[5] + avg1[6] + avg1[7]) / 8.0;
                                        y = result1 - (-25.1716666667);


                                        d1 = (y - b) / a;
                                        d1 = Math.pow(10, d1);

                                        device1.setText("" + result1);
                                        device1_distance.setText(" dis " + d1);

//                                        adapter.addItem(device.getName(), device.getAddress(), "" + ts, "" + rssi, "" + d1);
//
//                                        //TODO calcultor avg
//                                        adapter.addItem(device.getName(), "result", "" + ts, "" + result1, "" + d1);

                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                //device 2
                                if (device.getAddress().equals("B8:27:EB:26:28:F4")){

                                    double a = -27.37390491, b = -26.44442921 , y = rssi - (-25.6375) ;

                                    avg2[size2++] = rssi;

                                    if(size2 > 7){
                                        full2 = true;
                                        size2 = 0;
                                    }

                                    if( full2) {
                                        result2 = (avg2[0] + avg2[1] + avg2[2] + avg2[3]
                                                + avg2[4] + avg2[5] + avg2[6] + avg2[7]) / 8.0;
                                        y = result2 - (-25.6375);


                                        d2 = (y - b) / a;
                                        d2 = Math.pow(10, d2);


//                                        device2.setText("" + result2);
//                                        device2_distance.setText(" dis " + d2);

//                                    adapter.addItem(device.getName(), device.getAddress(),"" + ts, "" + rssi, ""+ d2);

//                                    //TODO calcultor avg
//                                    adapter.addItem(device.getName(), "result", ""+ts ,"" + result, ""+ d2);
//
//                                    //TODO calcultor avg2
//                                    adapter.addItem(device.getName(), "result2", ""+ts ,"" + result2, ""+ d2);

                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                //device 3
                                if (device.getAddress().equals("B8:27:EB:25:31:D6")) {

                                    //double a = -23.13184903, b = -25.48251426 , y = rssi - (-25.8245833333) ;
                                    double a = -22.94102589, b = -24.71862505 , y = rssi - (-25.1366666667) ;

                                    avg3[size3++] = rssi;

                                    if(size3 > 7){
                                        full3 = true;
                                        size3 = 0;
                                    }

//                                    device3.setText("" + result3);
//                                    device3_distance.setText(" dis "+ d3);

                                    if( full3) {
                                        result3 = (avg3[0] + avg3[1] + avg3[2] + avg3[3]
                                                + avg3[4] + avg3[5] + avg3[6] + avg3[7]) / 8.0;

                                        y = result3 - (-25.8245833333);


                                        d3 = (y - b) / a;
                                        d3 = Math.pow(10, d3);


//                                    adapter.addItem(device.getName(), device.getAddress(),"" + ts, "" + rssi, ""+ d3);
//
//                                    //TODO calcultor avg
//                                    adapter.addItem(device.getName(), "result", ""+ts ,"" + result, ""+ d3);
//
//                                    //TODO calcultor avg2
//                                    adapter.addItem(device.getName(), "result2", ""+ts ,"" + result2, ""+ d3);

                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                //device 4
                                if (device.getAddress().equals("B8:27:EB:3A:91:F4")) {


                                    double a = -22.94102589, b = -24.71862505 , y = rssi - (-25.1366666667) ;


                                    avg4[size4++] = rssi;

                                    if(size4 > 7){
                                        full4 = true;
                                        size4 = 0;
                                    }

                                    if( full4) {
                                        result4 = (avg4[0] + avg4[1] + avg4[2] + avg4[3]
                                                + avg4[4] + avg4[5] + avg4[6] + avg4[7]) / 8.0;
                                        y = result4 - (-25.1366666667);


                                        d4 = (y - b) / a;
                                        d4 = Math.pow(10, d4);



//                                    adapter.addItem(device.getName(), device.getAddress(),"" + ts, "" + rssi, ""+ d4);
//
//                                    adapter.addItem(device.getName(), "result4", ""+ts ,"" + result4, ""+ d4);
//
                                    }
                                    adapter.notifyDataSetChanged();

                                }

//                                double [][] arrayA = {{-3.0,0.0},{-3.0,3.0}};
//                                Matrix matA = new Matrix(arrayA);
//                                double [][] arrayB = {{ 0.5*(d3*d3-d1*d1+9)},{0.5*(d3*d3-d2*d2+18)}};
//                                Matrix matB = new Matrix(arrayB);
//
//                                Matrix matX = (((matA.transpose().times(matA)).inverse()).times(matA.transpose())).times(matB);
//
//
//                                Log.i("dd","matX= " + (matX.get(0,0) + 3) + " " + matX.get(1,0));

                              /*  double w1,w2,w3;
                                w1 = Math.exp(-d1);
                                w2 = Math.exp(-d2);
                                w3 = Math.exp(-d3);*/
                                /*double result_x = (d3*d3-d1*d1-9)/(-6.0);
                                double result_y = (d2*d2-d1*d1-9)/(-6.0);*/

//                                double result_x = matX.get(0,0) + 3;
//                                double result_y = matX.get(1,0);
//
//
//                                device1.setText("x = " + result_x);
//                                device2.setText("y = " + result_y);
//
//                                adapter.addItem(device.getName(), "result", "" + ts, "" + result1, d1 + " " + d2 + " " + d3 + " "
//                                        + matX.get(0,0)+ " " + matX.get(1,0));



                                //TODO calculate the distance

                                //weight
                                double w1,w2;

                                w1 = Math.exp(-d2);
                                w2 = Math.exp(-d3);

                                if(d2 > 0.0 && d3 > 0.0 ) {
                                    distance = ((3.0 - d2)*w1 + d3*w2) / (w1+w2);

                                    double update_g = predict_p / (predict_p + consitant_r) ;
                                    predict_x = predict_x + update_g*(distance-predict_x);
                                    predict_p = (1 - update_g) * predict_p;

                                    adapter.addItem("yyg", "result", "" + ts, "" + rssi, d2 + " " + d3 + " " + distance + " " + predict_x);
                                    device2_distance.setText(" "+ distance);
                                    device3_distance.setText(" "+ predict_x);

                                    adapter.notifyDataSetChanged();
                                }





                                //device1.setText("" + ((3.0 - d2)*w1 + d3*w2) / (w1+w2));

//                                double x1 = 0, y1 = 0;     // device2 addr = d2
//                                double x2 = 0, y2 = 2;    // device3 addr = d3
//                                double x3 = -1, y3 = -3;   // device1 addr = d1
//                                double x=0, y =0; // terminal addr init
//
//                                y = (Math.pow(d2,2)+4-Math.pow(d3,2))/4;
//
//                                double x_t =Math.pow(d2,2)-y;
//                                if(x_t <0)
//                                    x_t=-1*x_t;
//
//                                x=Math.sqrt(x_t);
//                                Log.e("d1","d1 = "+d1);
//                                Log.e("d2","d2 = "+d2);
//                                Log.e("d3","d3 = "+d3);
//                                Log.e("x","x = "+x);
//                                Log.e("y","y = "+y);
//
//                                Log.e("rssi", "rssi = " + " name : " + device.getName() + " address" + device.getAddress() + " " + rssi + "time " + ts + "txpw =-56" + " " + scanRecord);
//                                Log.e("scanRecord", "scanRecord = " + scanRecord.toString());
//


//                                for(int i = 0 ; i < 56 ; i++){
//                                    for(int j = 10 ; j < 50 ; j++)
//                                    Log.e("distance", "i hope = "+ i +" " + j + " " + calculateAccuracy(i,j));
//                                }
                            }
                        }
                    });
                }
            };
}

