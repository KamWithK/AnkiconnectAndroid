package com.kamwithk.ankiconnectandroid.routing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kamwithk.ankiconnectandroid.ankidroid_api.DeckAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.MediaAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.ModelAPI;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class AnkiAPIRouting {
    private final IntegratedAPI integratedAPI;
    private final DeckAPI deckAPI;
    private final ModelAPI modelAPI;
    private final MediaAPI mediaAPI;

    public AnkiAPIRouting(IntegratedAPI integratedAPI) {
        this.integratedAPI = integratedAPI;
        deckAPI = integratedAPI.deckAPI;
        modelAPI = integratedAPI.modelAPI;
        mediaAPI = integratedAPI.mediaAPI;
    }

    private String findRoute(JsonObject raw_json) throws Exception {
        switch (Parser.get_action(raw_json)) {
            case "version":
                return version();
            case "deckNames":
                return deckNames();
            case "deckNamesAndIds":
                return deckNamesAndIds();
            case "modelNames":
                return modelNames();
            case "modelNamesAndIds":
                return modelNamesAndIds();
            case "modelFieldNames":
                return modelFieldNames(raw_json);
            case "findNotes":
                return findNotes();
            case "canAddNotes":
                return canAddNotes(raw_json);
            case "addNote":
                return addNote(raw_json);
            case "storeMediaFile":
                return storeMediaFile(raw_json);
            case "multi":
                JsonArray actions = Parser.getMultiActions(raw_json);
                JsonArray results = new JsonArray();

                for (JsonElement jsonElement : actions) {
                    results.add(Parser.parse(findRoute(jsonElement.getAsJsonObject())));
                }

                return Parser.gson.toJson(results);
            default:
                return default_version();
        }
    }

    public NanoHTTPD.Response findRouteHandleError(JsonObject raw_json) {
        try {
            return returnResponse(findRoute(raw_json));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("result", null);
            response.put("error", e.toString());

            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
        }
    }

    private NanoHTTPD.Response returnResponse(String response) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", response);
    }

    private String version() {
        return "6";
    }

    private String default_version() {
        return "AnkiConnect v.6";
    }

    private String deckNames() throws Exception {
        return Parser.gson.toJson(deckAPI.deckNames());
    }

    private String deckNamesAndIds() throws Exception {
        return Parser.gson.toJson(deckAPI.deckNamesAndIds());
    }

    private String modelNames() throws Exception {
        return Parser.gson.toJson(modelAPI.modelNames());
    }

    private String modelNamesAndIds() throws Exception {
        return Parser.gson.toJson(modelAPI.modelNamesAndIds(0));
    }

    private String modelFieldNames(JsonObject raw_json) throws Exception {
        String model_name = Parser.getModelNameFromParam(raw_json);
        if (model_name != null && !model_name.equals("")) {
            Long model_id = modelAPI.getModelID(model_name, 0);

            return Parser.gson.toJson(modelAPI.modelFieldNames(model_id));
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("result", null);
            response.put("error", "model was not found: ");

            return Parser.gson.toJson(response);
        }
    }

    private String findNotes() {
        return "[]";
    }

//    TODO: Implement
    private String canAddNotes(JsonObject raw_json) throws Exception {
        Map<String, boolean[]> response = new HashMap<>();
        response.put("result", Parser.getNoteTrues(raw_json));
        response.put("error", null);

        return Parser.gson.toJson(response);
    }

    private String addNote(JsonObject raw_json) throws Exception {
        return String.valueOf(integratedAPI.addNote(
                Parser.getNoteValues(raw_json),
                Parser.getDeckName(raw_json),
                Parser.getModelName(raw_json),
                Parser.getNoteTags(raw_json)
        ));
    }

    private String storeMediaFile(JsonObject raw_json) throws Exception {
        Map<String, String> response = new HashMap<>();

        response.put("result", integratedAPI.storeMediaFile(
                Parser.getMediaFilename(raw_json),
                Parser.getMediaData(raw_json)
        ));
        response.put("error", null);

        return Parser.gson.toJson(response);
    }
}