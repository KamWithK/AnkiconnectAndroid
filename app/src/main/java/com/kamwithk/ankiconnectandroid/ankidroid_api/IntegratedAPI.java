package com.kamwithk.ankiconnectandroid.ankidroid_api;

import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.*;

import static com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION;

public class IntegratedAPI {
    private Context context;
    public final DeckAPI deckAPI;
    public final ModelAPI modelAPI;
    public final NoteAPI noteAPI;
    public final MediaAPI mediaAPI;

    public IntegratedAPI(Context context) {
        this.context = context;

        deckAPI = new DeckAPI(context);
        modelAPI = new ModelAPI(context);
        noteAPI = new NoteAPI(context);
        mediaAPI = new MediaAPI(context);
    }

    public static void authenticate(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = ContextCompat.checkSelfPermission(context, READ_WRITE_PERMISSION);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity)context, new String[]{READ_WRITE_PERMISSION}, 0);
            }

            // TODO this needs a good interface!
//            if (!Settings.canDrawOverlays(context)) {
//                Intent permIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                context.startActivity(permIntent);
//            }
        }
    }

    public void addSampleCard() {
        Map<String, String> data = new HashMap<>();
        data.put("Back", "sunrise");
        data.put("Front", "日の出");

        try {
            addNote(data, "Temporary", "Basic", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add flashcards to AnkiDroid through instant add API
     * @param data Map of (field name, field value) pairs
     */
    public Long addNote(final Map<String, String> data, String deck_name, String model_name, Set<String> tags) throws Exception {
        Long deck_id = deckAPI.getDeckID(deck_name);
        Long model_id = modelAPI.getModelID(model_name, data.size());
        Long note_id = noteAPI.addNote(data, deck_id, model_id, tags);

        if (note_id != null) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Card added", Toast.LENGTH_LONG).show());
            return note_id;
        } else {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to add card", Toast.LENGTH_LONG).show());
            throw new Exception("Couldn't add note");
        }
    }

    public String storeMediaFile(String filename, byte[] data) throws IOException {
        return mediaAPI.storeMediaFile(filename, data);
    }

    public ArrayList<Long> guiBrowse(String query) {
        // https://github.com/ankidroid/Anki-Android/pull/11899
        Uri webpage = Uri.parse("anki://x-callback-url/browser?search=" + query);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        webIntent.setPackage("com.ichi2.anki");
        // might need FLAG_ACTIVITY_CLEAR_TOP?
        // see: https://stackoverflow.com/a/23874622
        webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(webIntent);

        // I don't think the result is used in yomichan at all, so it can be safely ignored?
        return new ArrayList<>();
    }
}
