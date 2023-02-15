package com.kamwithk.ankiconnectandroid.routing.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "audio_file_entries")
public class AudioFileEntry {
    @PrimaryKey
    public int id;


    @ColumnInfo(name = "file")
    public String file;

    @ColumnInfo(name = "source")
    public String source;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "data")
    public byte[] data;
}

