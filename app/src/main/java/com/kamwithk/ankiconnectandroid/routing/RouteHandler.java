package com.kamwithk.ankiconnectandroid.routing;

import android.content.Context;
import com.kamwithk.ankiconnectandroid.ankidroid_api.DeckAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.MediaAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.ModelAPI;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.kamwithk.ankiconnectandroid.routing.Router.contentType;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class RouteHandler extends RouterNanoHTTPD.DefaultHandler {

    private AnkiAPIRouting ankiAPIRouting = null;

    public RouteHandler() {
        super();
    }
    @Override
    public String getText() {
        return null;
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
//        Setup
        if (ankiAPIRouting == null) {
            Context context = uriResource.initParameter(0, Context.class);
            ankiAPIRouting = new AnkiAPIRouting(new IntegratedAPI(context));
        }

//        Enforce UTF-8 encoding (response doesn't always contain by default)
        session.getHeaders().put("content-type", contentType);

        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            e.printStackTrace();
        }

        return ankiAPIRouting.findRouteHandleError(files.get("postData"));
    }
}
