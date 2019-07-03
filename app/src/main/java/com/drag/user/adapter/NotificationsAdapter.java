package com.drag.user.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.drag.user.R;
import com.drag.user.model.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationHolder> {

    private List<Notification> notificationList;
    private int itemCount = 0;

    public NotificationsAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_notifications, parent, false);
        return new NotificationHolder(view);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        switch (notificationList.get(position).getType()) {
            case "1":
                holder.iconView.setImageResource(R.drawable.ic_notification1);
                break;
            case "2":
                holder.iconView.setImageResource(R.drawable.ic_notification2);
                break;
            case "3":
                holder.iconView.setImageResource(R.drawable.ic_notification3);
                break;
            case "4":
                holder.iconView.setImageResource(R.drawable.ic_notification4);
                break;
        }

        holder.subjectView.setText(notificationList.get(position).getSubject());
        holder.bodyView.setText(notificationList.get(position).getBody());

        try {
            Calendar calendar = Calendar.getInstance();
            Date displayTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(notificationList.get(position).getUpdatedAt());
            calendar.setTime(displayTime);
            calendar.add(Calendar.HOUR, 5);
            calendar.add(Calendar.MINUTE, 30);
            holder.timeView.setText(new SimpleDateFormat("MMM d, hh:mm a").format(calendar.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (notificationList != null) {
            itemCount = notificationList.size();
        }
        return itemCount;
    }

    class NotificationHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView subjectView, bodyView, timeView;

        NotificationHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.notifications_icon);
            subjectView = itemView.findViewById(R.id.notifications_subject);
            bodyView = itemView.findViewById(R.id.notifications_body);
            timeView = itemView.findViewById(R.id.notifications_time);
        }
    }
}