package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.util.HashMap;
import java.util.Map;

public class DeckAPI {
    private static final String TAG = "DeckAPI";
    private final AddContentApi api;
    private final Context context;
    private String cachedAuthority = null;

    public DeckAPI(Context context) {
        this.context = context;
        this.api = new AddContentApi(context);
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
     * Manually query decks using the correct authority
     */
    private Map<Long, String> getDeckListManual() {
        String authority = findAnkiDroidAuthority();
        if (authority == null) {
            return null;
        }

        // Manually construct the URI with the correct authority
        Uri authorityUri = Uri.parse("content://" + authority);
        Uri decksUri = Uri.withAppendedPath(authorityUri, "decks");
        
        Log.d(TAG, "Querying decks with URI: " + decksUri);
        
        Cursor cursor = context.getContentResolver().query(
            decksUri,
            new String[]{FlashCardsContract.Deck.DECK_ID, FlashCardsContract.Deck.DECK_NAME},
            null,
            null,
            null
        );

        if (cursor == null) {
            Log.e(TAG, "Failed to query decks - cursor is null");
            return null;
        }

        Map<Long, String> decks = new HashMap<>();
        try {
            int deckIdIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Deck.DECK_ID);
            int deckNameIdx = cursor.getColumnIndexOrThrow(FlashCardsContract.Deck.DECK_NAME);
            
            while (cursor.moveToNext()) {
                long deckId = cursor.getLong(deckIdIdx);
                String deckName = cursor.getString(deckNameIdx);
                decks.put(deckId, deckName);
            }
            Log.d(TAG, "Successfully retrieved " + decks.size() + " decks manually");
        } catch (Exception e) {
            Log.e(TAG, "Error reading deck list from cursor", e);
            return null;
        } finally {
            cursor.close();
        }

        return decks;
    }

    public String[] deckNames() throws Exception {
        // Check if AnkiDroid is installed
        String authority = findAnkiDroidAuthority();
        if (authority == null) {
            // Try to check if it's installed as a package (even if ContentProvider isn't accessible)
            try {
                context.getPackageManager().getPackageInfo("com.ichi2.anki", 0);
                Log.d(TAG, "AnkiDroid release package found, but ContentProvider not accessible");
            } catch (Exception e) {
                Log.d(TAG, "AnkiDroid release package not found");
            }
            try {
                context.getPackageManager().getPackageInfo("com.ichi2.anki.debug", 0);
                Log.d(TAG, "AnkiDroid debug package found, but ContentProvider not accessible");
            } catch (Exception e) {
                Log.d(TAG, "AnkiDroid debug package not found");
            }
            
            String errorMsg = "AnkiDroid ContentProvider is not accessible. " +
                    "Please ensure:\n" +
                    "1. AnkiDroid is installed on this device/emulator\n" +
                    "2. AnkiconnectAndroid has been uninstalled and reinstalled after adding permissions\n" +
                    "3. AnkiDroid has been opened at least once to initialize its collection\n" +
                    "4. The READ_WRITE_DATABASE permission has been granted\n" +
                    "5. Try restarting both apps";
            Log.e(TAG, errorMsg);
            throw new Exception(errorMsg);
        }
        Log.d(TAG, "Found AnkiDroid authority: " + authority);

        // Use manual query with correct authority instead of api.getDeckList()
        Map<Long, String> decks = getDeckListManual();

        if (decks != null && !decks.isEmpty()) {
            Log.d(TAG, "Successfully retrieved " + decks.size() + " decks");
            return decks.values().toArray(new String[0]);
        } else {
            String errorMsg = "Couldn't get deck names. AnkiDroid authority found: " + authority + 
                    ". Collection may not be initialized. Please open AnkiDroid and wait for it to load.";
            Log.e(TAG, errorMsg);
            throw new Exception(errorMsg);
        }
    }

    public Map<String, Long> deckNamesAndIds() throws Exception {
        // Check if AnkiDroid is installed
        String authority = findAnkiDroidAuthority();
        if (authority == null) {
            String errorMsg = "AnkiDroid is not installed or ContentProvider is not accessible. " +
                    "Please ensure AnkiDroid is installed and running.";
            Log.e(TAG, errorMsg);
            throw new Exception(errorMsg);
        }

        // Use manual query with correct authority instead of api.getDeckList()
        Map<Long, String> temporary = getDeckListManual();
        Map<String, Long> decks = new HashMap<>();

        if (temporary != null && !temporary.isEmpty()) {
            // Reverse hashmap to get entries of (Name, ID)
            for (Map.Entry<Long, String> entry : temporary.entrySet()) {
                decks.put(entry.getValue(), entry.getKey());
            }

            return decks;
        } else {
            String errorMsg = "Couldn't get deck names and IDs. AnkiDroid authority found: " + authority + 
                    ". Collection may not be initialized. Please open AnkiDroid and wait for it to load.";
            Log.e(TAG, errorMsg);
            throw new Exception(errorMsg);
        }
    }

    public Long getDeckID(String name) throws Exception {
        for (Map.Entry<String, Long> entry : deckNamesAndIds().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        // Can't find deck
        throw new Exception("Couldn't get deck ID");
    }
}
