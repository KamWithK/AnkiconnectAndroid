package com.kamwithk.ankiconnectandroid.routing;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Local audio in AnkidroidAndroid works similarly to the original python script found at
 * https://github.com/Aquafina-water-bottle/jmdict-english-yomichan/tree/master/local_audio
 *
 * Here are the differences:
 * - The memory-based version is not supported. Only the SQL version is supported.
 * - The sqlite3 database is NOT dynamically created. This means that the user must copy/paste
 *   the generated entries.db into the correct place within their Android device. The database
 *   is not created dynamically in order to make the process of implementing this feature
 *   as simple as possible.
 * - The URIs are different:
 *   - initial get:
 *     python:  http://localhost:5050/?sources=jpod,jpod_alternate,nhk16,forvo&term={term}&reading={reading}
 *     android: http://localhost:8765/localaudio/?type=getSources&sources=jpod,jpod_alternate,nhk16,forvo&term={term}&reading={reading}
 *   - audio file get:
 *     python:  http://localhost:5050/SOURCE/FILE_PATH_TO_AUDIO_FILE
 *     android: http://localhost:8765/localaudio/?type=SOURCE&path=FILE_PATH_TO_AUDIO_FILE
 */
public class LocalAudioAPIRouting {
    public LocalAudioAPIRouting() {}

    public NanoHTTPD.Response getAudioSourcesHandleError(Map<String, List<String>> parameters) {

        ArrayList<HashMap<String, String>> audio_sources = new ArrayList<>();

        List<String> sources = parameters.get("sources");
        for (String source : sources) {

        }

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

    public NanoHTTPD.Response getAudioHandleError(String type, String path) {
        ArrayList<HashMap<String, String>> audio_sources = new ArrayList<>();

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
}
