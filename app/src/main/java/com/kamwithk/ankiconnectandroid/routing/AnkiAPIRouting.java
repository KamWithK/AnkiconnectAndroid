package com.kamwithk.ankiconnectandroid.routing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kamwithk.ankiconnectandroid.ankidroid_api.BinaryFile;
import com.kamwithk.ankiconnectandroid.ankidroid_api.DeckAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.MediaAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.ModelAPI;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import com.kamwithk.ankiconnectandroid.request_parsers.MediaRequest;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.util.Log;


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
                return findNotes(raw_json);
            case "guiBrowse":
                return guiBrowse(raw_json);
            case "canAddNotes":
                return canAddNotes(raw_json);
            case "addNote":
                return addNote(raw_json);
            case "updateNoteFields":
                return updateNoteFields(raw_json);
            case "storeMediaFile":
                return storeMediaFile(raw_json);
            case "multi":
                JsonArray actions = Parser.getMultiActions(raw_json);
                JsonArray results = new JsonArray();

                for (JsonElement jsonElement : actions) {
                    int version = Parser.get_version(jsonElement.getAsJsonObject(), 4);
                    String routeResult = findRoute(jsonElement.getAsJsonObject());

                    JsonElement routeResultJson = JsonParser.parseString(routeResult);
                    JsonElement response = formatSuccessReply(routeResultJson, version);
                    results.add(response);
                }

                return Parser.gson.toJson(results);
            default:
                return default_version();
        }
    }
    /* taken from anki-connect's web.py: format_success_reply */
    public JsonElement formatSuccessReply(JsonElement raw_json, int version) {
        if (version <= 4) {
            return raw_json;
        } else {
            JsonObject reply = new JsonObject();
            reply.add("result", raw_json);
            reply.add("error", null);
            return reply;
        }
    }

    public NanoHTTPD.Response findRouteHandleError(JsonObject raw_json) {
        try {
            int version = Parser.get_version(raw_json, 4);
            String response = formatSuccessReply(JsonParser.parseString(findRoute(raw_json)), version).toString();
            Log.d("AnkiConnectAndroid", "response json: " + response);
            return returnResponse(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("result", null);

            StringWriter sw = new StringWriter();
            try {
                try (PrintWriter pw = new PrintWriter(sw)) {
                    e.printStackTrace(pw);
                }
                response.put("error", e.getMessage() + sw);
            } finally {
                try {
                    sw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
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

    private String findNotes(JsonObject raw_json) {
        return Parser.gson.toJson(integratedAPI.noteAPI.findNotes(Parser.getNoteQuery(raw_json)));
    }

    private String guiBrowse(JsonObject raw_json) {
        String query = Parser.getNoteQuery(raw_json);
        return Parser.gson.toJson(integratedAPI.guiBrowse(query));
    }

    private String canAddNotes(JsonObject raw_json) {
        ArrayList<HashMap<String, String>> notes_to_test = Parser.getNoteFront(raw_json);
        return Parser.gson.toJson(integratedAPI.noteAPI.canAddNotes(notes_to_test));
    }

    /**
     * Add a new note to Anki.
     * The note can include media files, which will be downloaded.
     * AnkiConnect desktop also supports other formats, but this method only supports downloadable media files.
     */
    private String addNote(JsonObject raw_json) throws Exception {
        Map<String, String> noteValues = Parser.getNoteValues(raw_json);

        ArrayList<MediaRequest> mediaRequests =
                Parser.getNoteMediaRequests(raw_json);
        integratedAPI.addMedia(noteValues, mediaRequests);

        String noteId = String.valueOf(integratedAPI.addNote(
                noteValues,
                Parser.getDeckName(raw_json),
                Parser.getModelName(raw_json),
                Parser.getNoteTags(raw_json)
        ));

        return noteId;
    }

    private String updateNoteFields(JsonObject raw_json) throws Exception {
        integratedAPI.updateNoteFields(
                Parser.getUpdateNoteFieldsId(raw_json),
                Parser.getUpdateNoteFieldsFields(raw_json),
                Parser.getNoteMediaRequests(raw_json)
        );
        return "null";
    }

    private String storeMediaFile(JsonObject raw_json) throws Exception {
        BinaryFile binaryFile = new BinaryFile();
        binaryFile.setFilename(Parser.getMediaFilename(raw_json));
        binaryFile.setData(Parser.getMediaData(raw_json));

        return Parser.gson.toJson(integratedAPI.storeMediaFile(binaryFile));
    }
}