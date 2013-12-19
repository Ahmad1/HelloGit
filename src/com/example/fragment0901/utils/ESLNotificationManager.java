package com.example.fragment0901.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.fragment0901.R;
import com.example.fragment0901.fragment.NotificationActivity;

public class ESLNotificationManager {
    private Context context;
    private String title;
    private NotificationManager manager;

    public ESLNotificationManager(Context context, String title) {
        this.context = context;
        this.title = title;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void addNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("ESL").setContentText(title).setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        if (manager != null)
        manager.notify(1994, builder.build());
    }

    public void removeNotification() {
        if (manager != null)
        manager.cancel(1994);
    }
}
