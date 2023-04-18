package com.kamwithk.ankiconnectandroid.request_parsers;

import java.util.ArrayList;

/**
 * Simple class that encodes the media passed into an addNote, updateNoteFields, etc request.
 * Currently very limited; only supports the type, data, filename, and fields[] part of the request.
 */
public class RequestMedia {
    private final RequestMediaTypes type;
    private final byte[] data;
    private final String filename;
    private final ArrayList<String> fields;

    public enum RequestMediaTypes {
        AUDIO,
        VIDEO,
        PICTURE,
    }

    public RequestMedia(RequestMediaTypes type, byte[] data, String filename, ArrayList<String> fields) {
        this.type = type;
        this.data = data;
        this.filename = filename;
        this.fields = fields;
    }

    public RequestMediaTypes getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public String getFilename() {
        return filename;
    }

    public ArrayList<String> getFields() {
        return fields;
    }
}
