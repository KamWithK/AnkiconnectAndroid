package com.example.ankiconnectandroid;

import android.content.Context;
import android.util.Log;
import com.example.ankiconnectandroid.ankidroid_api.DeckAPI;
import com.example.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.example.ankiconnectandroid.ankidroid_api.MediaAPI;
import com.example.ankiconnectandroid.ankidroid_api.ModelAPI;
import com.example.ankiconnectandroid.request_parsers.Parser;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.HashMap;
import java.util.Map;

import static com.example.ankiconnectandroid.Router.contentType;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class RouteHandler extends RouterNanoHTTPD.DefaultHandler {

    private IntegratedAPI integratedAPI = null;
    private DeckAPI deckAPI;
    private ModelAPI modelAPI;
    private MediaAPI mediaAPI;

    public RouteHandler() {
        super();
    }
    @Override
    public String getText() {
        return null;
    }

    @Override
    public String getMimeType() {
        return "text/json";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        Context context = uriResource.initParameter(0, Context.class);

//        Setup
        if (integratedAPI == null) {
            integratedAPI = new IntegratedAPI(context);
            deckAPI = integratedAPI.deckAPI;
            modelAPI = integratedAPI.modelAPI;
            mediaAPI = integratedAPI.mediaAPI;
        }

//        Enforce UTF-8 encoding (response doesn't always contain by default)
        session.getHeaders().put("content-type", contentType);

        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
            JsonObject raw_json = Parser.parse(files.get("postData"));
            switch (Parser.get_action(raw_json)) {
                case "version":
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", "6");
                case "deckNames":
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(deckAPI.deckNames()));
                case "deckNamesAndIds":
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(deckAPI.deckNamesAndIds()));
                case "modelNames":
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(modelAPI.modelNames()));
                case "modelNamesAndIds":
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(modelAPI.modelNamesAndIds(0)));
                case "modelFieldNames":
                    String model_name = Parser.getModelNameFromParam(raw_json);
                    if (model_name != null && !model_name.equals("")) {
                        Long model_id = modelAPI.getModelID(model_name, 0);
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(modelAPI.modelFieldNames(model_id)));
                    } else {
                        Map<String, String> response = new HashMap<>();
                        response.put("result", null);
                        response.put("error", "model was not found: ");

                        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
                    }
                case "canAddNotes": // Always respond yes
                    Map<String, boolean[]> response = new HashMap<>();
                    response.put("result", Parser.getNoteTrues(raw_json));
                    response.put("error", null);

                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
                case "addNote":
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", String.valueOf(integratedAPI.addNote(Parser.getNoteValues(raw_json), Parser.getDeckName(raw_json), Parser.getModelName(raw_json))));
                case "storeMediaFile":
                    Map<String, String> response_ = new HashMap<>();

                    response_.put("result", integratedAPI.storeMediaFile(Parser.getMediaFilename(raw_json), Parser.getMediaData(raw_json)));
                    response_.put("error", null);

                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response_));
                default:
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", "AnkiConnect v.6");
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("result", null);
            response.put("error", e.toString());

            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
        }
    }
}
