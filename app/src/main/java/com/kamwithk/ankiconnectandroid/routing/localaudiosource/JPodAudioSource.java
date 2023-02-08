package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JPodAudioSource extends StandardSQLite3Source {
    public JPodAudioSource() {
        super("jpod", "user_files/jpod_files");
    }

    @Override
    protected PreparedStatement prepareQuery(Connection connection, Map<String, List<String>> parameters) throws SQLException {
        String query = "SELECT DISTINCT file FROM jpod WHERE expression = ? AND reading = ?";
        String term = getTerm(parameters);
        String reading = getReading(parameters);
        PreparedStatement pstmt = connection.prepareStatement(query);
        // indices start at 1
        pstmt.setString(1, term);
        pstmt.setString(2, reading);
        return pstmt;
    }

    @Override
    protected String getSourceName(ResultSet rs) {
        return "JPod101";
    }
}
