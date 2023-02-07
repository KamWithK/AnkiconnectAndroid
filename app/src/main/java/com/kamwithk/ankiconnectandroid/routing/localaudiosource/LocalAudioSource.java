package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface LocalAudioSource {
    List<Map<String, String>> getSources(Connection connection, Map<String, List<String>> parameters);
    String getMediaDir();
}
