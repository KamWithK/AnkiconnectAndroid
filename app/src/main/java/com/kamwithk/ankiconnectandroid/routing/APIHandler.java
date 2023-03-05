package com.kamwithk.ankiconnectandroid.routing;

import android.content.Context;
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

    public APIHandler(IntegratedAPI integratedAPI, Context context) {
        ankiAPIRouting = new AnkiAPIRouting(integratedAPI);
        forvoAPIRouting = new ForvoAPIRouting();
        localAudioAPIRouting = new LocalAudioAPIRouting(context);
    }

    public NanoHTTPD.Response chooseAPI(String json_string, Map<String, List<String>> parameters) {

        if ((parameters.containsKey("term") || parameters.containsKey("expression")) && parameters.containsKey("reading")) {
            String reading = Objects.requireNonNull(parameters.get("reading")).get(0);

            return forvoAPIRouting.getAudioHandleError(parameters.get("term"), parameters.get("expression"), reading);
        } else {
            Log.d("AnkiConnectAndroid", "received json: " + json_string);
            JsonObject raw_json = Parser.parse(json_string);
            return ankiAPIRouting.findRouteHandleError(raw_json);
        }
    }
}
