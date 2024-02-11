package com.kamwithk.ankiconnectandroid.routing;

import android.content.Context;
import android.util.Log;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kamwithk.ankiconnectandroid.Scraper;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class ForvoAPIRouting {
    private final Scraper scraper;

    public ForvoAPIRouting(Context context) {
        scraper = new Scraper(context);
    }

//    Term can also be named expression (older versions)
    private String getTerm(List<String> term, List<String> expression) {
        try {
            return term.get(0);
        } catch (NullPointerException e) {
            return expression.get(0);
        }
    }

    public NanoHTTPD.Response getAudio(String word, String reading) throws IOException {
        ArrayList<HashMap<String, String>> audio_sources = scraper.scrape(word, reading);

        Type typeToken = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();

        JsonObject response = new JsonObject();
        response.addProperty("type", "audioSourceList");
        response.add("audioSources", Parser.gson.toJsonTree(audio_sources, typeToken));

        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "text/json",
                Parser.gson.toJson(response)
        );
    }

    public NanoHTTPD.Response getAudioHandleError(List<String> term, List<String> expression, String reading) {
        try {
            return getAudio(getTerm(term, expression), reading);
        } catch (IOException e) {
            Log.d("Error Scraping", e.toString());

            Map<String, String> response = new HashMap<>();
            response.put("result", null);
            response.put("error", e.toString());

            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
        }
    }
}
