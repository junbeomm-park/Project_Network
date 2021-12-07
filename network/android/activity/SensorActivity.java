package com.test.semi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SensorActivity extends AppCompatActivity {
    Button main_btn;
    ImageView img;
    TextView textView;
    NotificationManagerCompat notificationManager;
    String channelId = "channel";
    String channelName = "Channel_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        main_btn = findViewById(R.id.main_btn);
        img = findViewById(R.id.sen_img);
        textView = findViewById(R.id.sen_text);

        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SensorActivity.this, NativeActivity.class);
                startActivity(intent);
            }
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Main", "토큰 가져오는 데 실패함", task.getException());
                            return;
                        }

                        String newToken = task.getResult();
                        println("등록 id--------------------------------------- : " + newToken);
                    }
                });
    }

    public void println(String data1) {
        Log.d("FMS", data1);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        println("onNewIntent 호출됨");
        if (intent != null) {
            processIntent(intent);
        }

        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        String from = intent.getStringExtra("from");

        if (from == null) {
            println("from is null.");
            return;
        }

        String c1 = intent.getStringExtra("c1");
        String channelId = "channel";
        Log.d("c1",c1);
        textView.setText(c1+"ºC" );


        if(Integer.parseInt(c1)<= 25) {
            img.setImageResource(R.drawable.sensor);
        }else if (Integer.parseInt(c1)<= 29) {
            img.setImageResource(R.drawable.sensor25);
        }else if(Integer.parseInt(c1)<= 30) {
            img.setImageResource(R.drawable.sensor30);
        }
    }
}