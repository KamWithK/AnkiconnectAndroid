package com.kamwithk.ankiconnectandroid.routing.localaudiosource;

import com.kamwithk.ankiconnectandroid.routing.database.Entry;

public class ForvoAudioSource extends LocalAudioSource {
    public ForvoAudioSource() {
        super("forvo", "user_files/forvo_files");
    }

    @Override
    public String getSourceName(Entry entry)  {
        return "Forvo (" + entry.speaker + ")";
    }
}
