package com.mizofumi.androidserialsample1;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Serial serial = new Serial("AIUEO");
        serial.setSerialListener(new SerialListener() {
            @Override
            public void opened() {

            }

            @Override
            public void open_failed(String errorMessage) {

            }

            @Override
            public void read(String data) {

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

            }

            @Override
            public void closed() {

            }

            @Override
            public void close_failed(String s) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!serial.isConnected()){
                    serial.open(UUID.fromString(""));
                    serial.run();
                }else {
                    if (serial.isRunnable()){
                        serial.stop();
                    }
                    serial.close();
                }
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
