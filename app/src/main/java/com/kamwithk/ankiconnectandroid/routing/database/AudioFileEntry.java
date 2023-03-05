package com.kamwithk.ankiconnectandroid.routing.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "android",
    indices= {
        @Index(name="idx_android", value = {"file", "source"}),
    }
)
public class AudioFileEntry {
    @PrimaryKey
    public int id;

    @NonNull
    @ColumnInfo(name = "file")
    public String file;

    @NonNull
    @ColumnInfo(name = "source")
    public String source;

    @NonNull
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "data")
    public byte[] data;
}

