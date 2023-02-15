package com.kamwithk.ankiconnectandroid.routing;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    //private final File externalFilesDir;
//    private final EntriesDbOpenHelper entriesDbHelper;
//    private final AndroidDbOpenHelper androidDbHelper;
    private final Context context;

    private final Map<String, LocalAudioSource> sourceIdToSource;
    public LocalAudioAPIRouting(Context context) {
        //this.externalFilesDir = externalFilesDir;
//        this.entriesDbHelper = new EntriesDbOpenHelper(context);
//        this.androidDbHelper = new AndroidDbOpenHelper(context);
        this.context = context;

        this.sourceIdToSource = new HashMap<>();

        this.sourceIdToSource.put("jpod", new JPodAudioSource());
        this.sourceIdToSource.put("jpod_alternate", new JPodAltAudioSource());
        this.sourceIdToSource.put("nhk16", new NHK16AudioSource());
        this.sourceIdToSource.put("forvo", new ForvoAudioSource());
    }

    private EntriesDatabase getDB(String fileName) {
        // TODO global instance?
        File databasePath = new File(context.getExternalFilesDir(null), fileName);
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
        EntriesDatabase db = getDB("entries.db");
        EntryDao entryDao = db.entryDao();

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

        //String[] selectionArgs = { };

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

        //while (cursor.moveToNext()) {
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

        //cursor.close();

        // opens database
//        try {



            //String connectionURI = "jdbc:sqlite:" + externalFilesDir + "/entries.db";
            // for this to not error on android, must add .so files manually
            // https://github.com/xerial/sqlite-jdbc/blob/master/USAGE.md#how-to-use-with-android

            //Connection connection = DriverManager.getConnection(connectionURI);

//            String[] sources = Objects.requireNonNull(parameters.get("sources")).get(0).split(",");
//            for (String source : sources) {
//                //Log.d("AnkiConnectAndroid", source + " | " + sourceIdToSource.containsKey(source));
//                if (sourceIdToSource.containsKey(source)) {
//                    List<Map<String, String>> audioSources = Objects.requireNonNull(sourceIdToSource.get(source)).getSources(connection, parameters);
//                    audioSourcesResult.addAll(audioSources);
//                }
//            }
//        } catch (SQLException e) {
//            // if the error message is "out of memory",
//            // it probably means no database file is found
//            Log.e("AnkiConnectAndroid", e.getMessage());
//        }

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
        String[] sources = Objects.requireNonNull(parameters.get("sources")).get(0).split(",");
        return List.of(sources);
    }


    public NanoHTTPD.Response getAudioHandleError(String source, String path) {
        if (!sourceIdToSource.containsKey(source)) {
            return audioError("Unknown source: " + source);
        }

        EntriesDatabase db = getDB("android.db");
        AudioFileEntryDao audioFileEntryDao = db.audioFileEntryDao();
        byte[] data = audioFileEntryDao.getData(path, source);

        //SQLiteDatabase db = androidDbHelper.getReadableDatabase();

//        String selection = "file = ? AND source = ?";
//        String[] selectionArgs = { path, source };

//        Cursor cursor = db.query(
//                "android",        // The table to query
//                null,                   // The array of columns to return (pass null to get all)
//                selection,              // The columns for the WHERE clause
//                selectionArgs,          // The values for the WHERE clause
//                null,                   // don't group the rows
//                null,                   // don't filter by row groups
//                null                    // The sort order
//        );

//        if (!cursor.moveToNext()) {
//            return audioError("File not found in query: " + path);
//        }
//        byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow("data"));
//        cursor.close();

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
        if (path.endsWith(".mp3")) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "audio/mpeg", new ByteArrayInputStream(data), data.length);
        } else if (path.endsWith(".aac")) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "audio/aac", new ByteArrayInputStream(data), data.length);
        } else {
            return audioError("File is neither a .mp3 or .acc file: " + path);
        }



//        String mediaDir = Objects.requireNonNull(sourceIdToSource.get(source)).getMediaDir();
//        String fullPath = externalFilesDir + "/" + mediaDir + "/" + path;

//        File f = new File(fullPath);
//        if (!f.exists()) {
//            return audioError("File does not exist: " + fullPath);
//        }


//        String connectionURI = "jdbc:sqlite:" + externalFilesDir + "/android.db";
//
//        try {
//            Connection connection = DriverManager.getConnection(connectionURI);
//            String query = "SELECT data FROM android WHERE file = ? AND source = ?";
//            PreparedStatement pstmt = connection.prepareStatement(query);
//            // indices start at 1
//            pstmt.setString(1, path);
//            pstmt.setString(2, source);
//
//            pstmt.setQueryTimeout(3);  // set timeout to 3 sec.
//            ResultSet rs = pstmt.executeQuery();
//            if (!rs.next()) {
//                return audioError("File not found in query: " + path);
//            }
//            byte[] data = rs.getBytes("data");
//
//            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
//            if (path.endsWith(".mp3")) {
//                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "audio/mpeg", new ByteArrayInputStream(data), data.length);
//            } else if (path.endsWith(".aac")) {
//                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "audio/aac", new ByteArrayInputStream(data), data.length);
//            } else {
//                return audioError("File is neither a .mp3 or .acc file: " + path);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return audioError("File could not be read: " + path);
//        }
    }
}
