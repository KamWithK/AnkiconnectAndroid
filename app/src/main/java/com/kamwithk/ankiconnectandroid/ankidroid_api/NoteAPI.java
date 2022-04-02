package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.Context;
import android.database.Cursor;
import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.util.*;

public class NoteAPI {
    private Context context;
    private final AddContentApi api;

    public NoteAPI(Context context) {
        this.context = context;
        api = new AddContentApi(context);
    }

    /**
     * Add flashcards to AnkiDroid through instant add API
     * @param data Map of (field name, field value) pairs
     */
    public Long addNote(final Map<String, String> data, Long deck_id, Long model_id, Set<String> tags) throws Exception {
        String[] all_field_names = api.getFieldList(model_id);
        if (all_field_names == null) {
            throw new Exception("Couldn't get fields");
        }

        // Get list in correct order
        String[] fields = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            fields[i] = data.get(all_field_names[i]);
        }

        return api.addNote(model_id, deck_id, fields, tags);
    }

    public ArrayList<Boolean> canAddNotes(ArrayList<HashMap<String, String>> notes_to_test) {
        ArrayList<Boolean> note_exists = new ArrayList<>();

        for (HashMap<String, String> field : notes_to_test) {
            final Cursor cursor = context.getContentResolver().query(
                    FlashCardsContract.Note.CONTENT_URI,
                    null,
                    field.get("field") + ":" + field.get("value"),
                    null,
                    null
            );

            note_exists.add(cursor == null || !cursor.moveToFirst());
        }

        return note_exists;
    }
}
