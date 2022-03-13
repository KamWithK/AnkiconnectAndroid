package com.example.ankiconnectandroid.ankidroid_api;

import com.ichi2.anki.api.AddContentApi;

import java.util.Map;

import static com.example.ankiconnectandroid.Router.context;

public class NoteAPI {
    private final AddContentApi api;

    public NoteAPI() {
        api = new AddContentApi(context);
    }

    /**
     * Add flashcards to AnkiDroid through instant add API
     * @param data Map of (field name, field value) pairs
     */
    public Long addNote(final Map<String, String> data, Long deck_id, Long model_id) throws Exception {
        String[] all_field_names = api.getFieldList(model_id);
        if (all_field_names == null) {
            throw new Exception("Couldn't get fields");
        }

        // Get list in correct order
        String[] fields = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            fields[i] = data.get(all_field_names[i]);
        }

        return api.addNote(model_id, deck_id, fields, null);
    }
}
