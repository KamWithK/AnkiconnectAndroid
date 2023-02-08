package com.kamwithk.ankiconnectandroid.routing;

import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.util.Log;

import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;


public class LocalAudioRouteHandler extends RouterNanoHTTPD.DefaultHandler {
    private LocalAudioAPIRouting routing = null;

    public LocalAudioRouteHandler() {
        super();
    }

    @Override
    public String getText() {
        return "not implemented";
    }

    @Override
    public String getMimeType() {
        return "text/json";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        // setup ???
        // TODO this looks like a hack (same with the main handler!)
        if (routing == null) {
            Context context = uriResource.initParameter(0, Context.class); // ???
            IntegratedAPI integratedAPI = new IntegratedAPI(context);
            File externalFilesDir = integratedAPI.getExternalFilesDir();
            routing = new LocalAudioAPIRouting(externalFilesDir);
        }

        String uri = session.getUri();
        if (uri.equals("/localaudio/get/")) { // get sources
            return routing.getAudioSourcesHandleError(session.getParameters());
        }

        // otherwise, it's getting the actual audio file instead
        // the uri should be of the format: /localaudio/SOURCE/FILE_NAME
        // components should be: ["", "localaudio", SOURCE, FILE_NAME]
        String[] uriComponents = uri.split("/", 4);
        if (uriComponents.length != 4) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Invalid uri: " + uri);
        }
        return routing.getAudioHandleError(uriComponents[2], uriComponents[3]);
    }
}
