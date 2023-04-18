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

    private String escapeQueryStr(String s) {
      // first replace: \ -> \\
      // second replace: " -> \"
      return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /**
     * Add flashcards to AnkiDroid through instant add API
     *
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
        ArrayList<Boolean> note_does_not_exist = new ArrayList<>();

        for (HashMap<String, String> field : notes_to_test) {
            String escapedQuery = escapeQueryStr(field.get("field") + ":" + field.get("value"));
            final Cursor cursor = context.getContentResolver().query(
                    FlashCardsContract.Note.CONTENT_URI,
                    null,
                    escapedQuery,
                    null,
                    null
            );

            note_does_not_exist.add(cursor == null || !cursor.moveToFirst());
            if (cursor != null) {
                cursor.close();
            }
        }

        return note_does_not_exist;
    }

    public String[] getNoteFields(long note_id) throws Exception {
        return api.getNote(note_id).getFields();
    }

    public boolean updateNoteFields(long note_id, String[] fields) throws Exception {
        return api.updateNoteFields(note_id, fields);
    }

    public long getNoteModelId(long note_id) throws Exception {
        api.getNote(note_id); //set cursor to correct position
        return api.getCurrentModelId();
    }

    public ArrayList<Long> findNotes(String query) {
        ArrayList<Long> noteIds = new ArrayList<>();

        final Cursor cursor = context.getContentResolver().query(
                FlashCardsContract.Note.CONTENT_URI,
                null,
                query,
                null,
                null
        );

        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                return noteIds;
            }
            for (int i = 0; i < cursor.getCount(); i++) {
                long id = cursor.getLong(0);
                noteIds.add(id);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return noteIds;
    }
}
