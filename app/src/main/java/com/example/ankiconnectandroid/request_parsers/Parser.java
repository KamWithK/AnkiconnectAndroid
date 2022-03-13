package com.example.ankiconnectandroid.request_parsers;

import android.util.Base64;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class Parser {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static JsonObject parse(String raw_data) {
        return JsonParser.parseString(raw_data).getAsJsonObject();
    }

    public static String get_action(JsonObject data) {
        return data.get("action").getAsString();
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

    public static boolean[] getNoteTrues(JsonObject raw_data) {
        int num_notes = raw_data.get("params").getAsJsonObject().get("notes").getAsJsonArray().size();
        boolean[] array = new boolean[num_notes];
        Arrays.fill(array, true);

        return array;
    }

    public static String getMediaFilename(JsonObject raw_data) {
        return raw_data.get("params").getAsJsonObject().get("filename").getAsString();
    }

    public static byte[] getMediaData(JsonObject raw_data) {
        String encoded = raw_data.get("params").getAsJsonObject().get("data").getAsString();
        return Base64.decode(encoded, Base64.DEFAULT);
    }
}
