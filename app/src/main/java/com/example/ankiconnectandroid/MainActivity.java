package com.example.ankiconnectandroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.example.ankiconnectandroid.ankidroid_api.IntegratedAPI;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "ankiConnectAndroid";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntegratedAPI.authenticate(this);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Ankiconnect Android", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public void startService(View view) {
        Intent serviceIntent = new Intent(this, Service.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService(View view) {
        Intent serviceIntent = new Intent(this, Service.class);
        stopService(serviceIntent);
    }
}