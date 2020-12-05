package com.example.android.stocksearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.stocksearch.adapter.NewsAdapter;
import com.example.android.stocksearch.adapter.StatsAdapter;
import com.example.android.stocksearch.data.NewsData;
import com.example.android.stocksearch.data.SummaryData;
import com.example.android.stocksearch.data.TickerData;
import com.example.android.stocksearch.dialogs.DialogHandler;
import com.example.android.stocksearch.utils.DataUtils;
import com.example.android.stocksearch.utils.SectionUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {

    private Menu menu;
    private TickerData data;
    public String searchResult;
    private SectionUtils sectionUtils;

    private boolean flag = false;
    private static final String TAG = DetailsActivity.class.getSimpleName();

    DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_StockSearch_Launcher);
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        setContentView(R.layout.activity_details);

        // Changing status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.toolbarColor));

        Toolbar mToolbar = findViewById(R.id.dToolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            searchResult = intent.getStringExtra(Intent.EXTRA_TEXT).trim();
            Log.d(TAG, "onCreate: " + searchResult);
        }

        sectionUtils = new SectionUtils(this);
    }

    private void setInformation() {
        View rootView = findViewById(android.R.id.content);
        LinearLayout linearLayout = findViewById(R.id.detailLayout);
        linearLayout.setVisibility(View.INVISIBLE);
        this.menu.findItem(R.id.tbFavorite).setVisible(false);
        Log.d(TAG, "setInformation: ");
        DataUtils.showProgressBar(rootView);
        DataUtils.getTickerDetails(this, searchResult, this::setTickerInformation);
    }

    private void setTickerInformation(TickerData tickerInformation) {
        this.data = tickerInformation;

        TextView tickerTextView = findViewById(R.id.dTicker);
        tickerTextView.setText(tickerInformation.getTicker());
        Log.d(TAG, "setTickerInformation: " + tickerInformation.getTicker());

        TextView companyNameTextView = findViewById(R.id.dCompanyName);
        companyNameTextView.setText(tickerInformation.getCompanyName());
        Log.d(TAG, "setTickerInformation: " + tickerInformation.getCompanyName());

        TextView stockPriceTextView = findViewById(R.id.dStockPrice);
        stockPriceTextView.setText(String.format("$%s", decimalFormat.format(tickerInformation.getStockPrice())));
        Log.d(TAG, "setTickerInformation: " + tickerInformation.getStockPrice());

        TextView changeTextView = findViewById(R.id.dChange);
        double change = tickerInformation.getChange();        
        Log.d(TAG, "setTickerInformation: " + tickerInformation.getChange());
        
        if (change < 0) {
            changeTextView.setText(String.format("-$%s", decimalFormat.format(Math.abs(change))));
            changeTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            changeTextView.setText(String.format("$%s", decimalFormat.format(Math.abs(change))));
            if (change > 0) changeTextView.setTextColor(ContextCompat.getColor(this, R.color.green));
            else changeTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
        }

        setChartsInformation();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setChartsInformation() {
        Log.d(TAG, "setChartsInformation - Loading High charts!");
        WebView highChartsWebView = findViewById(R.id.highChartsWebView);
        highChartsWebView.getSettings().setJavaScriptEnabled(true);
        highChartsWebView.getSettings().setDomStorageEnabled(true);
        highChartsWebView.clearCache(true);

        highChartsWebView.loadUrl("file:///android_asset/index.html");
        highChartsWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equals("file:///android_asset/index.html")) {
                    highChartsWebView.loadUrl("javascript:createCharts('" + searchResult + "');");
                }
            }
        });

        setPortfolioInformation();
    }

    private void setPortfolioInformation() {
        updatePortfolioInformation();
        DataUtils.getSummaryDetails(this, searchResult, this::setSummaryInformation);
    }

    private void updatePortfolioInformation() {
        TextView tradingTextView = findViewById(R.id.tradingTextView);
        if (sectionUtils.isListed(searchResult, SectionUtils.portfolio)) {
            TickerData tickerData = sectionUtils.getSharePreferenceObjectData(searchResult, SectionUtils.portfolio);
            tradingTextView.setText(Html.fromHtml("<div>Shares owned: " + tickerData.getQuantity() + "</div><div>Market Value: $" + decimalFormat.format(sectionUtils.getMarketPrice()) + "</div>", Html.FROM_HTML_MODE_COMPACT));
        } else
            tradingTextView.setText(Html.fromHtml("<div>You have 0 shares of " + searchResult +"</div><div>Start trading!<div>", Html.FROM_HTML_MODE_COMPACT));
    }

    public void tradeOperation(View view) {
        if (sectionUtils.isListed(searchResult, SectionUtils.portfolio))
            DialogHandler.tradeOperation(DetailsActivity.this, sectionUtils.getSharePreferenceObjectData(searchResult, SectionUtils.portfolio), sectionUtils, true, this::updatePortfolioInformation);
        else
            DialogHandler.tradeOperation(DetailsActivity.this, data, sectionUtils, false, this::updatePortfolioInformation);
    }

    @SuppressLint("SetTextI18n")
    private void setSummaryInformation(SummaryData summaryData) {
        GridView statsGridView = findViewById(R.id.statsGridView);
        statsGridView.setAdapter(new StatsAdapter(DetailsActivity.this, SummaryData.getStatsList(summaryData)));
        TextView aboutTextView = findViewById(R.id.aboutTextView);
        TextView showTextView = findViewById(R.id.showTextView);

        aboutTextView.setText(summaryData.getDescription());
        showTextView.setText("Show more...");

        aboutTextView.post(() -> {
            if (aboutTextView.getLineCount() <= 2)
                showTextView.setVisibility(View.INVISIBLE);
            else {
                aboutTextView.setEllipsize(TextUtils.TruncateAt.END);
                aboutTextView.setMaxLines(2);
            }
        });

        Log.d(TAG, "setSummaryInformation: " + aboutTextView.getText());
        DataUtils.getNewsDetails(this, searchResult, this::setNewsInformation);
    }

    @SuppressLint("SetTextI18n")
    public void toggleDescription(View view) {
        TextView aboutTextView = findViewById(R.id.aboutTextView);
        TextView showTextView = findViewById(R.id.showTextView);
        if (flag) {
            flag = false;
            showTextView.setText("Show less");
            aboutTextView.setEllipsize(null);
            aboutTextView.setMaxLines(Integer.MAX_VALUE);
        } else {
            flag = true;
            showTextView.setText("Show more...");
            aboutTextView.setEllipsize(TextUtils.TruncateAt.END);
            aboutTextView.setMaxLines(2);
        }
    }

    private void setNewsInformation(NewsData[] data) {
        Log.d(TAG, "setNewsInformation: " + data[0].getTitle());
        setMainNewsInformation(data[0]);

        RecyclerView newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        newsRecyclerView.setAdapter(new NewsAdapter(DetailsActivity.this, Arrays.copyOfRange(data, 1, data.length)));

        View rootView = findViewById(android.R.id.content);
        DataUtils.hideProgressBar(rootView);
        this.menu.findItem(R.id.tbFavorite).setVisible(true);
        LinearLayout linearLayout = findViewById(R.id.detailLayout);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void setMainNewsInformation(NewsData newsItem) {
        TextView sourceTextView = findViewById(R.id.mainSourceTextView);
        sourceTextView.setText(newsItem.getSource());

        TextView timeTextView = findViewById(R.id.mainTimeTextView);
        timeTextView.setText(NewsData.getTime(newsItem.getDate()));

        TextView titleTextView = findViewById(R.id.mainTitleTextView);
        titleTextView.setText(newsItem.getTitle());

        ImageView newsImageView = findViewById(R.id.mainArticleImageView);
        Glide.with(DetailsActivity.this).load(newsItem.getImageURL()).into(newsImageView);

        ConstraintLayout newsLayout = findViewById(R.id.mainConstraintLayout);
        newsLayout.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(newsItem.getUrl()));
            startActivity(browserIntent);
        });

        newsLayout.setOnLongClickListener(v-> {
            DialogHandler.createNewsDialog(DetailsActivity.this, newsItem);
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_toolbar, menu);
        if (sectionUtils.isListed(searchResult, SectionUtils.favorites)){
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24));
        } else {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_border_24));
        }

        this.menu = menu;
        setInformation();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (itemID == R.id.tbFavorite) {
            if (sectionUtils.isListed(searchResult, SectionUtils.favorites)) {
                sectionUtils.removeSharedPreferenceObjectData(searchResult, SectionUtils.favorites);
                this.menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_border_24));
                Toast.makeText(this, String.format("\"%s\" was removed from favorites", searchResult), Toast.LENGTH_SHORT).show();
                return true;
            }
            sectionUtils.addToFavorites(this.data);
            this.menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24));
            Toast.makeText(this, String.format("\"%s\" was added to favorites", searchResult), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}