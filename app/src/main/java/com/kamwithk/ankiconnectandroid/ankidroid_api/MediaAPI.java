package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
import com.kamwithk.ankiconnectandroid.BuildConfig;
import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;
import com.kamwithk.ankiconnectandroid.request_parsers.DownloadMediaRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MediaAPI {
    private Context context;
    private final AddContentApi api;

    public MediaAPI(Context context) {
        this.context = context;
        api = new AddContentApi(context);
    }

    /**
     * Stores the given file and returns its name, without the initial slash.
     */
    @SuppressLint("SetWorldReadable")
    public String storeMediaFile(String filename, byte[] data) throws IOException {
        String lastPathSegment = Uri.parse(filename).getLastPathSegment();
        lastPathSegment = lastPathSegment == null ? filename : lastPathSegment;
        File file = new File(context.getCacheDir(), lastPathSegment);

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
        contentValues.put(FlashCardsContract.AnkiMedia.PREFERRED_NAME, lastPathSegment.replaceAll("\\..*", ""));

        ContentResolver contentResolver = context.getContentResolver();
        Uri returnUri = contentResolver.insert(FlashCardsContract.AnkiMedia.CONTENT_URI, contentValues);

//        Remove temporary file
        file.deleteOnExit();

        return new File(returnUri.getPath()).toString().substring(1);
    }

    /**
     * Download the requested audio file from the internet and store it on the disk.
     * @return The path to the audio file.
     */
    public String downloadAndStoreAudioFile(DownloadMediaRequest audioRequest) throws IOException {
        byte[] data = downloadMediaFile(audioRequest.getUrl());
        BinaryFile binaryFile = new BinaryFile();
        binaryFile.setFilename(audioRequest.getFilename());
        binaryFile.setData(data);

        String filePath = storeMediaFile(binaryFile.getFilename(), binaryFile.getData());
        return filePath;
    }

    public byte[] downloadMediaFile(String audioUri) throws IOException {
        URL url = new URL(audioUri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try (InputStream in = conn.getInputStream()) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024 * 5];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                byte[] data = out.toByteArray();
                return data;
            }
        }
    }

}
