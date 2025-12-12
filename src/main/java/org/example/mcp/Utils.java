package org.example.mcp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils {

    private static final OkHttpClient client = new OkHttpClient();

    public static String httpGet(String url) throws Exception {
        Request req = new Request.Builder().url(url).build();
        Response res = client.newCall(req).execute();
        return res.body().string();
    }
}
