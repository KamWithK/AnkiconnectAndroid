package com.kamwithk.ankiconnectandroid.routing;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import com.kamwithk.ankiconnectandroid.routing.database.AudioFileEntryDao;
import com.kamwithk.ankiconnectandroid.routing.database.EntriesDatabase;
import com.kamwithk.ankiconnectandroid.routing.database.Entry;
import com.kamwithk.ankiconnectandroid.routing.database.EntryDao;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.ForvoAudioSource;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.JPodAltAudioSource;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.JPodAudioSource;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.LocalAudioSource;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.NHK16AudioSource;
import com.kamwithk.ankiconnectandroid.routing.localaudiosource.Shinmeikai8AudioSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

/**
 * Local audio in AnkidroidAndroid works similarly to the original python script found at
 * https://github.com/Aquafina-water-bottle/jmdict-english-yomichan/tree/master/local_audio
 *
 * Here are the main differences:
 * - The memory-based version is not supported. Only the SQL version is supported.
 * - The SQLite3 database is *NOT* dynamically created. This means that the user must copy/paste
 *   the generated entries.db into the correct place within their Android device. The database
 *   is not created dynamically in order to make the process of implementing this feature
 *   as simple as possible.
 * - The URIs are different:
 *   - initial get:
 *     python:  http://localhost:5050/?sources=jpod,jpod_alternate,nhk16,forvo&term={term}&reading={reading}
 *     android: http://localhost:8765/localaudio/get/&sources=jpod,jpod_alternate,nhk16,forvo&term={term}&reading={reading}
 *   - audio file get:
 *     python:  http://localhost:5050/SOURCE/FILE_PATH_TO_AUDIO_FILE
 *     android: http://localhost:8765/localaudio/SOURCE/FILE_PATH_TO_AUDIO_FILE
 *  - NHK98 is not supported (because the audio files aren't available for the original anyways)
 */
public class LocalAudioAPIRouting {
    private final Context context;

    // sourceIdToSource is a LinkedHashMap to preserve insertion order
    private final LinkedHashMap<String, LocalAudioSource> sourceIdToSource;

    public LocalAudioAPIRouting(Context context) {
        this.context = context;

        // TODO: read config
        this.sourceIdToSource = new LinkedHashMap<>();
        this.sourceIdToSource.put("nhk16", new NHK16AudioSource());
        this.sourceIdToSource.put("shinmeikai8", new Shinmeikai8AudioSource());
        this.sourceIdToSource.put("forvo", new ForvoAudioSource());
        this.sourceIdToSource.put("jpod", new JPodAudioSource());
        this.sourceIdToSource.put("jpod_alternate", new JPodAltAudioSource());
    }

    private EntriesDatabase getDB() {
        // TODO global instance?
        File databasePath = new File(context.getExternalFilesDir(null), "android.db");
        EntriesDatabase db = Room.databaseBuilder(context,
                EntriesDatabase.class, databasePath.toString()).build();
        return db;
    }

