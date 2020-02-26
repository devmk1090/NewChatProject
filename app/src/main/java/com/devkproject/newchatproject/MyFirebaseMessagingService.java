package com.devkproject.newchatproject;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

   private FirebaseAuth mAuth;
   private FirebaseUser mCurrentUser;
   private DatabaseReference userRef;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String text = data.get("text");
            sendNotification(title,text);
        }
    }

    private void sendNotification(String title, String text) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String CHANNEL_ID = "BT";

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.icon_heart)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_NAME = "BlindTalk";
            String CHANNEL_DESCRIPTION ="BT Description";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
            channel.setSound(defaultSoundUri, null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        sendNewTokenToServer(token);
        Log.d("TOKEN", token);
    }

    private void sendNewTokenToServer(String token) {
       mAuth = FirebaseAuth.getInstance();
       mCurrentUser = mAuth.getCurrentUser();
       userRef = FirebaseDatabase.getInstance().getReference("users");
       if(mCurrentUser != null) {
           userRef.child(mCurrentUser.getUid()).child("deviceToken").setValue(token);
       }
    }
}