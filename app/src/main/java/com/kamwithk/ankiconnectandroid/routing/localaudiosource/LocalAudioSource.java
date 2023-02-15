package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import android.database.Cursor;
import android.net.Uri;

import com.kamwithk.ankiconnectandroid.Service;
import com.kamwithk.ankiconnectandroid.routing.database.Entry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LocalAudioSource {
    private final String sourceID;
    private final String mediaDir;

    public LocalAudioSource(String sourceID, String mediaDir) {
        this.sourceID = sourceID;
        this.mediaDir = mediaDir;
    }

    public String getSourceName(Entry entry) {
        return sourceID;
    }

    public String getMediaDir() {
        return mediaDir;
    }

    public String constructFileURL(String filePath) {
        Uri.Builder builder = new Uri.Builder();
        String NETLOC = "localhost:" + Service.PORT;

        builder.scheme("http")
                .encodedAuthority(NETLOC) // encoded to not escape the : character
                .appendPath("localaudio")
                .appendPath(sourceID)
                .appendPath(filePath);
        String uri = builder.build().toString();
        return uri;
    }
}