    public NanoHTTPD.Response getAudioSourcesHandleError(Map<String, List<String>> parameters) {

        String term = getTerm(parameters);
        String reading = getReading(parameters);
        List<String> sources = getSources(parameters);
        List<String> users = getUser(parameters);

        List<Map<String, String>> audioSourcesResult = new ArrayList<>();
        List<String> args = new ArrayList<>();


        // opens database (creates if doesn't exist)
        EntriesDatabase db = getDB();
        EntryDao entryDao = db.entryDao();

        // query generator based off of the original plugin:
        // https://github.com/Aquafina-water-bottle/local-audio-yomichan/blob/master/plugin/db_utils.py
        // Filter results WHERE "title" = 'My Title'
        String selection = "expression = ?\n" +
                "AND (reading IS NULL OR reading = ?)\n";
        args.add(term);
        args.add(reading);

        // filters by sources if necessary
        if (sources.size() != sourceIdToSource.size()) {
            String nQuestionMarks = String.join(",", Collections.nCopies(sources.size(), "?"));
            selection += "AND (source in (" + nQuestionMarks + "))\n";
            args.addAll(sources);
        }

        // filters by speakers if necessary
        if (users.size() > 0) {
            String nQuestionMarks = String.join(",", Collections.nCopies(users.size(), "?"));
            selection += "AND (speaker IS NULL or speaker in (" + nQuestionMarks + "))\n";
            args.addAll(users);
        }

        // How you want the results sorted in the resulting Cursor
        // order by source
        StringBuilder sortOrder = new StringBuilder("(CASE source ");
        for (int i = 0; i < sources.size(); i++) {
            sortOrder.append("WHEN ? THEN ").append(i).append("\n");
            args.add(sources.get(i));
        }
        sortOrder.append(" END)\n");

        // order by speakers if necessary
        if (users.size() > 0) {
            sortOrder.append(", (CASE speaker ");
            for (int i = 0; i < users.size(); i++) {
                sortOrder.append("WHEN ? THEN ").append(i).append("\n");
                args.add(users.get(i));
            }
            sortOrder.append(" END)\n");
        }

        String queryString = "\n" +
                "SELECT * FROM entries WHERE (" + selection + ")\n" +
                "ORDER BY " + sortOrder + ", reading;";

        SimpleSQLiteQuery query = new SimpleSQLiteQuery(queryString, args.toArray());
        List<Entry> entries = entryDao.getSources(query);

        for (Entry entry : entries) {
            String source = entry.source;
            String file = entry.file;

            LocalAudioSource audioSource = sourceIdToSource.get(entry.source);
            if (audioSource == null) {
                Log.w("AnkiConnectAndroid", "Unknown audio source: " + source);
                continue;
            }

            String name = audioSource.getSourceName(entry);
            String url = audioSource.constructFileURL(file);

            Map<String, String> audioSourceEntry = new HashMap<>();
            audioSourceEntry.put("name", name);
            audioSourceEntry.put("url", url);

            audioSourcesResult.add(audioSourceEntry);
        }

        Type typeToken = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();

        JsonObject response = new JsonObject();
        response.addProperty("type", "audioSourceList");
        response.add("audioSources", Parser.gson.toJsonTree(audioSourcesResult, typeToken));
        Log.d("AnkiConnectAndroid", "audio sources json: " + Parser.gson.toJson(response));

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

    private String getTerm(Map<String, List<String>> parameters) {
        try {
            return parameters.get("term").get(0);
        } catch (NullPointerException e) {
            return parameters.get("expression").get(0);
        }
    }

    private String getReading(Map<String, List<String>> parameters) {
        return Objects.requireNonNull(parameters.get("reading")).get(0);
    }

    private List<String> getUser(Map<String, List<String>> parameters) {
        List<String> _user = parameters.get("user");
        List<String> users = new ArrayList<>();
        if (_user != null && _user.size() > 0) {
            users = List.of(_user.get(0).split(","));
        }
        return users;
    }

    private List<String> getSources(Map<String, List<String>> parameters) {
        List<String> sources = parameters.get("sources");
        if (sources != null && sources.size() == 1) {
            return List.of(sources.get(0).split(","));
        }

        // default order
        return new ArrayList<>(sourceIdToSource.keySet());
    }

    public NanoHTTPD.Response getAudioHandleError(String source, String path) {
        if (!sourceIdToSource.containsKey(source)) {
            return audioError("Unknown source: " + source);
        }

        EntriesDatabase db = getDB();
        AudioFileEntryDao audioFileEntryDao = db.audioFileEntryDao();
        byte[] data = audioFileEntryDao.getData(path, source);

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
        String mimeType = null;
        if (path.endsWith(".mp3")) {
            mimeType = "audio/mpeg";
        } else if (path.endsWith(".aac")) {
            mimeType = "audio/aac";
        } else if (path.endsWith(".m4a")) {
            mimeType = "audio/mp4";
        } else if (path.endsWith(".ogg") || path.endsWith(".oga") || path.endsWith(".opus")) {
            mimeType = "audio/ogg";
        } else if (path.endsWith(".flac")) {
            mimeType = "audio/flac";
        } else if (path.endsWith(".wav")) {
            mimeType = "audio/wav";
        }
        if (mimeType == null) {
            return audioError("File is not a supported audio file: " + path);
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType, new ByteArrayInputStream(data), data.length);

    }
}
