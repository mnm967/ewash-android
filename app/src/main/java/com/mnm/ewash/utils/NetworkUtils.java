package com.mnm.ewash.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import androidx.appcompat.app.AlertDialog;
import android.view.ContextThemeWrapper;

import com.mnm.ewash.HomeActivity;
import com.mnm.ewash.R;

public class NetworkUtils {
    public static boolean isNetworkConnected(final Activity activity, DialogInterface.OnClickListener onConnectionFailedListener) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (!connected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.DialogCustom))
                    .setTitle("Connection Failed")
                    .setMessage("Please check your internet connection.")
                    .setPositiveButton("RETRY", onConnectionFailedListener)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            activity.finish();
                        }
                    })
                    .setCancelable(true);
            builder.create().show();
            return false;
        } else {
            return true;
        }
    }

    public static boolean isHomeNetworkConnected(final HomeActivity activity, DialogInterface.OnClickListener onConnectionFailedListener) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (!connected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.DialogCustom))
                    .setTitle("Connection Failed")
                    .setMessage("Please check your internet connection.")
                    .setPositiveButton("RETRY", onConnectionFailedListener)
                    .setCancelable(true);
            builder.create().show();
            return false;
        } else {
            return true;
        }
    }

    public static boolean isViewNetworkConnected(final Activity activity, DialogInterface.OnClickListener onConnectionFailedListener) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        if (!connected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.DialogCustom))
                    .setTitle("Connection Failed")
                    .setMessage("Please check your internet connection.")
                    .setPositiveButton("RETRY", onConnectionFailedListener)
                    .setCancelable(false);
            builder.create().show();
            return false;
        } else {
            return true;
        }
    }
}
