package com.example.android.stocksearch.data;

public class TickerData {
    private String ticker;
    private String companyName;
    private double stockPrice;
    private double change;
    private double quantity;

    public TickerData() {
        this.quantity = 0.0;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getStockPrice() {
        return stockPrice;
    }

    public void setStockPrice(double stockPrice) {
        this.stockPrice = stockPrice;
    }

    @Override
    public String toString() {
        return "TickerData{" +
                "ticker='" + ticker + '\'' +
                ", companyName='" + companyName + '\'' +
                ", stockPrice=" + stockPrice +
                ", change=" + change +
                ", quantity=" + quantity +
                '}';
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
