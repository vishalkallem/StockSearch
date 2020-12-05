package com.example.android.stocksearch.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.stocksearch.autocomplete.Autocomplete;

public class NetworkUtils {
    @SuppressLint("StaticFieldLeak")
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    @SuppressLint("StaticFieldLeak")
    private static NetworkUtils mInstance;

    public NetworkUtils(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized NetworkUtils getInstance(Context ctx) {
        if (mInstance == null){
            mInstance = new NetworkUtils(ctx);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue (Request<T> req) {
        getRequestQueue().add(req);
    }
}
