package com.example.android.stocksearch.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android.stocksearch.R;
import com.example.android.stocksearch.data.NewsData;
import com.example.android.stocksearch.dialogs.DialogHandler;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context mContext;
    private final NewsData[] newsData;

    private static final String TAG = NewsAdapter.class.getSimpleName();

    public NewsAdapter(Context ctx, NewsData[] data) {
        this.mContext = ctx;
        this.newsData = data;
    }


    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        holder.sourceTextView.setText(newsData[position].getSource());
        holder.timeTextView.setText(NewsData.getTime(newsData[position].getDate()));
        holder.titleTextView.setText(newsData[position].getTitle());
        Glide.with(mContext).load(newsData[position].getImageURL()).into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(newsData[position].getUrl()));
            mContext.startActivity(browserIntent);
        });

        holder.itemView.setOnLongClickListener( v -> {
            DialogHandler.createNewsDialog(mContext, newsData[position]);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return newsData.length;
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        TextView sourceTextView;
        TextView timeTextView;
        TextView titleTextView;
        ImageView imageView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            imageView = itemView.findViewById(R.id.newsItemImageView);
        }
    }
}
