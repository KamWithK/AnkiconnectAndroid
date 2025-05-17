package com.kamwithk.ankiconnectandroid;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;


public class MainActivity extends AppCompatActivity {

    public static class NotificationsPermissionDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            MainActivity activity = (MainActivity) getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.dialog_notif_perm_info)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // ASSUMPTION: NotificationsPermissionDialogFragment is only created
                            // on API level >= 33
                            activity.requestPermissionLauncher.launch(POST_NOTIFICATIONS);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            activity.startServiceWithoutNotifications();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static final String CHANNEL_ID = "ankiConnectAndroid";
    private NotificationManager notificationManager;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar support
        Toolbar toolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(toolbar);

        IntegratedAPI.authenticate(this);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Ankiconnect Android", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);

        // this cannot be put inside attemptGrantNotifyPermissions, because it is called by
        // a onClickListener and crashes the app: https://stackoverflow.com/a/67582633
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Attempting to start server without notification...", Toast.LENGTH_LONG).show();
                    }
                    startService();
                });
    }

    protected  void onStart() {
        super.onStart();
        startServiceWrap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // open settings
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void attemptGrantNotificationPermissions() {
        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher, as an instance variable.

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // there's nothing else we can do on older SDK versions
            startServiceWithoutNotifications();
            return;
        }

        // NOTE: I can't find anything about this in the actual documentation, but an
        // explanation for shouldShowRequestPermissionRationale is shown below
        // (taken from: https://stackoverflow.com/a/39739972):
        //
        // This method returns true if the app has requested this permission previously and the
        // user denied the request. Note: If the user turned down the permission request in the
        // past and chose the Don't ask again option in the permission request system dialog,
        // this method returns false.
        if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {
            // Explain that notifications are "needed" to display the server
            new NotificationsPermissionDialogFragment().show(this.getSupportFragmentManager(), "post_notifications_dialog");
        } else {
            // Directly ask for the permission.
            requestPermissionLauncher.launch(POST_NOTIFICATIONS);
        }

    }

    public void startServiceWithoutNotifications() {
        Toast.makeText(this, "Attempting to start server without notification...", Toast.LENGTH_LONG).show();
        startService();
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, Service.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }


    public void startServiceBtn(View view) {
        startServiceWrap();
    }

    public void startServiceWrap(){
        boolean notificationsEnabled = notificationManager.areNotificationsEnabled();
        if (notificationsEnabled) {
            startService();
        } else {
            attemptGrantNotificationPermissions();
        }
    }

    public void stopServiceBtn(View view) {
        Intent serviceIntent = new Intent(this, Service.class);
        stopService(serviceIntent);
    }
}
