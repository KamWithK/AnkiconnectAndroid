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
     * Always manually checks both authorities and verifies accessibility to handle
     * Android 11+ package visibility restrictions on Samsung and other devices.
     */
    private String findAnkiDroidAuthority() {
        if (cachedAuthority != null) {
            return cachedAuthority;
        }

        // Always manually check both release and debug authorities
        // Prioritize debug build first since it has the /cards endpoint we need
        // Don't rely on AddContentApi.getAnkiDroidPackageName() as it may not reflect
        // which ContentProvider is actually accessible on Android 11+ devices
        PackageManager pm = context.getPackageManager();
        String[] authorities = {
            "com.ichi2.anki.debug.flashcards",      // Debug build (prioritized - has /cards endpoint)
            "com.ichi2.anki.flashcards"             // Release build (fallback)
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
                    // Verify the ContentProvider is actually accessible by trying a test query
                    // This is important for Android 11+ where package visibility restrictions
                    // may prevent access even if the provider is found
                    Uri testUri = Uri.parse("content://" + authority + "/decks");
                    try {
                        Cursor testCursor = resolver.query(testUri, new String[]{"_id"}, null, null, null);
                        if (testCursor != null) {
                            testCursor.close();
                            Log.d(TAG, "Found accessible AnkiDroid ContentProvider with authority: " + authority + ", package: " + providerInfo.packageName);
                            cachedAuthority = authority;
                            return cachedAuthority;
                        } else {
                            Log.d(TAG, "Authority " + authority + " found but test query returned null cursor");
                        }
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, "Authority " + authority + " found but URI not supported: " + e.getMessage());
                        // Continue to next authority
                    } catch (SecurityException e) {
                        Log.d(TAG, "Authority " + authority + " found but access denied: " + e.getMessage());
                        // Continue to next authority
                    } catch (Exception e) {
                        Log.d(TAG, "Authority " + authority + " found but test query failed: " + e.getMessage());
                        // Continue to next authority
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Error checking authority " + authority + ": " + e.getMessage());
            }
        }

        Log.e(TAG, "Could not find any accessible AnkiDroid ContentProvider authority");
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
        
        try {
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
                    Log.d(TAG, "Successfully queried cards, found " + cardIds.size() + " card IDs");
                } finally {
                    cursor.close();
                }
            } else {
                Log.w(TAG, "Cursor is null for URI: " + cardsUri + ". ContentProvider may not be accessible.");
                // Clear cache to force re-detection next time
                cachedAuthority = null;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "URI not supported: " + cardsUri + ". Error: " + e.getMessage());
            // Clear cache to force re-detection next time
            cachedAuthority = null;
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception accessing ContentProvider: " + cardsUri + ". Error: " + e.getMessage());
            // Clear cache to force re-detection next time
            cachedAuthority = null;
        } catch (Exception e) {
            Log.e(TAG, "Error querying cards: " + e.getMessage(), e);
            // Clear cache to force re-detection next time
            cachedAuthority = null;
        }
        
        return cardIds;
    }

    /**
     * Get card information for a list of card IDs.
     * Returns data in Anki-Connect format for compatibility.
     * Optimized for large collections by batching note lookups.
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
        
        // Step 1: Collect all card data and note IDs
        // Use a map to store card data by cardId, and collect unique note IDs
        Map<Long, Map<String, Object>> cardDataMap = new HashMap<>();
        Set<Long> noteIdsSet = new HashSet<>();
        boolean verboseLogging = cardIds.size() <= 50; // Only log verbosely for small batches
        
        if (verboseLogging) {
            Log.d(TAG, "Processing " + cardIds.size() + " cards");
        } else {
            Log.d(TAG, "Processing " + cardIds.size() + " cards (batch mode, reduced logging)");
        }
        
        // Process cards in batches to avoid memory issues
        int batchSize = 100;
        for (int i = 0; i < cardIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, cardIds.size());
            List<Long> batch = cardIds.subList(i, end);
            
            for (Long cardId : batch) {
                try {
                    // Construct URI manually with the correct authority: content://AUTHORITY/cards/<cardId>
                    Uri authorityUri = Uri.parse("content://" + authority);
                    Uri cardsUri = Uri.withAppendedPath(authorityUri, "cards");
                    Uri cardUri = Uri.withAppendedPath(cardsUri, Long.toString(cardId));
                    
                    if (verboseLogging) {
                        Log.d(TAG, "Querying card info with URI: " + cardUri);
                    }
                    
                    Cursor cursor = resolver.query(cardUri, cardProjection, null, null, null);
                    
                    if (cursor != null && cursor.moveToFirst()) {
                        try {
                            // Get card data
                            int noteIdIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Card.NOTE_ID);
                            long noteId = cursor.getLong(noteIdIdx);
                            
                            // Get card scheduling info from cursor
                            int dueIdx = cursor.getColumnIndex("due");
                            int intervalIdx = cursor.getColumnIndex("interval");
                            int easeFactorIdx = cursor.getColumnIndex("ease_factor");
                            int reviewsIdx = cursor.getColumnIndex("reviews");
                            
                            long due = dueIdx >= 0 ? cursor.getLong(dueIdx) : 0;
                            long interval = intervalIdx >= 0 ? cursor.getLong(intervalIdx) : 0;
                            double factor = easeFactorIdx >= 0 ? cursor.getDouble(easeFactorIdx) : 0.0;
                            int reps = reviewsIdx >= 0 ? cursor.getInt(reviewsIdx) : 0;
                            
                            // Store card data temporarily
                            Map<String, Object> cardData = new HashMap<>();
                            cardData.put("cardId", cardId);
                            cardData.put("noteId", noteId);
                            cardData.put("due", due);
                            cardData.put("interval", interval);
                            cardData.put("factor", factor);
                            cardData.put("reps", reps);
                            
                            cardDataMap.put(cardId, cardData);
                            noteIdsSet.add(noteId);
                            
                            if (verboseLogging) {
                                Log.d(TAG, "Retrieved card data for card " + cardId);
                            }
                        } finally {
                            cursor.close();
                        }
                    } else {
                        if (verboseLogging) {
                            Log.w(TAG, "Card cursor is null or empty for cardId: " + cardId);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting card info for cardId: " + cardId, e);
                    // Skip invalid card IDs - loop will continue naturally
                }
            }
            
            if (!verboseLogging && (i + batchSize < cardIds.size())) {
                Log.d(TAG, "Processed " + (end) + "/" + cardIds.size() + " cards...");
            }
        }
        
        // Step 2: Batch fetch all note info at once
        if (!noteIdsSet.isEmpty()) {
            ArrayList<Long> noteIdList = new ArrayList<>(noteIdsSet);
            if (verboseLogging) {
                Log.d(TAG, "Fetching note info for " + noteIdList.size() + " unique notes");
            } else {
                Log.d(TAG, "Batch fetching note info for " + noteIdList.size() + " unique notes");
            }
            
            List<NoteAPI.NoteInfo> noteInfoList = noteAPI.notesInfo(noteIdList);
            Map<Long, NoteAPI.NoteInfo> noteInfoMap = new HashMap<>();
            for (NoteAPI.NoteInfo noteInfo : noteInfoList) {
                noteInfoMap.put(noteInfo.getNoteId(), noteInfo);
            }
            
            // Step 3: Combine card data with note info
            for (Map.Entry<Long, Map<String, Object>> entry : cardDataMap.entrySet()) {
                Long cardId = entry.getKey();
                Map<String, Object> cardData = entry.getValue();
                Long noteId = (Long) cardData.get("noteId");
                
                NoteAPI.NoteInfo noteInfo = noteInfoMap.get(noteId);
                if (noteInfo != null) {
                    String modelName = noteInfo.getModelName();
                    
                    // Convert NoteInfo fields to Anki-Connect format: Map<String, JsonObject>
                    Map<String, JsonObject> fields = new HashMap<>();
                    Map<String, NoteAPI.NoteInfoField> noteFields = noteInfo.getFields();
                    for (Map.Entry<String, NoteAPI.NoteInfoField> fieldEntry : noteFields.entrySet()) {
                        JsonObject fieldObj = new JsonObject();
                        fieldObj.addProperty("value", fieldEntry.getValue().getValue());
                        fieldObj.addProperty("order", fieldEntry.getValue().getOrder());
                        fields.put(fieldEntry.getKey(), fieldObj);
                    }
                    
                    // Create final card info map matching Anki-Connect format
                    Map<String, Object> cardInfo = new HashMap<>();
                    cardInfo.put("cardId", cardId);
                    cardInfo.put("note", noteId);
                    cardInfo.put("due", cardData.get("due"));
                    cardInfo.put("interval", cardData.get("interval"));
                    cardInfo.put("factor", cardData.get("factor"));
                    cardInfo.put("reps", cardData.get("reps"));
                    cardInfo.put("modelName", modelName);
                    cardInfo.put("fields", fields);
                    
                    cardsInfoList.add(cardInfo);
                } else {
                    if (verboseLogging) {
                        Log.w(TAG, "Could not get note info for noteId: " + noteId);
                    }
                }
            }
        }
        
        Log.d(TAG, "Successfully processed " + cardsInfoList.size() + " cards");
        return cardsInfoList;
    }
}

