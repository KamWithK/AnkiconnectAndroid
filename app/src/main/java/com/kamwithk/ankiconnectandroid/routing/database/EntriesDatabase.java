package com.kamwithk.ankiconnectandroid.routing.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Entry.class}, version = 1)
public abstract class EntriesDatabase extends RoomDatabase {
    public abstract EntryDao entryDao();
    public abstract AudioFileEntryDao audioFileEntryDao();
}
