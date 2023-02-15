package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import android.database.Cursor;

import com.kamwithk.ankiconnectandroid.routing.database.Entry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JPodAltAudioSource extends LocalAudioSource {
    public JPodAltAudioSource() {
        super("jpod_alternate", "user_files/jpod_alternate_files");
    }

//    @Override
//    protected PreparedStatement prepareQuery(Connection connection, Map<String, List<String>> parameters) throws SQLException {
//        String query = "SELECT file FROM jpod_alternate WHERE (\n" +
//                       "    (expression = ? AND reading = ?)\n" +
//                       "    OR (expression = ? AND reading IS NULL)\n" +
//                       ") ORDER BY priority DESC";
//        String term = getTerm(parameters);
//        String reading = getReading(parameters);
//        PreparedStatement pstmt = connection.prepareStatement(query);
//        // indices start at 1
//        pstmt.setString(1, term);
//        pstmt.setString(2, reading);
//        pstmt.setString(3, term);
//        return pstmt;
//    }

    @Override
    public String getSourceName(Entry entry) {
        return "JPod101 Alt";
    }
}
