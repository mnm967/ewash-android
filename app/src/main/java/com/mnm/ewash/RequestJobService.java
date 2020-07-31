package com.mnm.ewash;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnm.ewash.models.PendingRequest;
import com.pixplicity.easyprefs.library.Prefs;


public class RequestJobService extends JobService{
    public static final String TAG = "REQUEST_JOB_SERVICE";
    FirebaseAuth mAuth;
    DatabaseReference userReqReference;
    FirebaseJobDispatcher jobDispatcher;
    @Override
    public boolean onStartJob(JobParameters job) {
        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            jobDispatcher.cancelAll();
        }
        final String reqId = Prefs.getString("REQ_ID","");
        //Prefs.putString("REQ_STATUS", "");
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager.getNotificationChannel("ewash_notifications") == null) {
                channel = new NotificationChannel("ewash_notifications",
                        "eWash Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableVibration(true);
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                channel.setSound(uri, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build());
                notificationManager.createNotificationChannel(channel);
            }
        }
        //Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_LONG).show();
        if(mAuth.getCurrentUser() == null){
            jobDispatcher.cancelAll();
            return false;
        }
        userReqReference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("history");
        userReqReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(), "Im here", Toast.LENGTH_LONG).show();
                try {
                    PendingRequest request = null;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        request = snapshot.getValue(PendingRequest.class);
                        if(request.requestId.equals(reqId)) {
                            break;
                        }
                    }
                    if(request == null){
                        return;
                    }
                    //Toast.makeText(getApplicationContext(), "Im past here: "+request.status, Toast.LENGTH_LONG).show();
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    if (request.status.equals("ACCEPTED")) {
                        if(Prefs.getString("REQ_STATUS", "").contains("ACCEPTED")){
                            //stopSelf();
                            return;
                        }
                        Prefs.putString("REQ_STATUS", "ACCEPTED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been ACCEPTED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setSound(uri)
                                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ewash:requestwakelock");
                        wakeLock.acquire(3000);
                        wakeLock.release();
                        notificationManager.notify(9785, builder.build());
                        //jobDispatcher.cancelAll();
                    } else if (request.status.contains("REJECTED")) {
                        Prefs.putString("REQ_STATUS", "REJECTED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been REJECTED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setSound(uri)
                                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ewash:requestwakelock");
                        wakeLock.acquire(3000);
                        wakeLock.release();
                        notificationManager.notify(9785, builder.build());
                        jobDispatcher.cancelAll();
                    }else if (request.status.contains("CANCELLED")) {
                        Prefs.putString("REQ_STATUS", "CANCELLED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been CANCELLED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setSound(uri)
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ewash:requestwakelock");
                        wakeLock.acquire(3000);
                        wakeLock.release();
                        notificationManager.notify(9785, builder.build());
                        jobDispatcher.cancelAll();
                    }else if (request.status.contains("COMPLETED")) {
                        Prefs.putString("REQ_STATUS", "COMPLETED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been COMPLETED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setPriority(Notification.PRIORITY_MAX)
                                .setSound(uri)
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ewash:requestwakelock");
                        wakeLock.acquire(3000);
                        wakeLock.release();
                        notificationManager.notify(9785, builder.build());
                        jobDispatcher.cancelAll();
                    }
                    //stopSelf();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                //stopSelf();
            }
        });
        //Intent intent = new Intent(getApplicationContext(), RequestListenerService.class);
        //startService(intent);
        /*jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            jobDispatcher.cancelAll();
        }
        final String reqId = Prefs.getString("REQ_ID","");
        //Prefs.putString("REQ_STATUS", "");
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("ewash_notifications",
                    "eWash Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(uri, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build());
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_LONG).show();
        userReqReference = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("history");
        userReqReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(), "Im here", Toast.LENGTH_LONG).show();
                try {
                    PendingRequest request = null;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        request = snapshot.getValue(PendingRequest.class);
                        if(request.requestId.equals(reqId)) {
                            break;
                        }
                    }
                    if(request == null){
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "Im past here: "+request.status, Toast.LENGTH_LONG).show();
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    if (request.status.equals("ACCEPTED")) {
                        Prefs.putString("REQ_STATUS", "ACCEPTED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been ACCEPTED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        notificationManager.notify(9785, builder.build());
                        //jobDispatcher.cancelAll();
                    } else if (request.status.contains("REJECTED")) {
                        Prefs.putString("REQ_STATUS", "REJECTED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been REJECTED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        notificationManager.notify(9785, builder.build());
                        jobDispatcher.cancelAll();
                    }else if (request.status.contains("CANCELLED")) {
                        Prefs.putString("REQ_STATUS", "CANCELLED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been CANCELLED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        notificationManager.notify(9785, builder.build());
                        jobDispatcher.cancelAll();
                    }else if (request.status.contains("COMPLETED")) {
                        Prefs.putString("REQ_STATUS", "COMPLETED");
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        int icon = R.drawable.ic_launcher;
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "ewash_notifications")
                                .setContentTitle("eWash Request")
                                .setContentText("Your request has been COMPLETED.")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(icon)
                                .setAutoCancel(true)
                                .setSound(uri)
                                .setVibrate(new long[]{500, 1000})
                                .setWhen(System.currentTimeMillis());
                        notificationManager.notify(9785, builder.build());
                        jobDispatcher.cancelAll();
                    }else{
                        //Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });*/
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }
}
