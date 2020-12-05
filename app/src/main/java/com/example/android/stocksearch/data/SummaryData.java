package com.example.android.stocksearch.data;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SummaryData {
    private String description;
    private double currentPrice;
    private double highPrice;
    private double lowPrice;
    private double openPrice;
    private double midPrice;
    private double bidPrice;
    private double volume;

    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getMidPrice() {
        return midPrice;
    }

    public void setMidPrice(double midPrice) {
        this.midPrice = midPrice;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public static List<String> getStatsList(SummaryData data) {
        List<String> list = new ArrayList<>();

        list.add("Current Price: " + decimalFormat.format(data.getCurrentPrice()));
        list.add("Low: " + decimalFormat.format(data.getLowPrice()));
        list.add("Bid Price: " +  decimalFormat.format(data.getBidPrice()));
        list.add("Open Price: " + decimalFormat.format(data.getOpenPrice()));
        list.add("Mid: " + decimalFormat.format(data.getMidPrice()));
        list.add("High: " + decimalFormat.format(data.getHighPrice()));
        list.add("Volume: " + decimalFormat.format(data.getVolume()));

        return list;
    }
}
