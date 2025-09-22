package com.kamwithk.ankiconnectandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.internal.StringUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        private static final String DEFAULT_DIRECTORY_PATH = "/Android/data/com.kamwithk.ankiconnectandroid/files";
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

            preference = findPreference("access_manage_all_files_perms");
            if (preference != null) {
                // custom handler of preference: open permissions screen
                preference.setOnPreferenceClickListener(p -> {
                    Intent permIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(permIntent);
                    return true;
                });

            }


            EditTextPreference corsHostPreference = findPreference("cors_hostname");
            if (corsHostPreference != null) {
                corsHostPreference.setOnBindEditTextListener(editText -> editText.setHint("e.g. http://example.com"));            }



            preference = findPreference("storage_location");
            if (preference != null) {
                Context context = getContext();
                if (context == null) {
                    Toast.makeText(getContext(), "Cannot get local audio folder, as context is null.", Toast.LENGTH_LONG).show();
                } else {
                    String[] dirs = Arrays.stream(context.getExternalFilesDirs(null)).map(File::getAbsolutePath).map(s -> s.replace(DEFAULT_DIRECTORY_PATH, "")).toArray(String[]::new);
                    ((ListPreference) preference).setEntries(dirs);
                    ((ListPreference) preference).setEntryValues(dirs);

                    if((StringUtil.isBlank(((ListPreference) preference).getValue()))){
                        preference.setDefaultValue(dirs[0]); // The first value is equivalent to context.getExternalFilesDir(null)
                        ((ListPreference) preference).setValueIndex(0);
                    }
                }
            }
            preference = findPreference("storage_dir_path");
            if (preference != null) {
                Context context = getContext();
                if (context == null) {
                    Toast.makeText(getContext(), "Cannot get local audio folder, as context is null.", Toast.LENGTH_LONG).show();
                } else {
                    preference.setDefaultValue(DEFAULT_DIRECTORY_PATH);
                    preference.setOnPreferenceChangeListener((p, i) -> {
                        ListPreference storagePreference = findPreference("storage_location");
                        Path fullPath = Paths.get(storagePreference.getValue(), i.toString());

                        if (!Files.exists(fullPath)){
                            Snackbar.make(context, getView(), "Not a valid directory\n"+fullPath.toString(), Snackbar.LENGTH_LONG)
                                    .show();
                            return false;
                        }
                        return true;
                    });

                }
            }

            preference = findPreference("reset_storage_settings");
            if (preference != null) {
                // custom handler of preference: open permissions screen
                preference.setOnPreferenceClickListener(p -> {

                    Context context = getContext();
                    if (context == null) {
                        Toast.makeText(getContext(), "Cannot get local audio folder, as context is null.", Toast.LENGTH_LONG).show();
                    } else {
                        ListPreference storageDevicePreference = findPreference("storage_location");
                        EditTextPreference storageDirPreference = findPreference("storage_dir_path");

                        String[] dirs = Arrays.stream(context.getExternalFilesDirs(null)).map(File::getAbsolutePath).map(s -> s.replace(DEFAULT_DIRECTORY_PATH, "")).toArray(String[]::new);

                        storageDevicePreference.setValue(dirs[0]);
                        storageDevicePreference.setValueIndex(0);
                        storageDirPreference.setText(DEFAULT_DIRECTORY_PATH);

                    }
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
                        ListPreference storageDevicePreference = findPreference("storage_location");
                        EditTextPreference storageDirPreference = findPreference("storage_dir_path");

                        Path fullPath = Paths.get(storageDevicePreference.getValue(), storageDirPreference.getText());

                        Toast.makeText(getContext(), "Local audio folder: " + fullPath.toString(), Toast.LENGTH_LONG).show();

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
        }
    }


}