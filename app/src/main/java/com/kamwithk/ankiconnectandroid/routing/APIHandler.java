package com.kamwithk.ankiconnectandroid.routing;

import com.google.gson.JsonObject;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import fi.iki.elonen.NanoHTTPD;
import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class APIHandler {
    private final AnkiAPIRouting ankiAPIRouting;

    public APIHandler(IntegratedAPI integratedAPI) {
        ankiAPIRouting = new AnkiAPIRouting(integratedAPI);
    }

    public NanoHTTPD.Response chooseAPI(String json_string, Map<String, List<String>> parameters) {
        if (parameters.containsKey("expression") &parameters.containsKey("reading")) {
//            TODO: Forvo audio API
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson("Forvo audio will be added in soon..."));
        } else {
            JsonObject raw_json = Parser.parse(json_string);
            return ankiAPIRouting.findRouteHandleError(raw_json);
        }
    }
}
