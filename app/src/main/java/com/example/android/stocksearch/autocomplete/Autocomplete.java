package com.example.android.stocksearch.autocomplete;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.SearchView;

import android.widget.SimpleCursorAdapter;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.android.stocksearch.DetailsActivity;
import com.example.android.stocksearch.utils.NetworkUtils;

import org.json.JSONObject;

public class Autocomplete {

    private static final int SEARCH_QUERY_THRESHOLD = 3;

    private static final String[] autocompleteResults = new String[]{
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1
    };

    public static void setAutocompleteListeners(Context ctx, SearchView searchView) {
        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                ctx, android.R.layout.simple_list_item_1, null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1},
                new int[]{android.R.id.text1}));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() >= SEARCH_QUERY_THRESHOLD) {
                    Autocomplete.fetchAutocompleteResults(ctx, query, searchView);
                } else {
                    searchView.getSuggestionsAdapter().changeCursor(null);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                if (query.length() >= SEARCH_QUERY_THRESHOLD) {
                    Intent intent = new Intent(ctx, DetailsActivity.class);
                    intent.setAction(Intent.ACTION_SEARCH);
                    intent.putExtra(SearchManager.QUERY, query);
                    intent.putExtra(Intent.EXTRA_TEXT, query.split("-")[0]);
                    ctx.startActivity(intent);

                    searchView.getSuggestionsAdapter().changeCursor(null);
                    return true;
                }
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                searchView.setQuery(term, false);
                cursor.close();
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {

                return onSuggestionSelect(position);
            }
        });
    }

    public static void fetchAutocompleteResults(Context ctx, String query, SearchView searchView) {
        MatrixCursor cursor = new MatrixCursor(autocompleteResults);
        String url = Uri.parse("https://vk-stock-search.wl.r.appspot.com/api/autocomplete/" + query).toString();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, response -> {
            try {
                for (int index = 0; index < response.length(); index++) {
                    JSONObject obj = response.getJSONObject(index);
                    String ticker = obj.getString("ticker");
                    String companyName = obj.getString("name");
                    Object[] result = new Object[]{index, ticker + " - " + companyName};
                    cursor.addRow(result);
                }
                searchView.getSuggestionsAdapter().changeCursor(cursor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.d("Error response", error.toString()));
        NetworkUtils.getInstance(ctx).addToRequestQueue(jsonArrayRequest);
    }
}
