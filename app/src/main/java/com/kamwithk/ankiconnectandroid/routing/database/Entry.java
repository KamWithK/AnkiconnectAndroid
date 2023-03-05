package com.kamwithk.ankiconnectandroid.routing.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "entries",
    indices= { // TODO remove some indices?
        @Index(name="idx_all", value = {"expression", "reading", "source"}),

        @Index(name="idx_reading_speaker", value = {"expression", "reading", "speaker"}),

        @Index(name="idx_expr_reading", value = {"expression", "reading"}),

        @Index(name="idx_speaker", value = {"speaker"}),

        @Index(name="idx_reading", value = {"reading"}),
    }
)
public class Entry {
    @PrimaryKey
    //@NonNull
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "expression")
    public String expression;

    @ColumnInfo(name = "reading")
    public String reading;

    @NonNull
    @ColumnInfo(name = "source")
    public String source;

    @ColumnInfo(name = "speaker")
    public String speaker;

    @ColumnInfo(name = "display")
    public String display;

    @NonNull
    @ColumnInfo(name = "file")
    public String file;
}
