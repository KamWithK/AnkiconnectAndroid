package com.kamwithk.ankiconnectandroid.routing.database;

import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface EntryDao {
    // https://stackoverflow.com/questions/44287465/how-to-dynamically-query-the-room-database-at-runtime
    @RawQuery
    List<Entry> getSources(SupportSQLiteQuery query);
}
