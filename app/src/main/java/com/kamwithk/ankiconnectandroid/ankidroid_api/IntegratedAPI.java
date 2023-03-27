package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
        int permission = ContextCompat.checkSelfPermission(context, READ_WRITE_PERMISSION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity)context, new String[]{READ_WRITE_PERMISSION}, 0);
        }
    }

    //public File getExternalFilesDir() {
    //    return context.getExternalFilesDir(null);
    //}

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
     * @return The id of the note added
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

    public boolean updateNoteFields(long note_id, Map<String, String> new_fields, final Map<String, byte[]> files_to_add, final Map<String, ArrayList<String>> field_to_filenames_img, final Map<String, ArrayList<String>> field_to_filenames_sound) throws Exception {
        String[] model_field_names = modelAPI.modelFieldNames(noteAPI.getNoteModelId(note_id));
        String[] card_fields = noteAPI.getNoteFields(note_id);
        Map<String, String> stored_files = new HashMap<>();
        for (int i = 0; i < model_field_names.length; i++) {
            if (new_fields.get(model_field_names[i]) != null) {
                // Update field to new value
                card_fields[i] = new_fields.get(model_field_names[i]);

                // Add image files
                ArrayList<String> filenames = field_to_filenames_img.get(model_field_names[i]);
                if (filenames != null) {
                    for (String filename: filenames) {
                        // See if file already stored, otherwise add it
                        String stored_name = stored_files.get(filename);
                        if (stored_name == null) {
                            stored_name = mediaAPI.storeMediaFile(filename, files_to_add.get(filename));
                            stored_files.put(filename, stored_name);
                        }
                        card_fields[i] += "<img src=\"" + stored_name + "\">";
                    }
                }

                // Add sound and video files
                filenames = field_to_filenames_sound.get(model_field_names[i]);
                if (filenames != null) {
                    for (String filename: filenames) {
                        // See if file already stored, otherwise add it
                        String stored_name = stored_files.get(filename);
                        if (stored_name == null) {
                            stored_name = mediaAPI.storeMediaFile(filename, files_to_add.get(filename));
                            stored_files.put(filename, stored_name);
                        }
                        card_fields[i] += "[sound:" + stored_name + "]";
                    }
                }
            }
        }

        boolean res =  noteAPI.updateNoteFields(note_id, card_fields);

        if (res) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Card updated", Toast.LENGTH_LONG).show());
        } else {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to update card", Toast.LENGTH_LONG).show());
        }
        return res;
    }

    public String storeMediaFile(BinaryFile binaryFile) throws IOException {
        return mediaAPI.storeMediaFile(binaryFile.getFilename(), binaryFile.getData());
    }

    public ArrayList<Long> guiBrowse(String query) {
        // https://github.com/ankidroid/Anki-Android/pull/11899
        Uri webpage = Uri.parse("anki://x-callback-url/browser?search=" + query);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
        webIntent.setPackage("com.ichi2.anki");
        // FLAG_ACTIVITY_NEW_TASK is needed in order to display the intent from a different app
        // FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_TASK_ON_HOME is needed in order to not
        // cause a long chain of activities within Ankidroid
        // (i.e. browser <- word <- browser <- word <- browser <- word)
        // FLAG_ACTIVITY_CLEAR_TOP also allows the browser window to refresh with the new word
        // if AnkiDroid was already on the card browser activity.
        // see: https://stackoverflow.com/a/23874622
        webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        context.startActivity(webIntent);

        // The result doesn't seem to be used by Yomichan at all, so it can be safely ignored.
        // If we want to get the results, calling the findNotes() method will likely cause
        // unwanted delay.
        return new ArrayList<>();
    }
}

