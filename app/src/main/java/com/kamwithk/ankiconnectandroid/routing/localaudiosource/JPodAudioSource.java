package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import com.kamwithk.ankiconnectandroid.routing.database.Entry;

public class JPodAudioSource extends LocalAudioSource {
    public JPodAudioSource() {
        super("jpod", "user_files/jpod_files");
    }

    @Override
    public String getSourceName(Entry entry) {
        return "JPod101";
    }
}
