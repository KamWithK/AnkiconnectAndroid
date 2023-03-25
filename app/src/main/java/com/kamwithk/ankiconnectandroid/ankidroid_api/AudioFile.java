package com.kamwithk.ankiconnectandroid.ankidroid_api;

/** An audio file whose contents have been read into memory */
public class AudioFile {
    private String filename;
    private byte[] data;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
