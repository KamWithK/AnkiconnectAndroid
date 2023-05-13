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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Parser {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

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

    public static ArrayList<RequestMedia> getUpdateNoteFieldsMedia(JsonObject raw_data) {
        Map<String, RequestMedia.RequestMediaTypes> media_types = Map.of(
            "audio", RequestMedia.RequestMediaTypes.AUDIO,
            "video", RequestMedia.RequestMediaTypes.VIDEO,
            "picture", RequestMedia.RequestMediaTypes.PICTURE
        );
        JsonObject note_json = raw_data.get("params").getAsJsonObject().get("note").getAsJsonObject();

        ArrayList<RequestMedia> request_medias = new ArrayList<>();
        for (Map.Entry<String, RequestMedia.RequestMediaTypes> entry: media_types.entrySet()) {
            if (note_json.get(entry.getKey()) == null || !note_json.get(entry.getKey()).isJsonArray()) {
                continue;
            }
            for (JsonElement media_element: note_json.get(entry.getKey()).getAsJsonArray()) {
                JsonObject media_object = media_element.getAsJsonObject();
                request_medias.add(new RequestMedia(
                    entry.getValue(),
                    Base64.decode(media_object.get("data").getAsString(), Base64.DEFAULT),
                    media_object.get("filename").getAsString(),
                    StreamSupport.stream(media_object.get("fields").getAsJsonArray().spliterator(), false)
                        .map(JsonElement::getAsString)
                        .collect(Collectors.toCollection(ArrayList::new))));
            }
        }
        return request_medias;
    }

    public static ArrayList<HashMap<String, String>> getNoteFront(JsonObject raw_data) {
        JsonArray notes = raw_data.get("params").getAsJsonObject().get("notes").getAsJsonArray();
        ArrayList<HashMap<String, String>> first_fields = new ArrayList<>();

        for (JsonElement jsonElement : notes) {
            JsonObject jsonObject = jsonElement.getAsJsonObject().get("fields").getAsJsonObject();

            String field = jsonObject.keySet().toArray()[0].toString();
            String value = jsonObject.get(field).getAsString();

            HashMap<String, String> fields = new HashMap<>();
            fields.put("field", field);
            fields.put("value", value);

            first_fields.add(fields);
        }

        return first_fields;
    }

    public static boolean[] getNoteTrues(JsonObject raw_data) {
        int num_notes = raw_data.get("params").getAsJsonObject().get("notes").getAsJsonArray().size();
        boolean[] array = new boolean[num_notes];
        Arrays.fill(array, true);

        return array;
    }

    public static String getMediaFilename(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("filename").getAsString();
    }

    /**
     * Returns a list of {@link DownloadMediaRequest} objects for audio from the raw_data.
     * They are used to download audio files so that they can be attached into notes.
     * If they are not available in the request, an empty list is returned.
     */
    public static List<DownloadMediaRequest> getDownloadAudioRequests(JsonObject raw_data) {
        try {
            JsonArray jsonAudioFiles = raw_data
                    .get("params").getAsJsonObject()
                    .get("note").getAsJsonObject()
                    .get("audio").getAsJsonArray();

            ArrayList<DownloadMediaRequest> audioRequests = new ArrayList<>();
            for (JsonElement audioFile : jsonAudioFiles) {
                DownloadMediaRequest audioRequest = DownloadMediaRequest.fromJson(audioFile);
                audioRequests.add(audioRequest);
            }
            return audioRequests;
        } catch (NullPointerException e) {
            // valid audio was not provided
            return List.of();
        }
    }

    public static byte[] getMediaData(JsonObject raw_data) {
        String encoded = raw_data.get("params").getAsJsonObject().get("data").getAsString();
        return Base64.decode(encoded, Base64.DEFAULT);
    }

    public static JsonArray getMultiActions(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("actions").getAsJsonArray();
    }
}

