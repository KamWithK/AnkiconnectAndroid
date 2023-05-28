package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import com.kamwithk.ankiconnectandroid.routing.database.Entry;

public class Shinmeikai8AudioSource extends LocalAudioSource {
    public Shinmeikai8AudioSource() {
        super("shinmeikai8", "user_files/shinmeikai8_files");
    }

    @Override
    public String getSourceName(Entry entry) {
        return "SMK8 " + entry.display;
    }
}
