package com.example.android.stocksearch.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.stocksearch.R;

public class SectionItemViewHolder extends RecyclerView.ViewHolder {
    public final View rootView;
    public TextView tickerTextView;
    public TextView companyNameTextView;
    public TextView stockPriceTextView;
    public TextView changeTextView;
    public ImageView trendingImageView;
    public ImageView sideArrowImageView;

    public SectionItemViewHolder(@NonNull View itemView) {
        super(itemView);
        rootView = itemView;
        tickerTextView = itemView.findViewById(R.id.tickerTextView);
        companyNameTextView = itemView.findViewById(R.id.companyNameTextView);
        stockPriceTextView = itemView.findViewById(R.id.stockPriceTextView);
        changeTextView = itemView.findViewById(R.id.changeTextView);
        trendingImageView = itemView.findViewById(R.id.trendingImageView);
        sideArrowImageView = itemView.findViewById(R.id.sideArrowImageView);
    }
}
