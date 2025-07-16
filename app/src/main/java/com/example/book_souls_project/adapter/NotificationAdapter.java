package com.example.book_souls_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.api.types.notification.NotificationResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    
    private List<NotificationResponse> notifications;

    public NotificationAdapter(List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponse notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView textNotificationTitle;
        private TextView textNotificationContent;
        private TextView textNotificationTime;
        private View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textNotificationTitle = itemView.findViewById(R.id.textNotificationTitle);
            textNotificationContent = itemView.findViewById(R.id.textNotificationContent);
            textNotificationTime = itemView.findViewById(R.id.textNotificationTime);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }

        public void bind(NotificationResponse notification) {
            textNotificationTitle.setText(notification.getTitle());
            textNotificationContent.setText(notification.getContent());
            textNotificationTime.setText(formatTime(notification.getCreatedAt()));
            
            // Show/hide unread indicator
            viewUnreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        }

        private String formatTime(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                
                if (date != null) {
                    long timeAgo = System.currentTimeMillis() - date.getTime();
                    
                    if (timeAgo < TimeUnit.MINUTES.toMillis(1)) {
                        return "Just now";
                    } else if (timeAgo < TimeUnit.HOURS.toMillis(1)) {
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeAgo);
                        return minutes + " min ago";
                    } else if (timeAgo < TimeUnit.DAYS.toMillis(1)) {
                        long hours = TimeUnit.MILLISECONDS.toHours(timeAgo);
                        return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
                    } else if (timeAgo < TimeUnit.DAYS.toMillis(7)) {
                        long days = TimeUnit.MILLISECONDS.toDays(timeAgo);
                        return days + " day" + (days > 1 ? "s" : "") + " ago";
                    } else {
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                        return outputFormat.format(date);
                    }
                }
            } catch (ParseException e) {
                // If parsing fails, return the original string
            }
            
            return dateString;
        }
    }
}
