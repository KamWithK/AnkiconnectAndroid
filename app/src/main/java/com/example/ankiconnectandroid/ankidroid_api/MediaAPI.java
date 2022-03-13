package com.example.ankiconnectandroid.ankidroid_api;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.example.ankiconnectandroid.BuildConfig;
import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.ankiconnectandroid.Router.context;

public class MediaAPI {
    private final AddContentApi api;

    public MediaAPI() {
        api = new AddContentApi(context);
    }

    @SuppressLint("SetWorldReadable")
    public String storeMediaFile(String filename, byte[] data) throws IOException {
        File file = new File(context.getCacheDir(), Uri.parse(filename).getLastPathSegment());

//        Write to a temporary file
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(data);
        } catch (Exception e) {
            Log.w("Error", e);
            throw e;
        }

        Uri file_uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
        context.grantUriPermission("com.ichi2.anki", file_uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        ContentValues contentValues = new ContentValues();
        contentValues.put(FlashCardsContract.AnkiMedia.FILE_URI, file_uri.toString());
        contentValues.put(FlashCardsContract.AnkiMedia.PREFERRED_NAME, Uri.parse(filename).getLastPathSegment().replaceAll("\\..*", ""));

        ContentResolver contentResolver = context.getContentResolver();
        Uri returnUri = contentResolver.insert(FlashCardsContract.AnkiMedia.CONTENT_URI, contentValues);

//        Remove temporary file
        file.deleteOnExit();

        return new File(returnUri.getPath()).toString().substring(1);
    }
}
