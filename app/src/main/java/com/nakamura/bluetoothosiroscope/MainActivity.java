package com.nakamura.bluetoothosiroscope;

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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Serial serial;
    TextView textView;
    LineChart linechart;
    FloatingActionButton fab;
    boolean isBackPressed = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View content_view = findViewById(R.id.content_main);

        textView = (TextView)content_view.findViewById(R.id.textView);
        linechart = (LineChart) content_view.findViewById(R.id.linechart);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



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

    public void makeSerial(){
        serial = new Serial("BT04-A");
        serial.setSerialListener(new SerialListener() {

            ArrayList<Entry> xtimes = new ArrayList<Entry>();
            ArrayList<Entry> values = new ArrayList<Entry>();

            //要素のカウント
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
                            xtimes.add(new Entry(counter,Float.valueOf(datas[0])));//サンプル時間リスト追加
                            values.add(new Entry(counter,Float.valueOf(datas[1])));//データ値リストに追加

                            Log.d("MainAcitivity", String.valueOf(values.size()));

                            //画面描画時に波形が動き始める値の調整用
                            if (values.size() > 100){
                                linechart.getLineData().getDataSets().get(0).removeFirst();
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
                }catch (NumberFormatException e){

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
