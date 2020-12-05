package com.example.android.stocksearch;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.stocksearch.adapter.ItemTouchHelperCallback;
import com.example.android.stocksearch.adapter.SectionItemViewHolder;
import com.example.android.stocksearch.autocomplete.Autocomplete;
import com.example.android.stocksearch.data.TickerData;
import com.example.android.stocksearch.section.FavoritesSection;
import com.example.android.stocksearch.section.PortfolioSection;
import com.example.android.stocksearch.utils.DataUtils;
import com.example.android.stocksearch.utils.SectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mainRecyclerView;
    private LinkedHashMap<String, TickerData> portfolioData;
    private LinkedHashMap<String, TickerData> favoritesData;

    private Runnable runnable;
    private SectionUtils sectionUtils;
    private SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter;

    private final Handler handler = new Handler();
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_StockSearch_Launcher);
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        setContentView(R.layout.activity_main);

        // Changing status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.toolbarColor));

        Toolbar mToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mToolbar);

        TextView footerTextView = findViewById(R.id.footerTextView);
        footerTextView.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: Footer clicked!");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse("https://www.tiingo.com"));
            startActivity(browserIntent);
        });

        sectionUtils = new SectionUtils(MainActivity.this);
        mainRecyclerView = findViewById(R.id.mainRecyclerView);

        portfolioData = sectionUtils.getSharedPreferenceObject(SectionUtils.portfolio);
        favoritesData = sectionUtils.getSharedPreferenceObject(SectionUtils.favorites);

        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        portfolioData = sectionUtils.getSharedPreferenceObject(SectionUtils.portfolio);
        favoritesData = sectionUtils.getSharedPreferenceObject(SectionUtils.favorites);
        handler.post(runnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private void setAdapter() {
        sectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter();

        portfolioData = sectionUtils.getSharedPreferenceObject(SectionUtils.portfolio);
        favoritesData = sectionUtils.getSharedPreferenceObject(SectionUtils.favorites);

        if (sectionUtils.hasSection(SectionUtils.portfolio)) {
            sectionedRecyclerViewAdapter.addSection(new PortfolioSection(this, portfolioData, sectionUtils.getNetWorth()));
        }
        if (sectionUtils.hasSection(SectionUtils.favorites)) {
            sectionedRecyclerViewAdapter.addSection(new FavoritesSection(this, favoritesData));
        }

        mainRecyclerView.setAdapter(sectionedRecyclerViewAdapter);
    }

    private void initData() {
        View rootView = findViewById(android.R.id.content);
        DataUtils.showProgressBar(rootView);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(simpleDateFormat.format(new Date()));

        runnable =  new Runnable() {
            public void run() {
                if (!portfolioData.isEmpty()) {
                    DataUtils.getSectionData(MainActivity.this, portfolioData.values().toArray(), SectionUtils.portfolio, data -> {
                        portfolioData = data;
                        setAdapter();
                        Log.d(TAG, "initData: Updated the values in the mainRecyclerView Portfolio Section" + portfolioData);
                    });
                }
                if (!favoritesData.isEmpty()) {
                    DataUtils.getSectionData(MainActivity.this, favoritesData.values().toArray(), SectionUtils.favorites, data -> {
                        favoritesData = data;
                        setAdapter();
                        DataUtils.hideProgressBar(rootView);
                        LinearLayout linearLayout = findViewById(R.id.mainLayout);
                        linearLayout.setVisibility(View.VISIBLE);
                        Log.d(TAG, "initData: Updated the values in the mainRecyclerView Favorites Section" + favoritesData);
                    });
                }
                Log.d(TAG, "run: Fetched data from the endpoint and updated SharedPreferences after 15 sec");
                handler.postDelayed(this, 15000);
            }
        };

        setAdapter();

        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.addItemDecoration(new DividerItemDecoration(mainRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        if (sectionUtils.getSharedPreferenceObject(SectionUtils.portfolio).isEmpty() && sectionUtils.getSharedPreferenceObject(SectionUtils.favorites).isEmpty()) {
            DataUtils.hideProgressBar(rootView);
            LinearLayout linearLayout = findViewById(R.id.mainLayout);
            linearLayout.setVisibility(View.VISIBLE);
        }

        enableSwipeAndReorder();
    }

    private void enableSwipeAndReorder() {

        ItemTouchHelperCallback.HelperContract helperContract = new ItemTouchHelperCallback.HelperContract() {
            @Override
            public void onRowMoved(int fromPosition, int toPosition) {
                boolean flag = false;

                ArrayList<String> portfolioKeySetOrder = new ArrayList<>(portfolioData.keySet());

                if (fromPosition > 0 && fromPosition <= portfolioData.size() && toPosition > 0 && toPosition <= portfolioData.size()) {
                    flag = true;

                    Collections.swap(portfolioKeySetOrder, fromPosition-1, toPosition-1);
                    LinkedHashMap<String, TickerData> reorderedMap = new LinkedHashMap<>();

                    for (String key: portfolioKeySetOrder) {
                        reorderedMap.put(key, portfolioData.get(key));
                    }

                    portfolioData = new LinkedHashMap<>(reorderedMap);
                    sectionedRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition);
                }

                int favoritesFromPosition = fromPosition - portfolioData.size() - 1;
                int favoritesToPosition = toPosition - portfolioData.size() - 1;

                ArrayList<String> favoritesKeySetOrder = new ArrayList<>(favoritesData.keySet());

                if (favoritesFromPosition > 0 && favoritesFromPosition <= favoritesData.size() && favoritesToPosition > 0 && favoritesToPosition <= favoritesData.size()) {
                    flag = true;

                    Collections.swap(favoritesKeySetOrder, favoritesFromPosition-1, favoritesToPosition-1);
                    LinkedHashMap<String, TickerData> reorderedMap = new LinkedHashMap<>();

                    for (String key: favoritesKeySetOrder) {
                        reorderedMap.put(key, favoritesData.get(key));
                    }

                    favoritesData = new LinkedHashMap<>(reorderedMap);
                    sectionedRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition);
                }

                if (flag)
                    sectionUtils.updateSectionData(portfolioData, favoritesData);
                else{
                    setAdapter();
                }

            }

            @Override
            public void onRowSelected(SectionItemViewHolder itemViewHolder) {
                itemViewHolder.rootView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.bgColor));
            }

            @Override
            public void onRowClear(SectionItemViewHolder itemViewHolder) {
                itemViewHolder.rootView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.toolbarColor));
            }
        };

        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback(MainActivity.this, helperContract) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int actualPosition = viewHolder.getAdapterPosition();
                int position = actualPosition - portfolioData.size() - 2;
                if (position >= 0 && position < favoritesData.size()) {
                    TickerData data = (TickerData) favoritesData.values().toArray()[position];
                    if (direction == ItemTouchHelper.LEFT) {
                        Log.d(TAG, "onSwiped: Deleted " + data.getTicker());
                        favoritesData.remove(data.getTicker());
                        sectionUtils.removeSharedPreferenceObjectData(data.getTicker(), SectionUtils.favorites);
                        sectionedRecyclerViewAdapter.notifyItemRemoved(actualPosition);
                    }
                } else {
                    setAdapter();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.delete))
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mainRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView mSearchView =
                (SearchView) menu.findItem(R.id.tbSearch).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);

        Autocomplete.setAutocompleteListeners(this, mSearchView);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.tbSearch) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToDetails(View v) {
        TextView tickerTextView = v.findViewById(R.id.tickerTextView);
        String ticker = tickerTextView.getText().toString();

        Intent detailIntent = new Intent(this, DetailsActivity.class);
        detailIntent.putExtra(Intent.EXTRA_TEXT, ticker);
        startActivity(detailIntent);
    }
}