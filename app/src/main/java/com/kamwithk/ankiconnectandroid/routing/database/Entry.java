package com.kamwithk.ankiconnectandroid.routing.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "entries")
public class Entry {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "expression")
    public String expression;

    @ColumnInfo(name = "reading")
    public String reading;

    @ColumnInfo(name = "source")
    public String source;

    @ColumnInfo(name = "speaker")
    public String speaker;

    @ColumnInfo(name = "display")
    public String display;

    @ColumnInfo(name = "file")
    public String file;
}
