package com.mnm.ewash;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnm.ewash.models.PendingRequest;
import com.pixplicity.easyprefs.library.Prefs;

public class AdminRequestListenerService extends Service{
    FirebaseAuth mAuth;
    DatabaseReference userReqReference;
    FirebaseJobDispatcher jobDispatcher;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            jobDispatcher.cancelAll();
        }
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("ewash_notifications",
                    "eWash Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(uri, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build());
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        userReqReference = FirebaseDatabase.getInstance().getReference("requests");
        userReqReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pending = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PendingRequest request = snapshot.getValue(PendingRequest.class);
                    //if (request.status.contains("PENDING")) {
                        pending++;
                     //   break;
                    //}
                }
                if (pending > Prefs.getLong("LAST_CHECKED_COUNT", -1)) {
                    if (Prefs.getLong("LAST_CHECKED_COUNT", -1) == -1) {
                        Prefs.putLong("LAST_CHECKED_COUNT", pending);
                        return;
                    }
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    int icon = R.drawable.ic_launcher;
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Notification.Builder builder = new Notification.Builder(getApplicationContext())
                            .setContentTitle("eWash Admin")
                            .setContentText("There has been a change in the database.")
                            .setContentIntent(pendingIntent)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setSound(uri)
                            .setSmallIcon(icon)
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis());
                    PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "KD_NOTIFICATION_WAKE_LOCK");
                    wakeLock.acquire(3000);
                    wakeLock.release();
                    notificationManager.notify(AdminRequestListenerService.class.hashCode(), builder.build());
                }
                Prefs.putLong("LAST_CHECKED_COUNT", pending);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
