package org.example.mcp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.MediaType;

public class Utils {

    // Base URL for the backend API, configurable via environment variable.
    // Defaults to http://localhost:8080 for local development.
    public static final String BASE_URL;
    private static volatile String authCookie = "_ga=GA1.1.617673396.1758525774; _ga_699NE13B0K=GS2.1.s1762323070$o9$g0$t1762323070$j60$l0$h0; intercom-device-id-iqm8aavl=d9f7322e-0085-497b-9c8a-9d051dfd3485; SESSION=Nzc5NDhlYTgtYjQ5NC00YWVjLWE2MjktODQ0ZTQ0ZDE4MTY5; intercom-session-iqm8aavl=ZC8wWTJ1Wm5hOWUwa1BDMTFqeTB2c0hBK01OdU4zQmpFSitRbk1lQVd2cDRVK3Jmb1FNZVJrZ2dRNjRhUUZDbmFMTXN3bmJUQTBxblBZSlV2ZzlESVhIb3Z1VHFWb0R3ZnI5aWVRWmR3UXc9LS1iYTNObWNMQXg4ZGhzdzNxQTlKaUlnPT0=--b543bb803d8e0c958af8957dc7b8a85db83c2b32";

    static {
        String base = System.getenv("API_BASE_URL");
        if (base == null || base.isBlank()) {
            base = "http://localhost:8099";
        }
        // Strip trailing slash to avoid double slashes when building URLs
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        BASE_URL = base;
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * Low-level helper that performs an HTTP GET against a fully built URL string.
     * Prefer using the typed httpGet(path, params) helper where possible.
     */
    public static String httpGetRaw(String url) throws Exception {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .get();

        // Attach auth cookie if present
        if (authCookie != null && !authCookie.isBlank()) {
            builder.addHeader("Cookie", authCookie);
        }

        Request req = builder.build();

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                throw new IOException("HTTP " + res.code() + " calling " + url);
            }
            ResponseBody body = res.body();
            if (body == null) {
                throw new IOException("Empty response body from " + url);
            }
            return body.string();
        }
    }

    /**
     * High-level helper that builds a URL from a path and query parameters,
     * taking care of encoding and using the configured BASE_URL.
     */
    public static String httpGet(String path, Map<String, String> queryParams) throws Exception {
        HttpUrl base = HttpUrl.parse(BASE_URL + path);
        if (base == null) {
            throw new IllegalArgumentException("Invalid base URL or path: " + BASE_URL + path);
        }

        HttpUrl.Builder builder = base.newBuilder();
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    builder.addQueryParameter(entry.getKey(), entry.getValue());
                }
            }
        }

        HttpUrl url = builder.build();
        return httpGetRaw(url.toString());
    }

    /**
     * Helper for POSTing a JSON body to a relative path on the BASE_URL.
     */
    public static String httpPostJson(String path, String jsonBody) throws Exception {
        HttpUrl url = HttpUrl.parse(BASE_URL + path);
        if (url == null) {
            throw new IllegalArgumentException("Invalid base URL or path: " + BASE_URL + path);
        }

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);

        // Attach auth cookie
        if (authCookie != null && !authCookie.isBlank()) {
            builder.addHeader("Cookie", authCookie);
        }

        Request req =  builder.build();

        try (Response res = client.newCall(req).execute()) {
            ResponseBody responseBody = res.body();
            String responseBodyString = responseBody != null ? responseBody.string() : "";
            
            if (!res.isSuccessful()) {
                String errorMsg = "HTTP " + res.code() + " calling " + url;
                if (!responseBodyString.isEmpty()) {
                    errorMsg += ". Response: " + responseBodyString;
                }
                throw new IOException(errorMsg);
            }
            
            if (responseBodyString.isEmpty()) {
                throw new IOException("Empty response body from " + url);
            }
            return responseBodyString;
        }
    }
}
