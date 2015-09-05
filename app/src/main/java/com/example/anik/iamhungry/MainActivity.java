package com.example.anik.iamhungry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.anik.iamhungry.fragments.GPSDisabledFragment;
import com.example.anik.iamhungry.fragments.InternetDisabledFragment;
import com.example.anik.iamhungry.fragments.ShowPlacesFragment;
import com.example.anik.iamhungry.helpers.AppHelper;
import com.example.anik.iamhungry.helpers.AppStorage;
import com.example.anik.iamhungry.helpers.GeoLocation;


public class MainActivity extends AppCompatActivity {

    private static boolean internetConnectionChangedBroadcastReceived = false;
    private static boolean gpsProviderChangedBroadcastReceived = false;
    final Context context = MainActivity.this;
    private final int ACTIVITY_RESULT_LOCATION = 101;
    private final int ACTIVITY_RESULT_INTERNET = 102;
    GeoLocation location = null;
    Intent intent;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    BroadcastReceiver internetBroadcastReceiver;
    BroadcastReceiver locationProviderChangedBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        location = new GeoLocation(MainActivity.this);
        changeViewBasedOnSettingsChanged();
        registerInternetBroadcastReceiver();
        registerLocationChangeBroadcastReceiver();
    }

    private void changeViewBasedOnSettingsChanged() {
        boolean isLocationAvailable = location.isLocationAvailable();
        boolean isNetworkAvailable = AppHelper.isNetworkAvailable();

        fragmentTransaction = fragmentManager.beginTransaction();

        if (!isLocationAvailable) {
            GPSDisabledFragment gpsDisabledFragment = new GPSDisabledFragment();
            fragmentTransaction.replace(R.id.frameLayoutForFragment, gpsDisabledFragment);
            fragmentTransaction.commit();
        } else if (!isNetworkAvailable) {
            InternetDisabledFragment internetDisabledFragment = new InternetDisabledFragment();
            fragmentTransaction.replace(R.id.frameLayoutForFragment, internetDisabledFragment);
            fragmentTransaction.commit();
        } else if (isNetworkAvailable && isLocationAvailable) {
            ShowPlacesFragment showPlacesFragment = new ShowPlacesFragment();
            fragmentTransaction.replace(R.id.frameLayoutForFragment, showPlacesFragment);
            fragmentTransaction.commit();
        }
    }

    private void registerLocationChangeBroadcastReceiver() {
        locationProviderChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!AppStorage.isApplicationInForeground()) return;

                if (!location.isLocationAvailable() || !gpsProviderChangedBroadcastReceived) {
                    gpsProviderChangedBroadcastReceived = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeViewBasedOnSettingsChanged();
                        }
                    });
                } else {
                    if (gpsProviderChangedBroadcastReceived) {
                        gpsProviderChangedBroadcastReceived = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeViewBasedOnSettingsChanged();
                            }
                        });
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        registerReceiver(locationProviderChangedBroadcastReceiver, intentFilter);
    }

    private void registerInternetBroadcastReceiver() {
        internetBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!AppStorage.isApplicationInForeground()) return;

                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                // network not available, not connected
                if (networkInfo == null || !networkInfo.isConnected()) {
                    internetConnectionChangedBroadcastReceived = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeViewBasedOnSettingsChanged();
                        }
                    });
                } else {
                    if (!internetConnectionChangedBroadcastReceived) {
                        internetConnectionChangedBroadcastReceived = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeViewBasedOnSettingsChanged();
                            }
                        });
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(internetBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        if (null != internetBroadcastReceiver) {
            unregisterReceiver(internetBroadcastReceiver);
            internetBroadcastReceiver = null;
        }
        if (null != locationProviderChangedBroadcastReceiver) {
            unregisterReceiver(locationProviderChangedBroadcastReceiver);
            locationProviderChangedBroadcastReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppStorage.setApplicationInForeground(true);
        registerInternetBroadcastReceiver();
        registerLocationChangeBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        AppStorage.setApplicationInForeground(false);
        unregisterBroadcastReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        unregisterBroadcastReceiver();
        super.onStop();
    }
}
