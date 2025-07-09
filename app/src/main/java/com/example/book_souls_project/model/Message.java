package com.example.book_souls_project.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String text;
    private boolean isSent; // true if sent by user, false if received
    private long timestamp;

    public Message(String text, boolean isSent) {
        this.text = text;
        this.isSent = isSent;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String text, boolean isSent, long timestamp) {
        this.text = text;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
