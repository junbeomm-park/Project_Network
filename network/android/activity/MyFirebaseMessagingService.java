package com.test.semi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FMS";
    NotificationManagerCompat notificationManager;

    public MyFirebaseMessagingService() {

    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d(TAG, "onNewToken 호출됨 : " + token);
    }

    @Override
    public void onMessageReceived(@NonNull  RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived 호출됨.");

        String from = remoteMessage.getFrom();
      //  String from2 = remoteMessage.getFrom();
        Map<String, String> data = remoteMessage.getData();
        String contents1 = data.get("c1");
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        Log.d(TAG, "from : " + from + ", contents : " + contents1+" " +title+" "+body);
        if(body.equals("Button ON")){
            sendToActivity(getApplicationContext(), from, title, body);
        }else if(body.equals("LED ON")){
            sendToActivity(getApplicationContext(), from, title, body);

        }else{
            sendToActivity2(getApplicationContext(), from, contents1);
        }
        //sendToActivity(getApplicationContext(), from,  contents1, title, body);
        //sendToActivity2(getApplicationContext(), from2, contents1,title, body);
    }

    private void sendToActivity2(Context context, String from, String contents1) {
        Intent intent = new Intent(context, SensorActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("c1",contents1);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    private void sendToActivity(Context context, String from, String title, String body) {
        Intent intent = new Intent(context, semi_web.class);
        intent.putExtra("from", from);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
