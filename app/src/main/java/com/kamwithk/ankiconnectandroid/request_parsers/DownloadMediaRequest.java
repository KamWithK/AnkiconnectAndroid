package com.kamwithk.ankiconnectandroid.request_parsers;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents the request for downloading a single media file. The media can be e.g. audio or an image.
 */
public class DownloadMediaRequest {
    private String url;
    private String filename;
    private String[] fields;

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @NonNull
    public static DownloadMediaRequest fromJson(JsonElement mediaFile) {
        // This is the expected format of the mediaFile:
        // {
        //  "url": "https://www.example.com/audio.mp3",
        //  "filename": "audio_自転車_2023-03-24T15:39:17.151Z",
        //  "fields": [
        //    "Audio"
        //  ]
        // }
        JsonObject mediaObject = mediaFile.getAsJsonObject();
        String url = mediaObject.get("url").getAsString();
        String filename = mediaObject.get("filename").getAsString();
        JsonArray fields = mediaObject.get("fields").getAsJsonArray();

        // convert fields to String[]
        String[] fieldsArray = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            fieldsArray[i] = fields.get(i).getAsString();
        }

        DownloadMediaRequest request = new DownloadMediaRequest();
        request.setUrl(url);
        request.setFilename(filename);
        request.setFields(fieldsArray);
        return request;
    }

}
