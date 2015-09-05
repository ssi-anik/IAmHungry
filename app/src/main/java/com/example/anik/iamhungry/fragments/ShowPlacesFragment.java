package com.example.anik.iamhungry.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anik.iamhungry.R;
import com.example.anik.iamhungry.helpers.AppConstant;
import com.example.anik.iamhungry.helpers.AppStorage;
import com.example.anik.iamhungry.helpers.GeoLocation;
import com.example.anik.iamhungry.helpers.Restaurant;
import com.example.anik.iamhungry.httpService.HttpService;
import com.example.anik.iamhungry.httpService.IHttpService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Anik on 04-Sep-15, 004.
 */
public class ShowPlacesFragment extends Fragment implements Runnable {
    public static double latitude = 0.0;
    public static double longitude = 0.0;
    public static List<Restaurant> restaurantList = new ArrayList<>();
    private final int SHOW_RESTAURANT_LIST = 1;
    private final int SHOW_MAP = 2;
    private TextView locationName;
    private GeoLocation location;
    private Context context;
    private Activity activity;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        context = getActivity().getApplicationContext();

        View view = inflater.inflate(R.layout.layout_final, container, false);
        this.view = view;

        location = new GeoLocation(getActivity().getApplicationContext());

        locationName = (TextView) view.findViewById(R.id.tvUserLocationName);
        setHasOptionsMenu(true);
        new Thread(this).start();

        return view;
    }

    private void retrieveLocations() {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    private void changeLocationName() {
        Geocoder geocoder = new Geocoder(AppStorage.getCurrentAppContext(), Locale.getDefault());
        if (latitude == 0.0 || longitude == 0.0)
            return;
        try {
            Address address = geocoder.getFromLocation(latitude, longitude, 1).get(0);

            if (null == address) {
                return;
            }

            String locationRealName = "";
            for (int i = 0; i <= address.getMaxAddressLineIndex(); ++i) {
                locationRealName += (i > 0) ? ", " : "";
                locationRealName += address.getAddressLine(i);
            }

            if (locationRealName.isEmpty()) {
                locationRealName = "No location name is available";
            } else {
                locationRealName = String.format("Hungry Near: <u>%s</u>", locationRealName);
            }

            locationName.setText(Html.fromHtml(String.format("<b>%s</b>", locationRealName)));

        } catch (IOException e) {
            e.printStackTrace();
            locationName.setText(Html.fromHtml(String.format("<b>Finding your location...</b>")));
            return;
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (!AppStorage.isApplicationInForeground())
                    continue;
                try {
                    if (latitude != 0.0 && longitude != 0.0) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeLocationName();
                            }
                        });
                        Thread.currentThread().interrupt();
                    }
                    Thread.sleep(1 * AppConstant.TIME_SECONDS);
                    retrieveLocations();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_findRestaurant:
                clearFrameLayout();
                findRestaurants();
                return true;
            case R.id.menu_refresh:
                locationName.setText(Html.fromHtml(String.format("<b>Finding your location...</b>")));
                latitude = 0.0;
                longitude = 0.0;
                new Thread(this).start();
                clearFrameLayout();
                return true;
            default:
                return false;
        }
    }

    private void findRestaurants() {
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(context, "Your location is not ready. Wait for a while!", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = buildUrl(latitude, longitude);
        if (url.isEmpty()) {
            Toast.makeText(context, "Sorry, Something going wrong!", Toast.LENGTH_SHORT).show();
            return;
        }
        new HttpService(activity)
                .onUrl(url)
                .withMethod(HttpService.HTTP_GET)
                .registerResponse(new IHttpService() {
                    @Override
                    public void onResponseReceived(int statusCode, String response) {
                        try {
                            restaurantList.clear();
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("REQUEST_DENIED")) {
                                // request is denied
                                Toast.makeText(context, "Are you requesting something else?", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (status.equals("OVER_QUERY_LIMIT")) {
                                // request query limit exceed
                                Toast.makeText(context, "Sorry, Query limit exceeded. Try later!", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (status.equals("INVALID_REQUEST")) {
                                // location or radius unavailable
                                Toast.makeText(context, "Don't know how, but something is missing.", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (status.equals("ZERO_RESULTS")) {
                                // no result found for the query.
                                showInView(SHOW_RESTAURANT_LIST);
                                return;
                            }

                            JSONArray results = jsonResponse.getJSONArray("results");
                            for (int i = 0; i < results.length(); ++i) {

                                JSONObject currentRestaurant = results.getJSONObject(i);

                                JSONObject geometry = currentRestaurant.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                String restaurant_latitude = location.getString("lat");
                                String restaurant_longitude = location.getString("lng");

                                String restaurant_icon = currentRestaurant.getString("icon");
                                String restaurant_id = currentRestaurant.getString("id");
                                String restaurant_name = currentRestaurant.getString("name");
                                String restaurant_place_id = currentRestaurant.getString("place_id");

                                JSONArray restaurant_available_types = currentRestaurant.getJSONArray("types");
                                boolean isFoodType = false, isRestaurantType = false, isCafe = false;
                                for (int index = 0; index < restaurant_available_types.length(); ++index) {
                                    if (restaurant_available_types.getString(index).equals("food"))
                                        isFoodType = true;
                                    else if (restaurant_available_types.getString(index).equals("restaurant"))
                                        isRestaurantType = true;
                                    else if (restaurant_available_types.getString(index).equals("cafe"))
                                        isCafe = true;
                                }

                                String restaurant_type = isCafe ? "Cafe" : isRestaurantType ? "Restaurant" : isFoodType ? "Food" : "Others";
                                String restaurant_vicinity = currentRestaurant.getString("vicinity");

                                Restaurant restaurant = new Restaurant(restaurant_latitude, restaurant_longitude, restaurant_name, restaurant_icon, restaurant_id, restaurant_place_id, restaurant_type, restaurant_vicinity);
                                restaurantList.add(restaurant);
                            }
                            showInView(SHOW_RESTAURANT_LIST);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .execute();
    }

    private void showInView(int viewType) {
        clearFrameLayout();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (viewType == SHOW_RESTAURANT_LIST) {
            fragmentTransaction.replace(R.id.frameLayoutForDisplay, new RestaurantFragment());
            fragmentTransaction.commit();
        } else if (viewType == SHOW_MAP) {

        }
    }

    private String buildUrl(double latitude, double longitude) {
        try {
            return String.format("https://maps.googleapis.com/maps/api/place/search/json?location=%f,%f&radius=2000&sensor=false&key=%s&types=%s", latitude, longitude, AppConstant.APP_KEY, URLEncoder.encode(AppConstant.SEARCH_TYPES, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void clearFrameLayout() {
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.frameLayoutForDisplay);
        frameLayout.removeAllViews();
    }
}
