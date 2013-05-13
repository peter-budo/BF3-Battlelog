package com.ninetwozero.bf3droid.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

import org.apache.http.HttpResponse;

public class JsonProducer {

    public static JsonObject jsonObjectFromResponse(HttpResponse response, String requiredObject){
        Reader reader = getJSONReader(response);
        return jsonSubobject(reader, requiredObject);
    }

    private static Reader getJSONReader(HttpResponse response) {
        Reader reader = null;
        try {
            InputStream data = response.getEntity().getContent();
            reader = new InputStreamReader(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

    public static JsonObject jsonObjectFromString(String httpResponse, String requestedObject){
        Reader reader = getJSONReader(httpResponse);
        return jsonSubobject(reader, requestedObject);
    }

    private static Reader getJSONReader(String httpResponse){
        InputStream is = new ByteArrayInputStream(httpResponse.getBytes());
        return new InputStreamReader(is);
    }

    private static JsonObject jsonSubobject(Reader reader, String requestedObject) {
        JsonParser parser = new JsonParser();
        JsonObject mainObject = parser.parse(reader).getAsJsonObject();
        return mainObject.getAsJsonObject(requestedObject);
    }
}
