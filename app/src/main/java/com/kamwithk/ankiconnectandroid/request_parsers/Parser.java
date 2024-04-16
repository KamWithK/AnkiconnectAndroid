package com.kamwithk.ankiconnectandroid.request_parsers;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Parser {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    public static Gson gsonNoSerialize = new GsonBuilder().setPrettyPrinting().create();

    private static final String FIELD_SEPARATOR = Character.toString('\u001f');

    public static JsonObject parse(String raw_data) {
        return JsonParser.parseString(raw_data).getAsJsonObject();
    }

    public static String get_action(JsonObject data) {
        return data.get("action").getAsString();
    }

    public static int get_version(JsonObject data, int fallback) {
        if (data.has("version")) {
            return data.get("version").getAsInt();
        }
        return fallback;
    }

    public static String getDeckName(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject().get("deckName").getAsString();
    }

    public static String getModelName(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject().get("modelName").getAsString();
    }

    public static String getModelNameFromParam(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("modelName").getAsString();
    }

    public static Map<String, String> getNoteValues(JsonObject raw_data) {
        Type fieldType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject().get("fields"), fieldType);
    }

    public static Set<String> getNoteTags(JsonObject raw_data) {
        Type fieldType = new TypeToken<Set<String>>() {}.getType();
        return gson.fromJson(raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject().get("tags"), fieldType);
    }

    public static String getNoteQuery(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("query").getAsString();
    }

    public static long getUpdateNoteFieldsId(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject().get("id").getAsLong();
    }

    public static Map<String, String> getUpdateNoteFieldsFields(JsonObject raw_data) {
        Type fieldType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject().get("fields"), fieldType);
    }

    /**
     * For each key ("audio", "video", "picture"), expect EITHER a list or singular json object!
     * According to the official Anki-Connect docs:
     * > If you choose to include [audio, video, picture keys], they should contain a single object
     * > or an array of objects
     */
    public static ArrayList<MediaRequest> getNoteMediaRequests(JsonObject raw_data) {
        Map<String, MediaRequest.MediaType> media_types = Map.of(
            "audio", MediaRequest.MediaType.AUDIO,
            "video", MediaRequest.MediaType.VIDEO,
            "picture", MediaRequest.MediaType.PICTURE
        );
        JsonObject note_json = raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject();

        ArrayList<MediaRequest> request_medias = new ArrayList<>();
        for (Map.Entry<String, MediaRequest.MediaType> entry: media_types.entrySet()) {
            JsonElement media_value = note_json.get(entry.getKey());
            if (media_value == null) {
                continue;
            }
            if (media_value.isJsonArray()) {
                for (JsonElement media_element: media_value.getAsJsonArray()) {
                    JsonObject media_object = media_element.getAsJsonObject();
                    MediaRequest requestMedia = MediaRequest.fromJson(media_object, entry.getValue());
                    request_medias.add(requestMedia);
                }
            } else if (media_value.isJsonObject()) {
                JsonObject media_object = media_value.getAsJsonObject();
                MediaRequest requestMedia = MediaRequest.fromJson(media_object, entry.getValue());
                request_medias.add(requestMedia);
            }
        }
        return request_medias;
    }


    public static class NoteFront {
        private final String fieldName;
        private final String fieldValue;
        private final String modelName;

        public NoteFront(String fieldName, String fieldValue, String modelName) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.modelName = modelName;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldValue() {
            return fieldValue;
        }

        public String getModelName() {
            return modelName;
        }
    }

    /**
     * Gets the first field of the note
     */
    public static ArrayList<NoteFront> getNoteFront(JsonObject raw_data) {
        JsonArray notes = raw_data.get("params").getAsJsonObject().get("notes").getAsJsonArray();
        ArrayList<NoteFront> projections = new ArrayList<>();

        for (JsonElement jsonElement : notes) {
            JsonObject jsonObject = jsonElement.getAsJsonObject().get("fields").getAsJsonObject();

            String field = jsonObject.keySet().toArray()[0].toString();
            String value = jsonObject.get(field).getAsString();
            String model = jsonElement.getAsJsonObject().get("modelName").getAsString();
            NoteFront projection = new NoteFront(field, value, model);
            projections.add(projection);
        }

        return projections;
    }

    public static boolean[] getNoteTrues(JsonObject raw_data) {
        int num_notes = raw_data.get("params").getAsJsonObject().get("notes").getAsJsonArray().size();
        boolean[] array = new boolean[num_notes];
        Arrays.fill(array, true);

        return array;
    }

    public static ArrayList<Long> getNoteIds(JsonObject raw_data) {
        ArrayList<Long> noteIds = new ArrayList<>();
        JsonArray jsonNoteIds = raw_data.get("params").getAsJsonObject().get("notes").getAsJsonArray();
        for(JsonElement noteId: jsonNoteIds) {
            noteIds.add(noteId.getAsLong());
        }
        return noteIds;
    }

    public static String getMediaFilename(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("filename").getAsString();
    }

    public static byte[] getMediaData(JsonObject raw_data) {
        String encoded = raw_data.get("params").getAsJsonObject().get("data").getAsString();
        return Base64.decode(encoded, Base64.DEFAULT);
    }

    public static JsonArray getMultiActions(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("actions").getAsJsonArray();
    }

    // taken from AnkiDroid
    public static String[] splitTags(String tags) {
        if (tags == null) {
            return null;
        }
        return tags.trim().split("\\s+");
    }

    public static String[] splitFields(String fields) {
        return fields != null? fields.split(FIELD_SEPARATOR, -1): null;
    }
}

