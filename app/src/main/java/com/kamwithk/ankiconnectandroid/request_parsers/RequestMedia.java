package com.kamwithk.ankiconnectandroid.request_parsers;

import java.util.ArrayList;

/**
 * Simple class that encodes the media passed into an addNote, updateNoteFields, etc request.
 * Currently very limited; only supports the type, data, filename, and fields[] part of the request.
 *
 * stored is metadata for whether it has already been stored in the Anki media folder. Used in
 * combination with setFilename() to ensure the file is only stored once despite
 * mediaAPI.storeMediaFile() not saving under the provided filename at the moment.
 */
public class RequestMedia {
    private final RequestMediaTypes type;
    private final byte[] data;
    private String filename;
    private final ArrayList<String> fields;
    private boolean stored;

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
        this.stored = false;
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public boolean isStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }
}
