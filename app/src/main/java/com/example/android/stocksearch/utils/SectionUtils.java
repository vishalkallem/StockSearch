package com.example.android.stocksearch.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.mtp.MtpStorageInfo;
import android.os.storage.StorageManager;
import android.util.Log;

import com.example.android.stocksearch.data.TickerData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;

public class SectionUtils {
    private static final String TAG = SectionUtils.class.getSimpleName();

    public static final String sharedPreference = "SharedPreference";
    public static final String netWorth = "NetWorth";
    public static final String unInvestedCash = "UnInvestedCash";
    public static final String marketPrice = "MarketPrice";
    public static final String favorites = "Favorites";
    public static final String portfolio = "Portfolio";

    public LinkedHashMap<String, TickerData> sharedData;

    public SharedPreferences sharedPreferences;
    public Editor editor;
    public Gson gson;

    @SuppressLint("CommitPrefEdits")
    public SectionUtils(Context ctx) {
        gson = new Gson();
        sharedData = new LinkedHashMap<>();
        this.sharedPreferences = ctx.getSharedPreferences(sharedPreference, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (!sharedPreferences.contains(SectionUtils.portfolio)) {
            editor.putString(SectionUtils.portfolio, gson.toJson(new LinkedHashMap<String, TickerData>()));
        }

        if (!sharedPreferences.contains(SectionUtils.favorites)) {
            editor.putString(SectionUtils.favorites, gson.toJson(new LinkedHashMap<String, TickerData>()));
        }

        if (!sharedPreferences.contains(SectionUtils.netWorth)) {
            editor.putFloat(SectionUtils.netWorth, 20000f);
        }

        if (!sharedPreferences.contains(SectionUtils.marketPrice)) {
            editor.putFloat(SectionUtils.marketPrice, 0f);
        }

        if (!sharedPreferences.contains(SectionUtils.unInvestedCash)) {
            editor.putFloat(SectionUtils.unInvestedCash, 20000f);
        }

        editor.apply();
    }

    public float getNetWorth() {
        return sharedPreferences.getFloat(netWorth, 20000f);
    }

    public float getUnInvestedCash(){
        return sharedPreferences.getFloat(unInvestedCash, 20000f);
    }

    public float getMarketPrice() {
        return sharedPreferences.getFloat(marketPrice, 0f);
    }

    public void addToFavorites(TickerData tickerData) {
        LinkedHashMap<String, TickerData> favoritesData = new LinkedHashMap<>();
        LinkedHashMap<String, TickerData> portfolioData = getSharedPreferenceObject(SectionUtils.portfolio);
        if (portfolioData.containsKey(tickerData.getTicker())) {
            TickerData td = portfolioData.get(tickerData.getTicker());
            tickerData.setQuantity(td.getQuantity());
        }
        if (sharedPreferences.contains(favorites)) {
            favoritesData = getSharedPreferenceObject(favorites);
        }
        favoritesData.put(tickerData.getTicker(), tickerData);
        editor.putString(favorites, gson.toJson(favoritesData));
        editor.commit();
        Log.d(TAG, "addToFavorites: " + tickerData.getTicker());
    }

    public TickerData getSharePreferenceObjectData(String ticker, String key) {
        if (sharedPreferences.contains(key)) {
            return getSharedPreferenceObject(key).get(ticker);
        }
        return null;
    }

    public void removeSharedPreferenceObjectData(String ticker, String key) {
        LinkedHashMap<String, TickerData> sharedPreferenceObject = getSharedPreferenceObject(key);
        sharedPreferenceObject.remove(ticker);
        editor.putString(key, gson.toJson(sharedPreferenceObject));
        editor.commit();
    }

    public void addToPortfolio(TickerData tickerData, float unInvestedCash, float marketPrice) {
        LinkedHashMap<String, TickerData> portfolioData = new LinkedHashMap<>();
        if (sharedPreferences.contains(portfolio))
                portfolioData = getSharedPreferenceObject(portfolio);
        updatePortfolio(portfolioData, tickerData, unInvestedCash, marketPrice);
    }

    public void updatePortfolio(Map<String, TickerData> portfolioData, TickerData tickerData, float unInvestedCash, float marketPrice) {
        if (sharedPreferences.contains(SectionUtils.favorites)){
            LinkedHashMap<String, TickerData> favoritesData = getSharedPreferenceObject(favorites);
            if (isListed(tickerData.getTicker(), SectionUtils.favorites)) {
                TickerData data = getSharePreferenceObjectData(tickerData.getTicker(), SectionUtils.favorites);
                data.setQuantity(tickerData.getQuantity());
                favoritesData.put(tickerData.getTicker(), data);
                editor.putString(SectionUtils.favorites, gson.toJson(favoritesData));
                editor.commit();
            }
        }

        editor.putFloat(SectionUtils.netWorth, unInvestedCash + marketPrice);
        editor.putFloat(SectionUtils.unInvestedCash, unInvestedCash);
        editor.putFloat(SectionUtils.marketPrice, marketPrice);
        editor.commit();

        if (tickerData.getQuantity() == 0.0) {
            removeSharedPreferenceObjectData(tickerData.getTicker(), SectionUtils.portfolio);
            return;
        }

        portfolioData.put(tickerData.getTicker(), tickerData);
        editor.putString(portfolio, gson.toJson(portfolioData));

        editor.commit();
    }

    public boolean isListed(String ticker, String key) {
        if (sharedPreferences.contains(key)) {
            LinkedHashMap<String, TickerData> sharedPreferenceObject = getSharedPreferenceObject(key);
            return sharedPreferenceObject.containsKey(ticker);
        }
        return false;
    }

    public LinkedHashMap<String, TickerData> getSharedPreferenceObject(String key) {
       String value = sharedPreferences.getString(key, gson.toJson(new LinkedHashMap<String, TickerData>()));
       return gson.fromJson(value, new TypeToken<LinkedHashMap<String, TickerData>>(){}.getType());
    }

    public boolean hasSection(String key) {
        return sharedPreferences.contains(key);
    }

    public void updateSectionData(LinkedHashMap<String, TickerData> portfolioData, LinkedHashMap<String, TickerData> favoritesData) {
        float marketPrice = 0f;
        for (Map.Entry<String, TickerData> entry: portfolioData.entrySet()) {
            marketPrice += (float) (entry.getValue().getQuantity() * entry.getValue().getStockPrice());
        }

        editor.putFloat(SectionUtils.marketPrice, marketPrice);
        editor.putFloat(SectionUtils.netWorth, marketPrice+getUnInvestedCash());

        editor.putString(SectionUtils.favorites, gson.toJson(favoritesData));
        editor.putString(SectionUtils.portfolio, gson.toJson(portfolioData));

        editor.commit();
    }
}
