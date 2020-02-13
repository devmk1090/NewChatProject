package com.devkproject.newchatproject.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.devkproject.newchatproject.R;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Notification  {

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;

    private CharSequence name = "NEWCHATPROJECT";
    private String description = "THIS IS NOTI CHANNEL";
    private String CHANNEL_ID = "NewChat_Notification";
    private int importance = NotificationManager.IMPORTANCE_DEFAULT;


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "NEWCHATPROJECT";
            String description = "THIS IS NOTI CHANNEL";
            String CHANNEL_ID = "NewChat_Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] {100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(channel);
        }
    }
    public Notification(Context context) {
        this.mContext = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] {100, 200, 300, 400, 500, 400, 300, 200, 400});
            channel.setShowBadge(false);
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID);
        mNotifyBuilder.setVibrate(new long[] {1000, 1000});
        mNotifyBuilder.setPriority(100);
        mNotifyBuilder.setSmallIcon(R.drawable.icon_heart);
        mNotifyBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    }
    public Notification setTitle(String title) {
        mNotifyBuilder.setContentTitle(title);
        mNotifyBuilder.setTicker(title);
        return this;
    }
    public Notification setText(String text) {
        mNotifyBuilder.setContentText(text);
        return this;
    }
    public Notification setData(Intent intent) {
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(140, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyBuilder.setContentIntent(pendingIntent);
        return this;
    }
    public void notification() {
        try {
            mNotificationManager.notify(1, mNotifyBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
