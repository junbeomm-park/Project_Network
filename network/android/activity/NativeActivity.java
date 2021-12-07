package com.test.semi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NativeActivity extends AppCompatActivity {
    BufferedReader server_in;
    PrintWriter server_out;
    Socket server_main;
    Button web_btn;
    Button sensor_btn;
    Button on;
    Button off;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);
        web_btn = findViewById(R.id.web_btn);
        sensor_btn = findViewById(R.id.web_sensor);
        new LED_Three_Thread().start();
        on = findViewById(R.id.led_on);
        off = findViewById(R.id.led_off);
        img = findViewById(R.id.imageView);

        web_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NativeActivity.this, semi_web.class);
                startActivity(intent);
            }
        });

        sensor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NativeActivity.this, SensorActivity.class);
                startActivity(intent);
            }
        });
    }


    public void send_mssage(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = " ";
                if(view.getId() == R.id.led_on) {
                    message = "led_on";
                    img.setImageResource(R.drawable.led_on);

                }else if(view.getId() == R.id.led_off) {
                    message = "led_off";
                    img.setImageResource(R.drawable.led_off);
                }
                server_out.println(message);
            }
        }).start();
    }

    class LED_Three_Thread extends Thread {
        public void run() {
            try {
                server_main = new Socket("192.168.0.16",12345);
                if(server_main!=null) {
                    io_init();
                }
                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            String msg;
                            try {
                                msg = server_in.readLine();
                                Log.d("network","서버 수신 : "+msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                t1.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        void io_init() {
            try {
                server_in = new BufferedReader(new InputStreamReader(server_main.getInputStream()));
                server_out = new PrintWriter(server_main.getOutputStream(),true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}