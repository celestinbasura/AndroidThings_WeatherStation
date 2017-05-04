package com.example.pitepmerature.WebServer;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Celestin on 4/05/2017.
 */

public class HttpdServer extends NanoHTTPD {

    private static final int PORT = 8888;
    private float temp;
    private float press;


    public HttpdServer() {
        super(PORT);

    }

    public void setTemp(float temp){
        this.temp = temp;
    }
    public void setPress(float press){
        this.press = press;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();


        String html =
                "<!DOCTYPE html><html><body>" +
                        "<h1>Temperature Reading: " + temp + " C" +
                        "<h1>Pressure Reading: " + press + " hPa" +
                        "</body></html>";

        return newFixedLengthResponse(html);
    }
}