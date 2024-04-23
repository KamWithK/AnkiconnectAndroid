package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.*;

import static com.ichi2.anki.api.AddContentApi.READ_WRITE_PERMISSION;

import com.kamwithk.ankiconnectandroid.request_parsers.MediaRequest;
import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;
import com.kamwithk.ankiconnectandroid.request_parsers.NoteRequest;

public class IntegratedAPI {
    private Context context;
    public final DeckAPI deckAPI;
    public final ModelAPI modelAPI;
    public final NoteAPI noteAPI;
    public final MediaAPI mediaAPI;
    private final AddContentApi api; // TODO: Combine all API classes???

    //From anki-connect repo
    private static final String CAN_ADD_ERROR_REASON = "cannot create note because it is a duplicate";
    public IntegratedAPI(Context context) {
        this.context = context;

        deckAPI = new DeckAPI(context);
        modelAPI = new ModelAPI(context);
        noteAPI = new NoteAPI(context);
        mediaAPI = new MediaAPI(context);

        api = new AddContentApi(context);
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

    public ArrayList<Boolean> canAddNotes(ArrayList<NoteRequest> notesToTest) throws Exception {
        final String[] NOTE_PROJECTION = {FlashCardsContract.Note._ID, FlashCardsContract.Note.CSUM};
        final String[] CARD_PROJECTION = {FlashCardsContract.Card.DECK_ID};

        if(notesToTest.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Long> checksums = new ArrayList<>(notesToTest.size());
        ArrayList<Boolean> canAddNote = new ArrayList<>(notesToTest.size());
        NoteRequest.NoteOptions noteOptions = notesToTest.get(0).getOptions();

        for (NoteRequest note: notesToTest) {
            String key = note.getFieldValue();
            checksums.add(Utility.getFieldChecksum(key));
        }

        //If duplicates are allowed, just need to see if they are valid notes (checksum != 0)
        if (noteOptions.isAllowDuplicate()) {
            for (long checksum: checksums) {
                canAddNote.add(checksum != 0);
            }
            return canAddNote;
        }

        //Grabbing the note options and model for the first note and assuming the rest are the same.
        //This is true for yomitan but might not be for other applications.
        String modelName = notesToTest.get(0).getModelName();

        //If duplicate scope is "deck" or "deck root", we need to get extra information to figure out if did matches.
        //If duplicate scope is "deck root" we need to include children, noteOptions.getDeckName() will not be null
        String deckName = noteOptions.getDeckName();
        HashSet<Long> deckIds = new HashSet<>();
        Map<String, Long> deckNamesToIds = deckAPI.deckNamesAndIds();
        if(deckName == null) {
            //Deck, not root
            deckName = notesToTest.get(0).getDeckName();
            deckIds.add(deckNamesToIds.get(deckName));
        }
        else {
            for (String name: deckNamesToIds.keySet()) {
                if (name.contains(deckName)) {
                    deckIds.add(deckNamesToIds.get(name));
                }
            }
        }
        Map<String, Long> modelNameToId = modelAPI.modelNamesAndIds(0);
        Long modelId = modelNameToId.get(modelName);

        String selectionQuery = "";
        if (!noteOptions.isCheckAllModels()) {
            selectionQuery = String.format(
                    Locale.US,
                    "%s=%d and ",
                    FlashCardsContract.Note.MID,
                    modelId
            );
        }
        selectionQuery = selectionQuery + String.format(
                Locale.US,
                "%s in (%s)",
                FlashCardsContract.Note.CSUM,
                TextUtils.join(",", checksums)
        );

        final Cursor cursor = context.getContentResolver().query(
                FlashCardsContract.Note.CONTENT_URI_V2,
                NOTE_PROJECTION,
                selectionQuery,
                null,
                null
        );

        if (cursor == null || cursor.getCount() == 0) {
            for (int i = 0; i < notesToTest.size(); i++) {
                canAddNote.add(true);
            }
        }
        else {
            LinkedHashSet<Long> queryChecksums = new LinkedHashSet<>();
            try (cursor) {
                while (cursor.moveToNext()) {
                    //build list of CSUM (queryChecksums)
                    //if an entry in queryChecksums is in checksums, then we have a duplicate
                    //if scope is "deck", these duplicates need to be checked again for the deck
                    int idIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Note._ID);
                    int csumIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.CSUM);

                    long queryNid = cursor.getLong(idIdx);
                    long queryCsum = cursor.getLong(csumIdx);

                    if (noteOptions.getDuplicateScope().equals("deck")) {
                        //if duplicate scope is "deck" need an additional query
                        Uri noteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, Long.toString(queryNid));
                        Uri cardUri = Uri.withAppendedPath(noteUri, "cards");
                        Cursor cardCursor = context.getContentResolver().query(
                                cardUri,
                                CARD_PROJECTION,
                                null,
                                null,
                                null
                        );

                        if(cardCursor != null) {
                            try (cardCursor) {
                                while(cardCursor.moveToNext()) {
                                    int didIdx = cardCursor.getColumnIndexOrThrow(FlashCardsContract.Card.DECK_ID);
                                    long did = cardCursor.getLong(didIdx);

                                    if (deckIds.contains(did)) {
                                        queryChecksums.add(queryCsum);
                                    }
                                }
                            }
                        }
                    }
                    else {
                        queryChecksums.add(queryCsum);
                    }
                }
            }

            for (int i = 0; i < checksums.size(); i++) {
                if (!queryChecksums.contains(checksums.get(i))) {
                    canAddNote.add(true);
                }
                else {
                    canAddNote.add(false);
                }
            }
        }

