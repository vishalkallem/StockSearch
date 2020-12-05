package com.example.android.stocksearch.section;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.stocksearch.DetailsActivity;
import com.example.android.stocksearch.R;
import com.example.android.stocksearch.adapter.SectionItemViewHolder;
import com.example.android.stocksearch.data.TickerData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class PortfolioSection extends Section {

    private Context mContext;
    private final float netWorth;
    private LinkedHashMap<String, TickerData> items;

    private static final String headerText = "PORTFOLIO";
    DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private static final String TAG = PortfolioSection.class.getSimpleName();

    public PortfolioSection(Context ctx, LinkedHashMap<String, TickerData> items, float netWorth) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.section_item)
                .headerResourceId(R.layout.header_portfolio)
                .build());
        this.mContext = ctx;
        this.netWorth = netWorth;
        this.items = items;
    }

    @Override
    public int getContentItemsTotal() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new SectionItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        SectionItemViewHolder holder = (SectionItemViewHolder) viewHolder;
        TickerData data = (TickerData) items.values().toArray()[position];

        holder.tickerTextView.setText(data.getTicker());
        holder.stockPriceTextView.setText(decimalFormat.format(data.getStockPrice()));
        holder.companyNameTextView.setText(String.format("%s shares", decimalFormat.format(data.getQuantity())));
        holder.changeTextView.setText(decimalFormat.format(Math.abs(data.getChange())));
        if (data.getChange() < 0) {
            holder.changeTextView.setTextColor(ContextCompat.getColor(mContext, R.color.trending));
            holder.trendingImageView.setImageResource(R.drawable.ic_baseline_trending_down_24);
        } else {
            if (data.getChange() > 0) {
                holder.changeTextView.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                holder.trendingImageView.setImageResource(R.drawable.ic_twotone_trending_up_24);
            }
            else holder.changeTextView.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
        holder.sideArrowImageView.setOnClickListener(v -> {
            Intent detailIntent = new Intent(mContext, DetailsActivity.class);
            detailIntent.putExtra(Intent.EXTRA_TEXT, data.getTicker());
            mContext.startActivity(detailIntent);
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((HeaderViewHolder) holder).headerTextView.setText(headerText);
        ((HeaderViewHolder) holder).netWorthTextView.setText(decimalFormat.format(netWorth));
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView headerTextView;
        TextView netWorthTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            headerTextView = itemView.findViewById(R.id.headerPortfolio);
            netWorthTextView = itemView.findViewById(R.id.netWorthTextView);
        }
    }
}
