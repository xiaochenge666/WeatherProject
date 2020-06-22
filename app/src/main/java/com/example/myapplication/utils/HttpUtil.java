package com.example.myapplication.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendOkHttpReq(String addr,okhttp3.Callback callback){

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(addr).build();
        client.newCall(request).enqueue(callback);

    }
}
