package com.kamwithk.ankiconnectandroid.routing;

import static com.kamwithk.ankiconnectandroid.routing.Router.contentType;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class RouteHandler extends RouterNanoHTTPD.DefaultHandler {

    private APIHandler apiHandler = null;

    public RouteHandler() {
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
//        Setup
        Context context = uriResource.initParameter(0, Context.class);
        if (apiHandler == null) {
            apiHandler = new APIHandler(new IntegratedAPI(context), context);
        }

//        Enforce UTF-8 encoding (response doesn't always contain by default)
        session.getHeaders().put("content-type", contentType);

        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            e.printStackTrace();
        }

        Map<String, List<String>> parameters = session.getParameters();
        if (parameters == null || parameters.isEmpty() && files.get("postData") == null) {
            // No data was provided in the POST request so we return a simple response
            NanoHTTPD.Response rep = newFixedLengthResponse("Ankiconnect Android is running.");
            addCorsHeaders(context, rep);
            return rep;
        }

        NanoHTTPD.Response rep = apiHandler.chooseAPI(files.get("postData"), parameters);

        addCorsHeaders(context, rep);
        return rep;
    }

    private void addCorsHeaders(Context context, NanoHTTPD.Response rep) {
        // Add a CORS header if it is set in the preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String corsHost = sharedPreferences.getString("cors_host", "");

        if (!corsHost.trim().equals("")) {
            rep.addHeader("Access-Control-Allow-Origin", corsHost);
            rep.addHeader("Access-Control-Allow-Headers", "*");
        }
    }
}
