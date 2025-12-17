package org.example.mcp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Utils {

    // Base URL for the backend API, configurable via environment variable.
    // Defaults to http://localhost:8080 for local development.
    public static final String BASE_URL;

    static {
        String base = System.getenv("API_BASE_URL");
        if (base == null || base.isBlank()) {
            base = "http://localhost:8080";
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
        Request req = new Request.Builder().url(url).build();
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
}
