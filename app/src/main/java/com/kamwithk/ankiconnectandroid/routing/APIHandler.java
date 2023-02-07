package com.kamwithk.ankiconnectandroid.routing;

import android.util.Log;

import com.google.gson.JsonObject;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import fi.iki.elonen.NanoHTTPD;

import java.util.*;

public class APIHandler {
    private final AnkiAPIRouting ankiAPIRouting;
    private final ForvoAPIRouting forvoAPIRouting;
    private final LocalAudioAPIRouting localAudioAPIRouting;

    public APIHandler(IntegratedAPI integratedAPI) {
        ankiAPIRouting = new AnkiAPIRouting(integratedAPI);
        forvoAPIRouting = new ForvoAPIRouting();
        localAudioAPIRouting = new LocalAudioAPIRouting();
    }

    public NanoHTTPD.Response chooseAPI(String json_string, String uri, Map<String, List<String>> parameters) {
        if (uri.equals("/localaudio/")) {
            String type = Objects.requireNonNull(parameters.get("type")).get(0);
            if (type.equals("getSources")) {
                return localAudioAPIRouting.getAudioSourcesHandleError(parameters);
            }
            String path = Objects.requireNonNull(parameters.get("path")).get(0);
            return localAudioAPIRouting.getAudioHandleError(type, path);

        } else if ((parameters.containsKey("term") || parameters.containsKey("expression")) && parameters.containsKey("reading")) {
            String reading = Objects.requireNonNull(parameters.get("reading")).get(0);

            return forvoAPIRouting.getAudioHandleError(parameters.get("term"), parameters.get("expression"), reading);
        } else {
            Log.d("AnkiConnectAndroid", "received json: " + json_string);
            JsonObject raw_json = Parser.parse(json_string);
            return ankiAPIRouting.findRouteHandleError(raw_json);
        }
    }
}
