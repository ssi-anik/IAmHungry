package com.example.anik.iamhungry.helpers;

/**
 * Created by Anik on 04-Sep-15, 004.
 */
public class Restaurant {
    private String latitude = "";
    private String longitude = "";
    private String name = "";
    private String icon = "";
    private String id = "";
    private String placeId = "";
    private String type = "";
    private String vicinity = "";

    public Restaurant(String latitude, String longitude, String name, String icon, String id, String placeId, String type, String vicinity) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.icon = icon;
        this.id = id;
        this.placeId = placeId;
        this.type = type;
        this.vicinity = vicinity;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getType() {
        return type;
    }

    public String getVicinity() {
        return vicinity;
    }
}
