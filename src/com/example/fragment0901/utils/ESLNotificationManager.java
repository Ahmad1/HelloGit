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

    public ESLNotificationManager(Context context, String title) {
        this.context = context;
        this.title = title;
    }

    public void addNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("ESL").setContentText(title).setPriority(Notification.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(context, NotificationActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1994, builder.build());
    }

    public void removeNotification() {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1994);
    }
}
