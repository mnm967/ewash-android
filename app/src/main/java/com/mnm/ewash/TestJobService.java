package com.mnm.ewash;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


public class TestJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("ewash_notifications",
                    "eWash Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
        int icon = R.drawable.ic_launcher;
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                .setContentTitle("eWash Request")
                .setContentText("Testing.")
                .setContentIntent(pendingIntent)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setVibrate(new long[]{500, 1000})
                .setWhen(System.currentTimeMillis());
        notificationManager.notify(123, builder.build());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }
}
