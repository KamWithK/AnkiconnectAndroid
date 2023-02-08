package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class NHK16AudioSource extends StandardSQLite3Source {
    public NHK16AudioSource() {
        super("nhk16", "user_files/nhk16_files");
    }

    @Override
    protected PreparedStatement prepareQuery(Connection connection, Map<String, List<String>> parameters) throws SQLException {
        String query =
                "SELECT display, file FROM nhk16 WHERE expression = ? AND reading = ?" +
                " UNION" +
                "SELECT display, file FROM nhk16 WHERE expression = ?" +
                "   AND NOT EXISTS (SELECT display, file FROM nhk16 WHERE expression = ? AND reading = ?)" +
                " UNION" +
                "SELECT display, file FROM nhk16 WHERE reading = ?" +
                "   AND NOT EXISTS (SELECT display, file FROM nhk16 WHERE expression = ?)";

        String term = getTerm(parameters);
        String reading = getReading(parameters);
        PreparedStatement pstmt = connection.prepareStatement(query);
        // indices start at 1
        pstmt.setString(1, term);
        pstmt.setString(2, reading);
        pstmt.setString(3, term);
        pstmt.setString(4, term);
        pstmt.setString(5, reading);
        pstmt.setString(6, reading);
        pstmt.setString(7, term);
        return pstmt;
    }

    @Override
    protected String getSourceName(ResultSet rs) throws SQLException {
        return "NHK16 " + rs.getString("display");
    }
}
