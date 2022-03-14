package com.example.ankiconnectandroid.ankidroid_api;

import android.content.Context;
import com.ichi2.anki.api.AddContentApi;

import java.util.HashMap;
import java.util.Map;

public class ModelAPI {
    private final AddContentApi api;

    public ModelAPI(Context context) {
        api = new AddContentApi(context);
    }

    public String[] modelNames() throws Exception {
        Map<Long, String> models = api.getModelList(0);

        if (models != null) {
            return models.values().toArray(new String[0]);
        } else {
            throw new Exception("Couldn't get model names");
        }
    }

    public Map<String, Long> modelNamesAndIds(Integer numFields) throws Exception {
        Map<Long, String> temporary = api.getModelList(numFields);
        Map<String, Long> models = new HashMap<>();

        if (temporary != null) {
            // Reverse hashmap to get entries of (Name, ID)
            for (Map.Entry<Long, String> entry : temporary.entrySet()) {
                models.put(entry.getValue(), entry.getKey());
            }

            return models;
        } else {
            throw new Exception("Couldn't get models names and IDs");
        }
    }

    public String[] modelFieldNames(Long model_id) {
        return api.getFieldList(model_id);
    }

    public Long getModelID(String modelName, Integer numFields) throws Exception {
        Map<String, Long> modelList = modelNamesAndIds(numFields);
        for (Map.Entry<String, Long> entry : modelList.entrySet()) {
            if (entry.getKey().equals(modelName)) {
                return entry.getValue(); // first model wins
            }
        }

        // Can't find model
        throw new Exception("Couldn't get model ID");
    }
}
