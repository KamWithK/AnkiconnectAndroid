package com.kamwithk.ankiconnectandroid.routing;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.JPodAudioSource;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.LocalAudioSource;


import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final File externalFilesDir;
    private Map<String, LocalAudioSource> sourceIdToSource;
    public LocalAudioAPIRouting(File externalFilesDir) {
        this.externalFilesDir = externalFilesDir;
        this.sourceIdToSource = new HashMap<>();
        this.sourceIdToSource.put("jpod", new JPodAudioSource());
    }

    public NanoHTTPD.Response getAudioSourcesHandleError(Map<String, List<String>> parameters) {

        List<Map<String, String>> audioSourcesResult = new ArrayList<>();

        // opens database
        try {
            String connectionURI = "jdbc:sqlite:" + externalFilesDir + "/entries.db";
            // for this to not error on android, must add .so files manually
            // https://github.com/xerial/sqlite-jdbc/blob/master/USAGE.md#how-to-use-with-android

            DriverManager.registerDriver((Driver) Class.forName(
                    "org.sqlite.JDBC").newInstance());
            Connection connection = DriverManager.getConnection(connectionURI);

            String[] sources = Objects.requireNonNull(parameters.get("sources")).get(0).split(",");
            for (String source : sources) {
                //Log.d("AnkiConnectAndroid", source + " | " + sourceIdToSource.containsKey(source));
                if (sourceIdToSource.containsKey(source)) {
                    List<Map<String, String>> audioSources = Objects.requireNonNull(sourceIdToSource.get(source)).getSources(connection, parameters);
                    audioSourcesResult.addAll(audioSources);
                }
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            Log.e("AnkiConnectAndroid", e.getMessage());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        Type typeToken = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();

        JsonObject response = new JsonObject();
        response.addProperty("type", "audioSourceList");
        response.add("audioSources", Parser.gson.toJsonTree(audioSourcesResult, typeToken));
        Log.d("AnkiConnectAndroid", "audio sources json: " + Parser.gson.toJson(response));
        Log.d("AnkiConnectAndroid", "audio sources len: " + audioSourcesResult.size());

        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "text/json",
                Parser.gson.toJson(response)
        );
    }

    private NanoHTTPD.Response audioError(String msg) {
        Log.w("AnkiConnectAndroid", msg);
        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.BAD_REQUEST, // 400, mimics python script
                NanoHTTPD.MIME_PLAINTEXT, msg
        );
    }

    public NanoHTTPD.Response getAudioHandleError(String source, String path) {

        if (!sourceIdToSource.containsKey(source)) {
            return audioError("Unknown source: " + source);
        }

        String mediaDir = Objects.requireNonNull(sourceIdToSource.get(source)).getMediaDir();
        String fullPath = externalFilesDir + "/" + mediaDir + "/" + path;

        File f = new File(fullPath);
        if (!f.exists()) {
            return audioError("File does not exist: " + fullPath);
        }

        try {
            // https://stackoverflow.com/questions/858980/file-to-byte-in-java
            // Files.readAllBytes(Path) does not work! error:
            //   Call requires API level 26 (current min is 21): java.nio.file.Files#readAllBytes
            byte[] data = FileUtils.readFileToByteArray(f);

            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
            if (f.toString().endsWith(".mp3")) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "audio/mpeg", new ByteArrayInputStream(data), data.length);
            } else if (f.toString().endsWith(".aac")) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "audio/aac", new ByteArrayInputStream(data), data.length);
            } else {
                return audioError("File is neither a .mp3 or .acc file: " + fullPath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return audioError("File could not be read: " + fullPath);
        }
    }
}
