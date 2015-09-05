package com.example.anik.iamhungry.helpers;

import android.app.Application;
import android.content.Context;

/**
 * Created by Anik on 03-Sep-15, 003.
 */
public class AppStorage extends Application {
    private static AppStorage instance;
    private static boolean applicationInForeground = true;

    public static boolean isApplicationInForeground() {
        return applicationInForeground;
    }

    public static void setApplicationInForeground(boolean isApplicationLive) {
        AppStorage.applicationInForeground = isApplicationLive;
    }

    public static Context getCurrentAppContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}
