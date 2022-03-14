package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.Context;
import com.ichi2.anki.api.AddContentApi;

import java.util.HashMap;
import java.util.Map;

public class DeckAPI {
    private final AddContentApi api;

    public DeckAPI(Context context) {
        api = new AddContentApi(context);
    }

    public String[] deckNames() throws Exception {
        Map<Long, String> decks = api.getDeckList();

        if (decks != null) {
            return decks.values().toArray(new String[0]);
        } else {
            throw new Exception("Couldn't get deck names");
        }
    }

    public Map<String, Long> deckNamesAndIds() throws Exception {
        Map<Long, String> temporary = api.getDeckList();
        Map<String, Long> decks = new HashMap<>();

        if (temporary != null) {
            // Reverse hashmap to get entries of (Name, ID)
            for (Map.Entry<Long, String> entry : temporary.entrySet()) {
                decks.put(entry.getValue(), entry.getKey());
            }

            return decks;
        } else {
            throw new Exception("Couldn't get deck names and IDs");
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
