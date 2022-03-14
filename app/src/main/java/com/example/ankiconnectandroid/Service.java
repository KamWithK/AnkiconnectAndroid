package com.example.ankiconnectandroid;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import static com.example.ankiconnectandroid.MainActivity.CHANNEL_ID;

public class Service extends android.app.Service {
    private static final int PORT = 8765;

    private Router server;

    @Override
    public void onCreate() { // Only one time
        super.onCreate();

        try {
            server = new Router(PORT, this);
        } catch (IOException e) {
            Log.w("Httpd", "The Server was unable to start");
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // Every time start is called
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ankiconnect Android")
                .setContentText("Ankiconnect Android")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        server.closeAllConnections(); // TODO: Check if this is right way
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
