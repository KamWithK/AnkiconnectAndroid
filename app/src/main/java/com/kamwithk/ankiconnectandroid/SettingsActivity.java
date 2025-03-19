package com.kamwithk.ankiconnectandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.File;
import java.util.Arrays;


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
                        String folder = context.getSharedPreferences(getString(R.string.shared_app_preferences), 0)
                                .getString("storage_location", context.getExternalFilesDir(null).getAbsolutePath());
                        Toast.makeText(getContext(), "Local audio folder: " + folder, Toast.LENGTH_LONG).show();
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

            preference = findPreference("storage_location");
            if (preference != null) {
                Context context = getContext();
                // custom handler of preference: open permissions screen
                if (context == null) {
                    Toast.makeText(getContext(), "Cannot get local audio folder, as context is null.", Toast.LENGTH_LONG).show();
                } else {
                    String[] dirs = Arrays.stream(context.getExternalFilesDirs(null)).map(File::getAbsolutePath).toArray(String[]::new);
                    preference.setDefaultValue(dirs[0]);
                    ((ListPreference) preference).setEntries(dirs);
                    ((ListPreference) preference).setEntryValues(dirs);
                    preference.setOnPreferenceChangeListener((p, newValue) -> {
                        context.getSharedPreferences(getString(R.string.shared_app_preferences), 0)
                                .edit()
                                .putString("storage_location", (String) newValue)
                                .commit();
                        return true;
                    });
                }
            }

           EditTextPreference corsHostPreference = findPreference("cors_hostname");
           if (corsHostPreference != null) {
                corsHostPreference.setOnBindEditTextListener(editText -> editText.setHint("e.g. http://example.com"));            }
        }
    }


}