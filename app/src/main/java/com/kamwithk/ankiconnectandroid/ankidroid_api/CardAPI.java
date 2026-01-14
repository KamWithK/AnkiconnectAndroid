package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.util.*;

public class CardAPI {
    private static final String TAG = "CardAPI";
    private final ContentResolver resolver;
    private final AddContentApi api;
    private final NoteAPI noteAPI;
    private final Context context;
    private String cachedAuthority = null;

    public CardAPI(Context context, NoteAPI noteAPI) {
        this.context = context;
        this.resolver = context.getContentResolver();
        this.api = new AddContentApi(context);
        this.noteAPI = noteAPI;
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
     * Find cards using a search query.
     * Uses Ankidroid's ContentProvider cards endpoint with Collection.findCards()
     * 
     * @param query The search query (same syntax as Anki browser)
     * @return List of card IDs matching the query
     */
    public List<Long> findCards(String query) {
        List<Long> cardIds = new ArrayList<>();
        
        // Find the correct authority (release or debug)
        String authority = findAnkiDroidAuthority();
        if (authority == null) {
            Log.e(TAG, "Could not find AnkiDroid ContentProvider authority");
            return cardIds;
        }
        
        // Use the new cards ContentProvider endpoint
        // The selection parameter contains the search query
        // Construct URI manually with the correct authority: content://AUTHORITY/cards
        Uri authorityUri = Uri.parse("content://" + authority);
        Uri cardsUri = Uri.withAppendedPath(authorityUri, "cards");
        Log.d(TAG, "Querying cards with URI: " + cardsUri);
        
        Cursor cursor = resolver.query(
                cardsUri,
                new String[]{"_id"},
                query,
                null,
                null
        );
        
        if (cursor != null) {
            try {
                int idColumnIndex = cursor.getColumnIndexOrThrow("_id");
                while (cursor.moveToNext()) {
                    long cardId = cursor.getLong(idColumnIndex);
                    cardIds.add(cardId);
                }
            } finally {
                cursor.close();
            }
        }
        
        return cardIds;
    }

    /**
     * Get card information for a list of card IDs.
     * Returns data in Anki-Connect format for compatibility.
     * 
     * @param cardIds List of card IDs
     * @return List of card info maps (serialized as Map for JSON compatibility)
     */
    public List<Map<String, Object>> cardsInfo(List<Long> cardIds) throws Exception {
        List<Map<String, Object>> cardsInfoList = new ArrayList<>();
        
        if (cardIds == null || cardIds.isEmpty()) {
            return cardsInfoList;
        }
        
        // Find the correct authority (release or debug)
        String authority = findAnkiDroidAuthority();
        if (authority == null) {
            Log.e(TAG, "Could not find AnkiDroid ContentProvider authority");
            return cardsInfoList;
        }
        
        // Request all card columns including scheduling fields
        String[] cardProjection = {
            "_id",           // Card ID (using string literal since constant not available)
            FlashCardsContract.Card.NOTE_ID,
            FlashCardsContract.Card.CARD_ORD,
            FlashCardsContract.Card.CARD_NAME,
            FlashCardsContract.Card.DECK_ID,
            FlashCardsContract.Card.QUESTION,
            FlashCardsContract.Card.ANSWER,
            "due",           // Scheduling fields
            "interval",
            "ease_factor",
            "reviews"
        };
        
        // For each card ID, query the card info
        for (Long cardId : cardIds) {
            try {
                // Construct URI manually with the correct authority: content://AUTHORITY/cards/<cardId>
                Uri authorityUri = Uri.parse("content://" + authority);
                Uri cardsUri = Uri.withAppendedPath(authorityUri, "cards");
                Uri cardUri = Uri.withAppendedPath(cardsUri, Long.toString(cardId));
                Log.d(TAG, "Querying card info with URI: " + cardUri);
                
                Cursor cursor = resolver.query(cardUri, cardProjection, null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    try {
                        // Get card data
                        int noteIdIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Card.NOTE_ID);
                        long noteId = cursor.getLong(noteIdIdx);
                        
                        // Get card scheduling info from cursor (these should be available now)
                        int dueIdx = cursor.getColumnIndex("due");
                        int intervalIdx = cursor.getColumnIndex("interval");
                        int easeFactorIdx = cursor.getColumnIndex("ease_factor");
                        int reviewsIdx = cursor.getColumnIndex("reviews");
                        
                        long due = dueIdx >= 0 ? cursor.getLong(dueIdx) : 0;
                        long interval = intervalIdx >= 0 ? cursor.getLong(intervalIdx) : 0;
                        double factor = easeFactorIdx >= 0 ? cursor.getDouble(easeFactorIdx) : 0.0;
                        int reps = reviewsIdx >= 0 ? cursor.getInt(reviewsIdx) : 0;
                        
                        // Get note info to get fields and model name (using correct authority via NoteAPI)
                        ArrayList<Long> noteIdList = new ArrayList<>();
                        noteIdList.add(noteId);
                        List<NoteAPI.NoteInfo> noteInfoList = noteAPI.notesInfo(noteIdList);
                        
                        if (!noteInfoList.isEmpty()) {
                            NoteAPI.NoteInfo noteInfo = noteInfoList.get(0);
                            String modelName = noteInfo.getModelName();
                            
                            // Convert NoteInfo fields to Anki-Connect format: Map<String, JsonObject>
                            // where each JsonObject has "value" property
                            Map<String, JsonObject> fields = new HashMap<>();
                            Map<String, NoteAPI.NoteInfoField> noteFields = noteInfo.getFields();
                            for (Map.Entry<String, NoteAPI.NoteInfoField> entry : noteFields.entrySet()) {
                                JsonObject fieldObj = new JsonObject();
                                fieldObj.addProperty("value", entry.getValue().getValue());
                                fieldObj.addProperty("order", entry.getValue().getOrder());
                                fields.put(entry.getKey(), fieldObj);
                            }
                            
                            // Create card info map matching Anki-Connect format
                            Map<String, Object> cardInfo = new HashMap<>();
                            cardInfo.put("cardId", cardId);
                            cardInfo.put("note", noteId);
                            cardInfo.put("due", due);
                            cardInfo.put("interval", interval);
                            cardInfo.put("factor", factor);
                            cardInfo.put("reps", reps);
                            cardInfo.put("modelName", modelName);
                            cardInfo.put("fields", fields);
                            
                            cardsInfoList.add(cardInfo);
                            Log.d(TAG, "Successfully retrieved info for card " + cardId);
                        } else {
                            Log.w(TAG, "Could not get note info for noteId: " + noteId);
                        }
                    } finally {
                        cursor.close();
                    }
                } else {
                    Log.w(TAG, "Card cursor is null or empty for cardId: " + cardId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting card info for cardId: " + cardId, e);
                // Skip invalid card IDs - loop will continue naturally
            }
        }
        
        return cardsInfoList;
    }
}

