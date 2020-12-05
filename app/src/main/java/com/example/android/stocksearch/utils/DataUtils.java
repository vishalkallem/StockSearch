package com.example.android.stocksearch.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.android.stocksearch.R;
import com.example.android.stocksearch.data.NewsData;
import com.example.android.stocksearch.data.SummaryData;
import com.example.android.stocksearch.data.TickerData;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class DataUtils {

    private static final String TAG = DataUtils.class.getSimpleName();
    private static final TickerData tickerData = new TickerData();
    private static final SummaryData summaryData = new SummaryData();
    private static final NewsData[] newsData = new NewsData[20];

    public interface Callback<T> {
        void onSuccess(T data);
    }

    public static void hideProgressBar(View view) {
        ProgressBar mLoadingIndicator = view.findViewById(R.id.pbLoadingIndicator);
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        TextView mFetchingDataText = view.findViewById(R.id.tvFetchingData);
        mFetchingDataText.setVisibility(View.INVISIBLE);
    }

    public static void showProgressBar(View view) {
        ProgressBar mLoadingIndicator = view.findViewById(R.id.pbLoadingIndicator);
        mLoadingIndicator.setVisibility(View.VISIBLE);

        TextView mFetchingDataText = view.findViewById(R.id.tvFetchingData);
        mFetchingDataText.setVisibility(View.VISIBLE);
    }

    public static void getTickerDetails(Context ctx, String ticker, final Callback<TickerData> callback) {
        String url = Uri.parse("https://vk-stock-search.wl.r.appspot.com/api/details/" + ticker).toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response ->  {
            try {
                tickerData.setTicker(response.getString("ticker"));
                tickerData.setCompanyName(response.getString("companyName"));
                tickerData.setStockPrice(response.getDouble("last"));
                tickerData.setChange(response.getDouble("change"));
                tickerData.setQuantity(0.00);
                callback.onSuccess(tickerData);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.d(TAG, "getTickerDetails: Error response " + error.toString()));
        NetworkUtils.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public static void getSummaryDetails(Context ctx, String ticker, final Callback<SummaryData> callback) {
        String url = Uri.parse("https://vk-stock-search.wl.r.appspot.com/api/summary/" + ticker).toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                summaryData.setCurrentPrice(tickerData.getStockPrice());
                summaryData.setDescription(response.getString("description"));
                summaryData.setHighPrice(response.getDouble("high"));
                summaryData.setLowPrice(response.getDouble("low"));
                summaryData.setOpenPrice(response.getDouble("open"));
                summaryData.setVolume(response.getDouble("volume"));
                String midValue = response.getString("mid");
                if (midValue.equals("-")) {
                    summaryData.setMidPrice(0.00);
                } else {
                    summaryData.setMidPrice(Double.parseDouble(midValue));
                }
                String bidPrice = response.getString("bidPrice");
                if (bidPrice.equals("null")) {
                    summaryData.setBidPrice(0.00);
                } else {
                    summaryData.setBidPrice(Double.parseDouble(bidPrice));
                }
                Log.d(TAG, "getSummaryDetails: " + summaryData.getCurrentPrice());
                callback.onSuccess(summaryData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.d(TAG, "getSummaryDetails: Error response " + error.toString()));
        NetworkUtils.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    } 

    public static void getNewsDetails(Context ctx, String ticker, final Callback<NewsData[]> callback) {
        String url = Uri.parse("https://vk-stock-search.wl.r.appspot.com/api/news/" + ticker).toString();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            try {
                for (int i = 0; i < response.length(); i++) {
                    newsData[i] = new NewsData();
                    newsData[i].setTitle(response.getJSONObject(i).getString("title"));
                    newsData[i].setSource(response.getJSONObject(i).getString("source"));
                    newsData[i].setUrl(response.getJSONObject(i).getString("url"));
                    newsData[i].setImageURL(response.getJSONObject(i).getString("image"));
                    newsData[i].setDate(Instant.parse(response.getJSONObject(i).getString("publishedAt")));
                }
                Log.d(TAG, "getNewsDetails: " + newsData.length);
                callback.onSuccess(newsData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.d(TAG, "getNewsDetails: Error response " + error.toString()));
        NetworkUtils.getInstance(ctx).addToRequestQueue(jsonArrayRequest);
    }

    public static void getSectionData(Context ctx, Object[] data, String key, final Callback<LinkedHashMap<String, TickerData>> callback) {
        StringBuilder query = new StringBuilder();
        SectionUtils sectionUtils = new SectionUtils(ctx);

        Map<Integer, String> positionMap = new HashMap<>();

        int position = 0;
        for (Object obj: data) {
            TickerData d = (TickerData) obj;
            query.append(d.getTicker()).append(",");
            positionMap.put(position, d.getTicker());
            position += 1;
        }

        String url = Uri.parse("https://vk-stock-search.wl.r.appspot.com/api/lastPrices/" + query).toString();
        Log.d(TAG, "getSectionData: " + url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            try {
                LinkedHashMap<String, TickerData> sectionData = new LinkedHashMap<>();
                for (int i = 0; i < response.length(); i++) {
                    sectionData = sectionUtils.getSharedPreferenceObject(key);
                    TickerData td = sectionData.get(response.getJSONObject(i).getString("ticker"));
                    td.setStockPrice(response.getJSONObject(i).getDouble("last"));
                    td.setChange(response.getJSONObject(i).getDouble("change"));
                    sectionData.put(td.getTicker(), td);
                }
                LinkedHashMap<String, TickerData> orderedSectionData = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> entry: positionMap.entrySet()) {
                    orderedSectionData.put(entry.getValue(), sectionData.get(entry.getValue()));
                }
                callback.onSuccess(orderedSectionData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.d(TAG, "getSectionData: Error response " + error.toString()));
        NetworkUtils.getInstance(ctx).addToRequestQueue(jsonArrayRequest);
    }
}