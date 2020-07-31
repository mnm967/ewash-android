package com.mnm.ewash;

import android.content.ContextWrapper;
import androidx.multidex.MultiDexApplication;

import com.pixplicity.easyprefs.library.Prefs;

public class MyApplication extends MultiDexApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }
}
