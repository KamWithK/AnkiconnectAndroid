package com.kamwithk.ankiconnectandroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "ankiConnectAndroid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar support
        Toolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);

        IntegratedAPI.authenticate(this);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Ankiconnect Android", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: // open settings
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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