package com.example.anik.iamhungry.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GeoLocation implements LocationListener {
    private Context context;
    private Location location;
    private LocationManager gpsLocationManager, networkLocationManager;
    private double latitude;
    private double longitude;
    private boolean isGPSAvailable;
    private boolean isNetworkAvailable;

    public GeoLocation(Context context) {
        this.context = context;
        registerProvider();
    }

    private void registerProvider() {
        gpsLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        networkLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        isGPSAvailable = gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkAvailable = networkLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20 * AppConstant.TIME_SECONDS, 50, this);
        networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20 * AppConstant.TIME_SECONDS, 50, this);
    }

    public boolean isLocationAvailable() {
        /*gpsLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        networkLocationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);*/
        isGPSAvailable = gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkAvailable = networkLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGPSAvailable) return true;
        else if (isNetworkAvailable) return true;

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.getLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public void getLocation() {
        if (null == location) {
            return;
        }
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}