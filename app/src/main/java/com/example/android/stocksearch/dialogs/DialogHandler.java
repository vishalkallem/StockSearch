package com.example.android.stocksearch.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.stocksearch.R;
import com.example.android.stocksearch.data.NewsData;
import com.example.android.stocksearch.data.TickerData;
import com.example.android.stocksearch.utils.SectionUtils;

import java.text.DecimalFormat;

public class DialogHandler {

    private static final String TAG = DialogHandler.class.getSimpleName();
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public interface Callback {
        void onDismiss();
    }

    public static void tradeOperation(Context ctx, TickerData tickerInformation, SectionUtils sectionUtils, Boolean present, Callback callback) {
        final Dialog dialog = new Dialog(ctx);
        dialog.setContentView(R.layout.dialog_trade);

        TextView titleTextView = dialog.findViewById(R.id.titleTextView);
        titleTextView.setText(String.format("Trade %s shares", tickerInformation.getCompanyName()));

        EditText quantityEditText = dialog.findViewById(R.id.quantityEditText);
        TextView totalAmountTextView = dialog.findViewById(R.id.totalAmountTextView);
        totalAmountTextView.setText(String.format("0 x $%s/share = $0.00", tickerInformation.getStockPrice()));

        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "tradeOperation: beforeTextChanged: " + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "tradeOperation: onTextChanged: " + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = quantityEditText.getText().toString();
                if (text.matches("^[a-zA-Z]*$")) {
                    Toast.makeText(ctx,"Please enter valid amount",Toast.LENGTH_LONG).show();
                }
                if (text.isEmpty()) text="0";
                double quantity = Double.parseDouble(text);
                double stockPrice = tickerInformation.getStockPrice();
                totalAmountTextView.setText(String.format("%s x %s/share = $%s", quantity, stockPrice, decimalFormat.format(quantity * stockPrice)));
            }
        });

        TextView availableAmountTextView = dialog.findViewById(R.id.availableAmountTextView);
        availableAmountTextView.setText(String.format("$%s available to buy %s", decimalFormat.format(sectionUtils.getUnInvestedCash()), tickerInformation.getTicker()));

        dialog.show();

        Button buyButton = dialog.findViewById(R.id.buyButton);
        buyButton.setOnClickListener(v -> showSuccessDialog(true, dialog, quantityEditText, ctx, tickerInformation, sectionUtils, present, callback));

        Button sellButton = dialog.findViewById(R.id.sellButton);
        sellButton.setOnClickListener(v -> showSuccessDialog(false, dialog, quantityEditText, ctx, tickerInformation, sectionUtils, present, callback));
    }

    private static void showSuccessDialog(boolean bool, Dialog dialog, EditText quantityEditText, Context ctx, TickerData tickerInformation, SectionUtils sectionUtils, Boolean present, Callback callback) {
        String text = quantityEditText.getText().toString();
        float unInvestedCash = sectionUtils.getUnInvestedCash();
        float marketPrice = sectionUtils.getMarketPrice();
        double quantity;

        if (!text.isEmpty()) {
            quantity = Double.parseDouble(text);
            if (quantity == 0.0) {
                if (bool)
                    Toast.makeText(ctx, "Cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ctx, "Cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                return;
            }
            if (bool) {
                if (quantity * tickerInformation.getStockPrice() > unInvestedCash) {
                    Toast.makeText(ctx, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (quantity > tickerInformation.getQuantity()) {
                    Toast.makeText(ctx, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            Toast.makeText(ctx, "Please enter valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog successDialog = new Dialog(ctx);
        successDialog.setContentView(R.layout.dialog_success);

        TextView messageTextView = successDialog.findViewById(R.id.messageTextView);
        if (bool) {
            messageTextView.setText(String.format("You have successfully bought %s shares of %s", quantity, tickerInformation.getTicker()));
        } else {
            messageTextView.setText(String.format("You have successfully sold %s shares of %s", quantity, tickerInformation.getTicker()));
        }

        dialog.dismiss();
        successDialog.show();

        Button successButton = successDialog.findViewById(R.id.successButton);
        double finalQuantity = quantity;
        successButton.setOnClickListener(v -> {
            if (bool) buyPortfolio(sectionUtils, tickerInformation, finalQuantity, unInvestedCash, marketPrice, present); else sellPortfolio(sectionUtils, tickerInformation.getTicker(), finalQuantity, unInvestedCash, marketPrice);
            callback.onDismiss();
            successDialog.dismiss();
        });
    }

    private static void buyPortfolio(SectionUtils sectionUtils, TickerData tickerData, double quantity, float unInvestedCash, float marketPrice, Boolean present) {
        Log.d(TAG, "buyPortfolio: " + quantity * tickerData.getStockPrice());
        unInvestedCash -= (float)(quantity * tickerData.getStockPrice());
        marketPrice += (float)(quantity * tickerData.getStockPrice());
        if (present) {
            tickerData.setQuantity(tickerData.getQuantity() + quantity);
            sectionUtils.updatePortfolio(sectionUtils.getSharedPreferenceObject(SectionUtils.portfolio), tickerData, unInvestedCash, marketPrice);
        } else {
            tickerData.setQuantity(quantity);
            sectionUtils.addToPortfolio(tickerData, unInvestedCash, marketPrice);
        }
    }

    private static void sellPortfolio(SectionUtils sectionUtils, String ticker, double quantity, float unInvestedCash, float marketPrice) {
        TickerData portfolioData = sectionUtils.getSharePreferenceObjectData(ticker, SectionUtils.portfolio);
        portfolioData.setQuantity(portfolioData.getQuantity() - quantity);
        Log.d(TAG, "sellPortfolio: " + quantity * portfolioData.getStockPrice());
        unInvestedCash += (float)(quantity * portfolioData.getStockPrice());
        marketPrice -= (float)(quantity * portfolioData.getStockPrice());
        sectionUtils.updatePortfolio(sectionUtils.getSharedPreferenceObject(SectionUtils.portfolio), portfolioData, unInvestedCash, marketPrice);
    }

    public static void createNewsDialog(Context ctx, NewsData data) {
        final Dialog newsDialog = new Dialog(ctx);
        newsDialog.setContentView(R.layout.dialog_news);

        ImageView dialogImageView = newsDialog.findViewById(R.id.dialogImage);
        Glide.with(ctx).load(data.getImageURL()).into(dialogImageView);

        TextView dialogTitle = newsDialog.findViewById(R.id.dialogTitle);
        dialogTitle.setText(data.getTitle());

        newsDialog.show();

        ImageButton twitterButton = newsDialog.findViewById(R.id.twitterButton);
        twitterButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse("https://twitter.com/intent/tweet/?text=Check out this Link: " + data.getUrl() + " &hashtags=CSCI571StockApp"));
            ctx.startActivity(browserIntent);
        });

        ImageButton chromeButton = newsDialog.findViewById(R.id.chromeButton);
        chromeButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(data.getUrl()));
            ctx.startActivity(browserIntent);
        });
    }
}
