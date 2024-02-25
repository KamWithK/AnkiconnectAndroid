package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;

import java.util.*;

public class NoteAPI {
    private Context context;
    private final ContentResolver resolver;
    private final AddContentApi api;

    private static final String[] MODEL_PROJECTION = {FlashCardsContract.Note.MID};
    private static final String[] NOTE_ID_PROJECTION = {FlashCardsContract.Note._ID};

    public NoteAPI(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
        api = new AddContentApi(context);
    }

    static String escapeQueryStr(String s) {
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
        String[] allFieldNames = api.getFieldList(model_id);
        if (allFieldNames == null) {
            throw new Exception("Couldn't get fields");
        }

        // Get list in correct order
        String[] fields = new String[allFieldNames.length];

        for (int i = 0; i < allFieldNames.length; i++) {
            fields[i] = data.getOrDefault(allFieldNames[i], "");
        }

        return api.addNote(model_id, deck_id, fields, tags);
    }

    public String[] getNoteFields(long note_id) throws Exception {
        return api.getNote(note_id).getFields();
    }

    public boolean updateNoteFields(long note_id, final Map<String, String> data) throws Exception {
        long modelId = getNoteModelId(note_id);
        String[] allFieldNames = api.getFieldList(modelId);
        if (allFieldNames == null) {
            throw new Exception("Couldn't get fields");
        }

        // Get list in correct order
        String[] fields = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            fields[i] = data.get(allFieldNames[i]);
        }

        return api.updateNoteFields(note_id, fields);
    }

    public Long getNoteModelId(long note_id) {
        // Manually queries the note with a specific projection to get the model ID
        // Code copied/pasted from getNote() in AddContentAPI:
        // https://github.com/ankidroid/Anki-Android/blob/1711e56c2b5515ab89c3424b60e60867bb65d492/api/src/main/java/com/ichi2/anki/api/AddContentApi.kt#L244

        Uri noteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(note_id));
        Cursor cursor = this.resolver.query(noteUri, MODEL_PROJECTION, null, null, null);

        if (cursor == null) {
            return null;
        }
        try {
            if (!cursor.moveToNext()) {
                return null;
            }
            int index = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.MID);;
            return cursor.getLong(index); // mid
        } finally {
            cursor.close();
        }
    }

    public ArrayList<Long> findNotes(String query) {
        ArrayList<Long> noteIds = new ArrayList<>();

        final Cursor cursor = this.resolver.query(
                FlashCardsContract.Note.CONTENT_URI,
                NOTE_ID_PROJECTION,
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
