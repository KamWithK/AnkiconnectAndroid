package com.kamwithk.ankiconnectandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
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

        Toolbar settingsToolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(settingsToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // adds back button
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference preference = findPreference("access_overlay_perms");
            if (preference != null) {
                // custom handler of preference: open permissions screen
                preference.setOnPreferenceClickListener(p -> {
                    Intent permIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivity(permIntent);
                    return true;
                });

            }

            preference = findPreference("get_dir_path");
            if (preference != null) {
                // custom handler of preference: open permissions screen
                preference.setOnPreferenceClickListener(p -> {

                    Context context = getContext();
                    if (context == null) {
                        Toast.makeText(getContext(), "Cannot get local audio folder, as context is null.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Local audio folder: " + context.getExternalFilesDir(null), Toast.LENGTH_LONG).show();
                        // TODO snackbar?
                        // getView() seems to be null...
//                        Snackbar snackbar = Snackbar.make(getView().findViewById(R.id.settings),
//                                "Local audio folder: " + context.getExternalFilesDir(null),
//                                BaseTransientBottomBar.LENGTH_LONG);
//                        snackbar.show();
                    }
                    return true;
                });

            }

           EditTextPreference corsHostPreference = findPreference("cors_hostname");
           if (corsHostPreference != null) {
                corsHostPreference.setOnBindEditTextListener(editText -> editText.setHint("e.g. http://example.com"));            }
        }
    }


}