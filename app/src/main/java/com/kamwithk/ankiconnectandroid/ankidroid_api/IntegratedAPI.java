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

import com.kamwithk.ankiconnectandroid.request_parsers.RequestMedia;

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

    public void updateNoteFields(long note_id, Map<String, String> new_fields, ArrayList<RequestMedia> media_to_add) throws Exception {
        /*
         * updateNoteFields request looks like:
         * id: int,
         * fields: {
         *     field_name: string
         * },
         * audio | video | picture: [
         *     {
         *         data: base64 string,
         *         filename: string,
         *         fields: string[]
         *         + more fields that are currently unsupported
         *      }
         * ]
         *
         * Fields is an incomplete list of fields, and the Anki API expects the the passed in field
         * list to be complete. So, need to get the existing fields and only update them if present
         * in the request. Also need to reverse map each media file back to the field it will be
         * included in and append it enclosed in either <img> or [sound: ]
         */

        // Store media and create a field: enclosed_filename map to avoid O(n^2) lookup later
        Map<String, ArrayList<String>> field_to_files = new HashMap<>();
        for (RequestMedia media: media_to_add) {
            // mediaAPI.storeMediaFile() doesn't store as the passed in filename, need to use the returned one
            String stored_filename = mediaAPI.storeMediaFile(media.getFilename(), media.getData());

            String enclosed_filename = "";
            switch (media.getType()) {
                case AUDIO:
                case VIDEO:
                    enclosed_filename = "[sound:" + stored_filename + "]";
                    break;
                case PICTURE:
                    enclosed_filename = "<img src=\"" + stored_filename + "\">";
                    break;
            }

            for (String field: media.getFields()) {
                if (!field_to_files.containsKey(field)) {
                    field_to_files.put(field, new ArrayList<>());
                }
                field_to_files.get(field).add(enclosed_filename);
            }
        }

        // Get old fields and update values as needed
        String[] model_field_names = modelAPI.modelFieldNames(noteAPI.getNoteModelId(note_id));
        String[] card_fields = noteAPI.getNoteFields(note_id);
        for (int i = 0; i < model_field_names.length; i++) {
            if (new_fields.get(model_field_names[i]) != null) {
                // Update field to new value
                card_fields[i] = new_fields.get(model_field_names[i]);

                // Add media files
                ArrayList<String> enclosed_media_filenames = field_to_files.get(model_field_names[i]);
                if (enclosed_media_filenames != null) {
                    for (String enclosed_media_filename: enclosed_media_filenames) {
                        card_fields[i] += enclosed_media_filename;
                    }
                }
            }
        }

        noteAPI.updateNoteFields(note_id, card_fields);
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

