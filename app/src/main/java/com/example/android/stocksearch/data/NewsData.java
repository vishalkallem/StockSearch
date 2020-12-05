package com.example.android.stocksearch.data;

import android.util.Log;

import org.ocpsoft.prettytime.PrettyTime;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class NewsData {

    private static final String TAG = NewsData.class.getSimpleName();

    private String url;
    private String title;
    private String source;
    private String imageURL;
    private Instant date;

    public static String getTime(Instant date) {
        LocalDateTime t1 = LocalDateTime.ofInstant(date, ZoneId.of("America/Los_Angeles"));
        LocalDateTime t2 = LocalDateTime.now();

        Period period = Period.between(t1.toLocalDate(), t2.toLocalDate());
        Duration duration = Duration.between(t1, t2);

        if (period.getDays() == 0) {
            if (duration.toHours() > 0)
                return String.format("%s hours ago", duration.toHours() % 24);
            else
                return String.format("%s minutes ago", duration.toMinutes() % 60);
        }
        else if (period.getDays() == 1) {
            return String.format("%s day ago", period.getDays());
        }
        else if (period.getDays() > 1) {
            return String.format("%s days ago", period.getDays());
        }
        return null;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
