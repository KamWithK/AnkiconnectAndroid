package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import com.kamwithk.ankiconnectandroid.routing.database.Entry;

public class NHK16AudioSource extends LocalAudioSource {
    public NHK16AudioSource() {
        super("nhk16", "user_files/nhk16_files");
    }

    @Override
    public String getSourceName(Entry entry) {
        return "NHK16 " + entry.display;
    }
}
