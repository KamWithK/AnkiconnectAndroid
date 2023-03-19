package com.kamwithk.ankiconnectandroid.ankidroid_api;

/** A binary file whose contents have been read into memory */
public class BinaryFile {
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
