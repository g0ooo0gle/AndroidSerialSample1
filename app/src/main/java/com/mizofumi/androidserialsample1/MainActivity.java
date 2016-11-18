package com.mizofumi.androidserialsample1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Serial serial;
    TextView textView;
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View content_view = findViewById(R.id.content_main);

        textView = (TextView)content_view.findViewById(R.id.textView);


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
                                    serial.run();
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
                textView.setText(data+textView.getText());
            }

            @Override
            public void read_failed(String errorMessage) {

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
