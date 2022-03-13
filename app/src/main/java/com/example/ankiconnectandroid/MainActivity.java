package com.example.ankiconnectandroid;

import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ankiconnectandroid.ankidroid_api.IntegratedAPI;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PORT = 8765;
    public IntegratedAPI integratedAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            integratedAPI = new IntegratedAPI();
        } catch (Exception e) {
            Log.w("Ankidroid", "Unable to connect to Ankidroid");
            e.printStackTrace();
        }

        try {
            Router server = new Router(PORT, this);
        } catch (IOException e) {
            Log.w("Httpd", "The Server was unable to start");
            e.printStackTrace();
        }

        TextView textView = findViewById(R.id.text_message);
        textView.setText("Service Started\nYou can now mine with Yomichan!");

//        integratedAPI.addSampleCard();
    }
}