package com.example.ankiconnectandroid;

import android.content.Context;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;

public class Router extends RouterNanoHTTPD {
    public static Context context;

    public Router(Integer port, Context context) throws IOException {
        super(port);
        Router.context = context;
        addMappings();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public void addMappings() {
        addRoute("/", RouteHandler.class);
    }
}
