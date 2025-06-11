package com.example.finalproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Проверяем, содержит ли сообщение данные (полезную нагрузку)
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("body");
            showNotification(title, message);
        }

        // Проверяем, содержит ли сообщение уведомление
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = "fcm_default_channel";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Для Android Oreo и выше нужно создать канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(String token) {
        Log.d("FCM Token", "Refreshed token: " + token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        new Thread(() -> {
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String uid = String.valueOf(sharedPreferences.getInt("user_id", -1));

                URL url_for_add_fcm_token = new URL("http://" + BuildConfig.IP_PC + ":5050/api/add_fcm_token_to_user" +
                        "?token=" + URLEncoder.encode(token, "UTF-8") +
                        "&uid=" + URLEncoder.encode(uid, "UTF-8")
                );
                Log.d("go to server", url_for_add_fcm_token.toString());
                HttpURLConnection conn_for_add_fcm_token = (HttpURLConnection) url_for_add_fcm_token.openConnection();
                conn_for_add_fcm_token.setRequestMethod("POST");
                conn_for_add_fcm_token.setRequestProperty("Accept", "application/json");
                conn_for_add_fcm_token.connect();

                int responseCode = conn_for_add_fcm_token.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader_for_add_fcm_token = new BufferedReader(new InputStreamReader(conn_for_add_fcm_token.getInputStream()));
                    String line_for_add_fcm_token;
                    StringBuilder response_for_add_fcm_token = new StringBuilder();

                    while ((line_for_add_fcm_token = reader_for_add_fcm_token.readLine()) != null) {
                        response_for_add_fcm_token.append(line_for_add_fcm_token);
                    }
                    reader_for_add_fcm_token.close();

                }
        } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }}).start();
    }
}