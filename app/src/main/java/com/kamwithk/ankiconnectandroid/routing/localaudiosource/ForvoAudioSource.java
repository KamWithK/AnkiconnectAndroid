package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForvoAudioSource extends StandardSQLite3Source {
    public ForvoAudioSource() {
        super("forvo", "user_files/forvo_files");
    }

    @Override
    protected PreparedStatement prepareQuery(Connection connection, Map<String, List<String>> parameters) throws SQLException {
        String term = getTerm(parameters);
        List<String> users = parameters.get("user");
        if (users != null && users.size() > 0) {
            // creates a string of n question marks, joined by a comma (where n = users.size())
            int n = users.size();
            List<String> questionMarksList = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                questionMarksList.add("?");
            }
            String questionMarks = String.join("&", questionMarksList);
            String query = "SELECT speaker,file FROM forvo WHERE expression = ? and speaker IN (" + questionMarks + ") ORDER BY speaker";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, term);
            for (int i = 2; i <= users.size(); i++) {
                pstmt.setString(i, users.get(i-2));
            }
            return pstmt;

        } else {
            String query = "SELECT speaker,file FROM forvo WHERE expression = ? ORDER BY speaker";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, term);
            return pstmt;
        }
    }
}
