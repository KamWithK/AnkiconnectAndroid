package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.util.*;

public class NoteAPI {
    private static final String TAG = "NoteAPI";
    private Context context;
    private final ContentResolver resolver;
    private final AddContentApi api;
    private String cachedAuthority = null;

    private static final String[] MODEL_PROJECTION = {FlashCardsContract.Note.MID};
    private static final String[] NOTE_ID_PROJECTION = {FlashCardsContract.Note._ID};
    private static final String[] NOTES_INFO_PROJECTION = {FlashCardsContract.Note._ID, FlashCardsContract.Note.MID, FlashCardsContract.Note.TAGS, FlashCardsContract.Note.FLDS};

    public NoteAPI(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
        api = new AddContentApi(context);
    }

    /**
     * Try to find AnkiDroid's ContentProvider by checking both release and debug authorities
     * Returns the authority string (e.g., "com.ichi2.anki.debug.flashcards")
     */
    private String findAnkiDroidAuthority() {
        if (cachedAuthority != null) {
            return cachedAuthority;
        }

        // First try the standard method
        String packageName = AddContentApi.getAnkiDroidPackageName(context);
        if (packageName != null) {
            // If standard method works, use the standard authority
            cachedAuthority = FlashCardsContract.AUTHORITY;
            Log.d(TAG, "Using standard AnkiDroid authority: " + cachedAuthority);
            return cachedAuthority;
        }

        // If that fails, manually check both release and debug authorities
        PackageManager pm = context.getPackageManager();
        String[] authorities = {
            "com.ichi2.anki.flashcards",           // Release build
            "com.ichi2.anki.debug.flashcards"      // Debug build
        };

        for (String authority : authorities) {
            try {
                android.content.pm.ProviderInfo providerInfo;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    providerInfo = pm.resolveContentProvider(
                        authority,
                        PackageManager.ComponentInfoFlags.of(0L)
                    );
                } else {
                    providerInfo = pm.resolveContentProvider(authority, 0);
                }
                
                if (providerInfo != null) {
                    Log.d(TAG, "Found AnkiDroid ContentProvider with authority: " + authority + ", package: " + providerInfo.packageName);
                    cachedAuthority = authority;
                    return cachedAuthority;
                }
            } catch (Exception e) {
                Log.d(TAG, "Error checking authority " + authority + ": " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Build a note URI with the correct authority
     */
    private Uri getNoteContentUri() {
        String authority = findAnkiDroidAuthority();
        if (authority == null) {
            // Fallback to standard URI if authority not found
            return FlashCardsContract.Note.CONTENT_URI;
        }
        Uri authorityUri = Uri.parse("content://" + authority);
        return Uri.withAppendedPath(authorityUri, "notes");
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

        Uri noteUri = Uri.withAppendedPath(getNoteContentUri(), Long.toString(note_id));
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
                getNoteContentUri(),
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

    static class NoteInfoField {
        private final String value;
        private final int order;

        public NoteInfoField(String value, int order) {
            this.value = value;
            this.order = order;
        }

        public String getValue() {
            return value;
        }

        public int getOrder() {
            return order;
        }
    }
    static class NoteInfo {
        private final long noteId;
        private final String modelName;
        private final List<String> tags;
        private final Map<String, NoteInfoField> fields;

        public NoteInfo(long noteId, String modelName, List<String> tags, Map<String,
                NoteInfoField> fields) {
            this.noteId = noteId;
            this.modelName = modelName;
            this.tags = tags;
            this.fields = fields;
        }

        public long getNoteId() {
            return noteId;
        }

        public String getModelName() {
            return modelName;
        }

        public List<String> getTags() {
            return tags;
        }

        public Map<String, NoteInfoField> getFields() {
            return fields;
        }
    }

    static class Model {
        private final long modelId;
        private final String modelName;
        private final String[] fieldNames;

        public Model(long modelId, String modelName, String[] fieldNames) {
            this.modelId = modelId;
            this.modelName = modelName;
            this.fieldNames = fieldNames;
        }

        public long getModelId() {
            return modelId;
        }

        public String getModelName() {
            return modelName;
        }

        public String[] getFieldNames() {
            return fieldNames;
        }
    }

    public List<NoteInfo> notesInfo(ArrayList<Long> noteIds) throws Exception {
        List<NoteInfo> notesInfoList = new ArrayList<>();
        Map<Long, Model> cache = new HashMap<>();
        Uri baseNoteUri = getNoteContentUri();

        // Query each note individually using NOTES_ID endpoint for reliability
        for (Long noteId : noteIds) {
            try {
                Uri noteUri = Uri.withAppendedPath(baseNoteUri, Long.toString(noteId));
                Log.d(TAG, "Querying note with URI: " + noteUri);
                
                Cursor cursor = this.resolver.query(
                        noteUri,
                        NOTES_INFO_PROJECTION,
                        null,
                        null,
                        null
                );

                if (cursor != null && cursor.moveToFirst()) {
                    try {
                        int idIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Note._ID);
                        int midIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.MID);
                        int tagsIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.TAGS);
                        int fldsIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.FLDS);

                        long id = cursor.getLong(idIdx);
                        long mid = cursor.getLong(midIdx);
                        List<String> tags = Arrays.asList(Utility.splitTags(cursor.getString(tagsIdx)));
                        String[] fieldValues = Utility.splitFields(cursor.getString(fldsIdx));
                        Model model = null;

                        if (cache.containsKey(mid)) {
                            model = cache.get(mid);
                        } else {
                            // Query model directly from ContentProvider using correct authority
                            String authority = findAnkiDroidAuthority();
                            Uri authorityUri = Uri.parse("content://" + authority);
                            Uri modelsUri = Uri.withAppendedPath(authorityUri, "models");
                            Uri modelUri = Uri.withAppendedPath(modelsUri, Long.toString(mid));
                            
                            String[] modelProjection = {
                                FlashCardsContract.Model.NAME,
                                FlashCardsContract.Model.FIELD_NAMES
                            };
                            
                            Cursor modelCursor = resolver.query(modelUri, modelProjection, null, null, null);
                            String modelName = null;
                            String[] fieldNames = null;
                            
                            if (modelCursor != null && modelCursor.moveToFirst()) {
                                try {
                                    int nameIdx = modelCursor.getColumnIndexOrThrow(FlashCardsContract.Model.NAME);
                                    int fieldNamesIdx = modelCursor.getColumnIndexOrThrow(FlashCardsContract.Model.FIELD_NAMES);
                                    
                                    modelName = modelCursor.getString(nameIdx);
                                    String fieldNamesStr = modelCursor.getString(fieldNamesIdx);
                                    fieldNames = Utility.splitFields(fieldNamesStr);
                                } finally {
                                    modelCursor.close();
                                }
                            }
                            
                            if (fieldNames != null && modelName != null) {
                                model = new Model(mid, modelName, fieldNames);
                                cache.put(mid, model);
                            } else {
                                Log.w(TAG, "Could not get model info for modelId: " + mid);
                                continue;
                            }
                        }

                        Map<String, NoteInfoField> fields = new HashMap<>();
                        String[] fieldNames = model.getFieldNames();

                        for (int i = 0; i < fieldNames.length && i < fieldValues.length; i++) {
                            String fieldName = fieldNames[i];
                            String fieldValue = fieldValues[i];
                            NoteInfoField noteInfoField = new NoteInfoField(fieldValue, i);
                            fields.put(fieldName, noteInfoField);
                        }
                        NoteInfo noteInfo = new NoteInfo(id, model.getModelName(), tags, fields);
                        notesInfoList.add(noteInfo);
                    } finally {
                        cursor.close();
                    }
                } else {
                    Log.w(TAG, "Note cursor is null or empty for noteId: " + noteId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting note info for noteId: " + noteId, e);
                // Continue to next note
            }
        }
        return notesInfoList;
    }
}