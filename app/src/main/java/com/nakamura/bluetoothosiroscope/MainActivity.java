package com.nakamura.bluetoothosiroscope;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    private Serial serial;
    TextView textView ,debugTextView;
    LineChart linechart;
    FloatingActionButton fab;
    boolean isBackPressed = false;
    BluetoothSPP bt;
    Menu menu;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View content_view = findViewById(R.id.content_main);

        textView = (TextView)content_view.findViewById(R.id.textView);
        //spplib
        debugTextView = (TextView)content_view.findViewById(R.id.debugTextView);
        linechart = (LineChart) content_view.findViewById(R.id.linechart);

        //spplib
        bt = new BluetoothSPP(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //spplib
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                debugTextView.append(message + "\n");
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                textView.setText("Status : Not connect");
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                textView.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                textView.setText("Status : Connected to " + name);
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
            }
        });



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fab.hide();

                new Thread(new Runnable() {
                    @Override
                    public void run() {



                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (serial==null){
                                    makeSerial();
                                }

                                if (!serial.isConnected()){
                                    serial.open(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                    serial.run(100);
                                }else {
                                    if (serial.isRunnable()){
                                        serial.stop();
                                    }
                                    serial.close();
                                    serial = null;
                                }
                            }
                        });


                    }
                }).start();




            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }




    public void makeSerial(){
        serial = new Serial("BT04-A");
        serial.setSerialListener(new SerialListener() {

            ArrayList<Float> xtimes = new ArrayList<Float>();//実験
            ArrayList<Entry> values = new ArrayList<Entry>();

            //要素のカウント
            //オーバーフロー時考慮していない（動作未確認）
            int counter = 0 ;

            @Override
            public void opened() {
                Toast.makeText(MainActivity.this,"こねくしょんおっけー",Toast.LENGTH_SHORT).show();
                fab.setImageResource(R.drawable.ic_sync_disabled_white_48dp);
                fab.show();
            }

            @Override
            public void open_failed(String errorMessage) {
                Toast.makeText(MainActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                fab.setImageResource(R.drawable.ic_sync_white_48dp);
                fab.show();
            }

            @Override
            public void read(String data) {

                //textView.setText(data+textView.getText());

                try {
                    if (data.contains(",")){
                        String[] dataarray = data.split("\n");

                        for (int i = 0; i < dataarray.length; i++) {
                            String[] datas = dataarray[i].split(",");

                            if (datas.length < 2){
                                break;
                            }
                            Log.d("Recived",datas[0]+","+datas[1]);

                            //分けた配列0番目の処理

                            textView.setText(datas[1]);
                            xtimes.add(counter,Float.valueOf(datas[0]));//サンプル時間リスト追加
                            //values.add(new Entry(counter,Float.valueOf(datas[1])));//データ値リストに追加
                            values.add(new Entry(Float.valueOf(datas[0]),Float.valueOf(datas[1])));//データ値リストに追加(x,y)

                            Log.d("debugサンプル時間のリスト要素数:", String.valueOf(xtimes.size()));
                            Log.d("debugデータ値のリスト要素数", String.valueOf(values.size()));

                            //画面描画時に波形が動き始める値の調整用
                            //先頭の値削除
                            if (values.size() > 100){
                                linechart.getLineData().getDataSets().get(0).removeFirst();
                            }

                            if (Float.valueOf(datas[0]) == 0){
                                linechart.invalidate(); // refresh
                            }


                            //chart初期化
                            //touch gesture設定
                            linechart.setTouchEnabled(true);
                            // スケーリング&ドラッグ設定
                            linechart.setDragEnabled(true);
                            linechart.setScaleEnabled(true);
                            linechart.setDrawBorders(true);
                            //背景
                            linechart.setDrawGridBackground(false);

                            XAxis xAxis = linechart.getXAxis();
                            xAxis.setTextColor(Color.BLACK);

                            LineDataSet set1 = new LineDataSet(values,"波形");

                            //点線設定
                            //set1.enableDashedLine(10f, 5f, 0f);
                            //set1.enableDashedHighlightLine(10f, 5f, 0f);

                            set1.setColor(Color.GREEN);
                            set1.setDrawValues(false);          //値ラベル表示しない
                            set1.setLineWidth(1f);

                            //プロット点設定
                            set1.setCircleRadius(3f);
                            set1.setDrawCircleHole(false);
                            set1.setCircleColor(Color.GREEN);

                            set1.setValueTextSize(9f);
                            set1.setDrawFilled(false);
                            set1.setFormLineWidth(1f);
                            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                            set1.setFormSize(15.f);


                            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

                            dataSets.add(set1);

                            dataSets.get(0).getXMax();

                            LineData lineData = new LineData(dataSets);

                            linechart.setData(lineData);


                            linechart.getData().notifyDataChanged();
                            lineData.notifyDataChanged();

                            //最新データまで移動
                            linechart.moveViewToX(lineData.getEntryCount());


                            counter++;

                        }

                    }
                    else if (data.contains("e")){
                        linechart.invalidate(); // refresh
                    }

                }catch (NumberFormatException e){

                    Toast.makeText(MainActivity.this,"format error"+ data,Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void read_failed(String errorMessage) {

            }

            @Override
            public void write_success() {

            }

            @Override
            public void write_failed(String s) {

            }

            @Override
            public void stoped() {
                Toast.makeText(MainActivity.this,"よみこみてーししたで",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void closed() {
                Toast.makeText(MainActivity.this,"こねくしょんkillしたで",Toast.LENGTH_SHORT).show();
                fab.setImageResource(R.drawable.ic_sync_white_48dp);
                fab.show();
                if (isBackPressed){
                    finish();
                }
            }

            @Override
            public void close_failed(String errorMessage) {
                Toast.makeText(MainActivity.this,errorMessage,Toast.LENGTH_SHORT).show();
                fab.setImageResource(R.drawable.ic_sync_white_48dp);
                fab.show();
            }
        });



    }

    @Override
    public void onBackPressed() {


        if(serial!= null){
            if (serial.isRunnable()){
                isBackPressed =true;
                serial.stop();
                serial.close();

            }else{

                finish();
            }

        }else{
            finish();
        }

        super.onBackPressed();

    }

    @Override
    public void finish() {
        super.finish();
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //testlib
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_disconnect) {
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }
    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
 //               setup();//送信用
            }
        }
    }
//送信用のやつ
/*

    public void setup() {
        Button btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(etMessage.getText().length() != 0) {
                    bt.send(etMessage.getText().toString(), true);
                    etMessage.setText("");
                }
            }
        });
    }
*/

public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
        if(resultCode == Activity.RESULT_OK)
            bt.connect(data);
    } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
        if(resultCode == Activity.RESULT_OK) {
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_ANDROID);
            //setup();//とめてる
        } else {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth was not enabled."
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
}
