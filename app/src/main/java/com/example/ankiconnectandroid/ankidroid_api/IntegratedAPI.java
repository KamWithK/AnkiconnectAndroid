package com.example.ankiconnectandroid.ankidroid_api;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.ichi2.anki.api.AddContentApi;

import java.io.IOException;
import java.util.*;

import static com.example.ankiconnectandroid.Router.context;
import static com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION;

public class IntegratedAPI {
    private final DeckAPI deckAPI;
    private final ModelAPI modelAPI;
    private final NoteAPI noteAPI;
    private final MediaAPI mediaAPI;

    private static String preferredName = "";

    public IntegratedAPI() {
        deckAPI = new DeckAPI();
        modelAPI = new ModelAPI();
        noteAPI = new NoteAPI();
        mediaAPI = new MediaAPI();

//        Get permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = ContextCompat.checkSelfPermission(context, READ_WRITE_PERMISSION);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity)context, new String[]{READ_WRITE_PERMISSION}, 0);
            }
        }
    }

    public void addSampleCard() {
        Map<String, String> data = new HashMap<>();
        data.put("Back", "sunrise");
        data.put("Front", "日の出");

        try {
            addNote(data, "Temporary", "Basic");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add flashcards to AnkiDroid through instant add API
     * @param data Map of (field name, field value) pairs
     */
    public Long addNote(final Map<String, String> data, String deck_name, String model_name) throws Exception {
//        Replace old paths with new ones
//        TODO: Handle multiple media appropriately
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue().matches("\\[.*:.*\\]")) {
                String replaced = entry.getValue().replaceAll("(?<=:).*(?=])", preferredName);
                data.put(entry.getKey(), replaced);
            }
        }

        Long deck_id = deckAPI.getDeckID(deck_name);
        Long model_id = modelAPI.getModelID(model_name, data.size());
        Long note_id = noteAPI.addNote(data, deck_id, model_id);

        if (note_id != null) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Card added", Toast.LENGTH_LONG).show());
            return note_id;
        } else {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to add card", Toast.LENGTH_LONG).show());
            throw new Exception("Couldn't add note");
        }
    }

    public String storeMediaFile(String filename, byte[] data) throws IOException {
        String ankidroid_path = mediaAPI.storeMediaFile(filename, data);
        preferredName = ankidroid_path;
        return ankidroid_path;
    }
}
