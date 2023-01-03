package com.kamwithk.ankiconnectandroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        // my_child_toolbar is defined in the layout file
        Toolbar settingsToolbar =
                (Toolbar) findViewById(R.id.settingsToolbar);
        setSupportActionBar(settingsToolbar);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference preference = findPreference("access_overlay_perms");
            if (preference != null) {
                preference.setOnPreferenceClickListener(p -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Intent permIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivity(permIntent);
                    } else {
                        Toast.makeText(getContext(), "Android SDK is less than 23. No permissions must be set.", Toast.LENGTH_LONG).show();
                    }
                    return true;
                });

            }

        }
    }


}