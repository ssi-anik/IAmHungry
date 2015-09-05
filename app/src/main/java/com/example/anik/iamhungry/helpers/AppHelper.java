package com.example.anik.iamhungry.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Anik on 03-Sep-15, 003.
 */
public class AppHelper {

    public static boolean isNetworkAvailable() {
        Context context = AppStorage.getCurrentAppContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
