package com.kamwithk.ankiconnectandroid.routing;

import com.google.gson.JsonObject;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import fi.iki.elonen.NanoHTTPD;

import java.util.*;

public class APIHandler {
    private final AnkiAPIRouting ankiAPIRouting;
    private final ForvoAPIRouting forvoAPIRouting;

    public APIHandler(IntegratedAPI integratedAPI) {
        ankiAPIRouting = new AnkiAPIRouting(integratedAPI);
        forvoAPIRouting = new ForvoAPIRouting();
    }

    public NanoHTTPD.Response chooseAPI(String json_string, Map<String, List<String>> parameters) {
        if ((parameters.containsKey("term") | parameters.containsKey("expression")) & parameters.containsKey("reading")) {
            String reading = Objects.requireNonNull(parameters.get("reading")).get(0);

            return forvoAPIRouting.getAudioHandleError(parameters.get("term"), parameters.get("expression"), reading);
        } else {
            JsonObject raw_json = Parser.parse(json_string);
            return ankiAPIRouting.findRouteHandleError(raw_json);
        }
    }
}
