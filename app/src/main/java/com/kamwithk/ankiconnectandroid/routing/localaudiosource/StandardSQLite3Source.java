package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import android.net.Uri;
import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class StandardSQLite3Source implements LocalAudioSource {
    protected final String sourceID;

    private final String NETLOC = "localhost:8765";
    private final String mediaDir;

    public StandardSQLite3Source(String sourceID, String mediaDir) {
        this.sourceID = sourceID;
        this.mediaDir = mediaDir;
    }


    // named parameters cannot be used in vanilla jdbc:
    // https://stackoverflow.com/questions/2309970/named-parameters-in-jdbc
    // this is a hack around it in order to not download a huge dependency...
    protected abstract PreparedStatement prepareQuery(Connection connection, Map<String, List<String>> parameters) throws SQLException;

    protected String getSourceName(ResultSet rs) {
        return sourceID;
    }

    protected String getTerm(Map<String, List<String>> parameters) {
        try {
            return parameters.get("term").get(0);
        } catch (NullPointerException e) {
            return parameters.get("expression").get(0);
        }
    }

    protected String getReading(Map<String, List<String>> parameters) {
        return Objects.requireNonNull(parameters.get("reading")).get(0);
    }

    @Override
    public List<Map<String, String>> getSources(Connection connection, Map<String, List<String>> parameters) {

        // formats data as:
        //   [
        //     {
        //       "name": string,
        //       "url": url
        //     }
        //     ...
        //   ]
        ArrayList<Map<String, String>> list = new ArrayList<>();

        // most code taken from the README of https://github.com/xerial/sqlite-jdbc
        try {
            PreparedStatement pstmt = prepareQuery(connection, parameters);
            pstmt.setQueryTimeout(3);  // set timeout to 3 sec.
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {  // reads all results
                String name = getSourceName(rs);
                // hack to convert all file separation markers '\' -> '/' (since entries.db
                // for windows users will store \ to separate directories and files)
                String filePath = rs.getString("file").replace("\\", "/");

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .encodedAuthority(NETLOC)
                        .appendPath("localaudio/")
                        .appendQueryParameter("type", sourceID)
                        .appendQueryParameter("path", filePath);
                String uri = builder.build().toString();

                Map<String, String> map = new HashMap<>();
                map.put("name", name);
                map.put("url", uri);
                list.add(map);
            }

        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            Log.e("AnkiConnectAndroid", "SQLException: " + e.getMessage());
        }

        return list;
    }

    @Override
    public String getMediaDir() {
        return mediaDir;
    }
}
