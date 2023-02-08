package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JPodAltAudioSource extends StandardSQLite3Source {
    public JPodAltAudioSource() {
        super("jpod_alternate", "user_files/jpod_alternate_files");
    }

    @Override
    protected PreparedStatement prepareQuery(Connection connection, Map<String, List<String>> parameters) throws SQLException {
        String query = "SELECT DISTINCT file FROM jpod_alt WHERE expression = ? AND reading = ?";
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
        return "JPod101 Alt";
    }
}
