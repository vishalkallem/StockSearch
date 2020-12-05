package com.example.android.stocksearch.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.android.stocksearch.R;

import java.util.List;

public class StatsAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<String> statsList;

    public StatsAdapter(Context ctx, List<String> list) {
        this.mContext = ctx;
        this.statsList = list;
    }

    @Override
    public int getCount() {
        return statsList.size();
    }

    @Override
    public Object getItem(int position) {
        return statsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(mContext);
        textView.setText((String)getItem(position));
        textView.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return textView;
    }
}
