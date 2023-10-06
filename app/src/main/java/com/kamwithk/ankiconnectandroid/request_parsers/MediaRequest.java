package com.kamwithk.ankiconnectandroid.request_parsers;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simple class that encodes the media passed into an addNote, updateNoteFields, etc request.
 * Currently very limited; only supports the type, data, filename, and fields[] part of the request.
 */
public class MediaRequest {
    private final MediaType mediaType;
    private final String filename;
    private final ArrayList<String> fields;

    private Optional<byte[]> data = Optional.empty();
    private Optional<String> url = Optional.empty();

    public enum MediaType {
        AUDIO,
        VIDEO,
        PICTURE,
    }

    public MediaRequest(MediaType mediaType, String filename, ArrayList<String> fields) {
        this.mediaType = mediaType;
        this.filename = filename;
        this.fields = fields;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
    
    public String getFilename() {
        return filename;
    }

    public List<String> getFields() {
        return fields;
    }

    public Optional<byte[]> getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = Optional.of(data);
    }

    public void setUrl(String url) {
        this.url = Optional.of(url);
    }
    public Optional<String> getUrl() {
        return url;
    }



    @NonNull
    public static MediaRequest fromJson(JsonElement mediaFile, MediaType mediaType) {
        // This is the expected format of the mediaFile:
        // {
        //  "url": "https://www.example.com/audio.mp3",
        //  "filename": "audio_自転車_2023-03-24T15:39:17.151Z",
        //  "fields": [
        //    "Audio"
        //  ]
        // }
        JsonObject mediaObject = mediaFile.getAsJsonObject();

        String filename = mediaObject.get("filename").getAsString();
        JsonArray fields = mediaObject.get("fields").getAsJsonArray();

        // convert fields to String[]
        ArrayList<String> fieldsList = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            fieldsList.add(fields.get(i).getAsString());
        }

        MediaRequest request = new MediaRequest(mediaType, filename, fieldsList);

        if (mediaObject.has("url")) {
            request.setUrl(mediaObject.get("url").getAsString());
        }

        if (mediaObject.has("data")) {
            request.setData(Base64.decode(mediaObject.get("data").getAsString(), Base64.DEFAULT));
        }

        return request;
    }
}