        return canAddNote;
    }


    public static class CanAddWithError {
        private final boolean canAdd;
        private final String error;

        public CanAddWithError(boolean canAdd, String error) {
            this.canAdd = canAdd;
            this.error = error;
        }

        public boolean isCanAdd() {
            return canAdd;
        }

        public String getError() {
            return error;
        }
    }

    public List<CanAddWithError> canAddNotesWithErrorDetail(ArrayList<NoteRequest> notesToTest) throws Exception {
        List<CanAddWithError> canAddWithErrorList = new ArrayList<>();
        List<Boolean> canAddList = canAddNotes(notesToTest);

        for (boolean canAdd: canAddList) {
            CanAddWithError canAddWithError;
            if (canAdd) {
                canAddWithError = new CanAddWithError(true, null);
            }
            else {
                canAddWithError = new CanAddWithError(false, CAN_ADD_ERROR_REASON);
            }
            canAddWithErrorList.add(canAddWithError);
        }

        return canAddWithErrorList;
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
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Card added", Toast.LENGTH_SHORT).show());
            return note_id;
        } else {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to add card", Toast.LENGTH_SHORT).show());
            throw new Exception("Couldn't add note");
        }
    }

    /**
     * Adds the media to the collection, and updates noteValues
     *
     * @param noteValues Map from field name to field value
     * @param mediaRequests
     * @throws Exception
     */
    public void addMedia(Map<String, String> noteValues, List<MediaRequest> mediaRequests) throws Exception {
        for (MediaRequest media : mediaRequests) {
            // mediaAPI.storeMediaFile() doesn't store as the passed in filename, need to use the returned one
            Optional<byte[]> data = media.getData();
            Optional<String> url = media.getUrl();
            String stored_filename;
            if (data.isPresent()) {
                stored_filename = mediaAPI.storeMediaFile(media.getFilename(), data.get());
            } else if (url.isPresent()) {
                stored_filename = mediaAPI.downloadAndStoreBinaryFile(media.getFilename(), url.get());
            } else {
                throw new Exception("You must provide a \"data\" or \"url\" field. Note that \"path\" is currently not supported on AnkiConnectAndroid.");
            }

            String enclosed_filename = "";
            switch (media.getMediaType()) {
                case AUDIO:
                case VIDEO:
                    enclosed_filename = "[sound:" + stored_filename + "]";
                    break;
                case PICTURE:
                    enclosed_filename = "<img src=\"" + stored_filename + "\">";
                    break;
            }

            for (String field : media.getFields()) {
                String existingValue = noteValues.get(field);

                if (existingValue == null) {
                    noteValues.put(field, enclosed_filename);
                } else {
                    noteValues.put(field, existingValue + enclosed_filename);
                }
            }
        }
    }

    public void updateNoteFields(long note_id, Map<String, String> newFields, ArrayList<MediaRequest> mediaRequests) throws Exception {
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

        String[] modelFieldNames = modelAPI.modelFieldNames(noteAPI.getNoteModelId(note_id));
        String[] originalFields = noteAPI.getNoteFields(note_id);

        // updated fields
        HashMap<String, String> cardFields = new HashMap<>();

        // Get old fields and update values as needed
        for (int i = 0; i < modelFieldNames.length; i++) {
            String fieldName = modelFieldNames[i];

            String newValue = newFields.get(modelFieldNames[i]);
            if (newValue != null) {
                // Update field to new value
                cardFields.put(fieldName, newValue);
            } else {
                cardFields.put(fieldName, originalFields[i]);
            }
        }

        addMedia(cardFields, mediaRequests);
        noteAPI.updateNoteFields(note_id, cardFields);
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

