package com.example.anik.iamhungry.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anik.iamhungry.R;
import com.example.anik.iamhungry.RestaurantInformation;
import com.example.anik.iamhungry.helpers.AppConstant;
import com.example.anik.iamhungry.helpers.Restaurant;
import com.example.anik.iamhungry.httpService.HttpService;
import com.example.anik.iamhungry.httpService.IHttpService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anik on 04-Sep-15, 004.
 */
public class RestaurantFragment extends Fragment {

    private static Map<String, String> restaurantDetailsMap = new HashMap<>();
    private static String currentPlaceId = "";
    private Activity activity;
    private Context context;
    private ListView listView;
    private RestaurantListViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = getActivity();
        context = activity.getApplicationContext();
        View view = null;

        if (ShowPlacesFragment.restaurantList.size() > 0) {
            view = inflater.inflate(R.layout.layout_for_restaurant_list, container, false);
            listView = (ListView) view.findViewById(R.id.listViewForDisplayingRestaurant);
            adapter = new RestaurantListViewAdapter(context);
            listView.setAdapter(adapter);
            listView.setScrollingCacheEnabled(false);
            registerListViewItemClickListener();
        } else {
            view = inflater.inflate(R.layout.layout_for_empty_list, container, false);
        }
        return view;
    }

    private void registerListViewItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Restaurant checkingRestaurant = ShowPlacesFragment.restaurantList.get(position);
                String restaurant_place_id = checkingRestaurant.getPlaceId();
                final String restaurant_type = checkingRestaurant.getType();

                currentPlaceId = restaurant_place_id;
                if (restaurantDetailsMap.containsKey(restaurant_place_id)) {
                    String response = restaurantDetailsMap.get(restaurant_place_id);
                    toNextIntent(response, restaurant_type);
                } else {
                    String url = build_url(restaurant_place_id);
                    new HttpService(activity)
                            .onUrl(url)
                            .withMethod(HttpService.HTTP_GET)
                            .registerResponse(new IHttpService() {
                                @Override
                                public void onResponseReceived(int statusCode, String response) {
                                    try {
                                        JSONObject object = new JSONObject(response);
                                        String status = object.getString("status");
                                        if (status.equals("REQUEST_DENIED")) {
                                            // request is denied
                                            Toast.makeText(context, "Are you requesting something else?", Toast.LENGTH_SHORT).show();
                                            return;
                                        } else if (status.equals("OVER_QUERY_LIMIT")) {
                                            // request query limit exceed
                                            Toast.makeText(context, "Sorry, Query limit exceeded. Try later!", Toast.LENGTH_SHORT).show();
                                            return;
                                        } else if (status.equals("UNKNOWN_ERROR")) {
                                            // request unknown error
                                            Toast.makeText(context, "It's google who's facing error. Try later!", Toast.LENGTH_SHORT).show();
                                            return;
                                        } else if (status.equals("INVALID_REQUEST")) {
                                            // location or radius unavailable
                                            Toast.makeText(context, "Don't know how, but something is missing.", Toast.LENGTH_SHORT).show();
                                            return;
                                        } else if (status.equals("ZERO_RESULTS")) {
                                            // no result found for the query.
                                            Toast.makeText(context, "No result is found for you.", Toast.LENGTH_SHORT).show();
                                            return;
                                        } else if (status.equals("NOT_FOUND")) {
                                            // no result found for the query.
                                            Toast.makeText(context, "Developer messed up. Nothing is found!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        restaurantDetailsMap.put(currentPlaceId, response);
                                        toNextIntent(response, restaurant_type);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .execute();
                }
            }
        });
    }

    private void toNextIntent(String response, String restaurant_type) {
        Intent intent = new Intent(context, RestaurantInformation.class);
        intent.putExtra("response", response);
        intent.putExtra("restaurant_type", restaurant_type);
        startActivity(intent);
    }

    private String build_url(String place_id) {
        return String.format("https://maps.googleapis.com/maps/api/place/details/json?placeid=%s&key=%s", place_id, AppConstant.APP_KEY);
    }
}

class RestaurantListViewAdapter extends BaseAdapter {

    private Context context;
    private List<Restaurant> restaurantList = new ArrayList<>();
    private int lastPosition = -1;

    public RestaurantListViewAdapter(Context context) {
        this.context = context;
        this.restaurantList.addAll(ShowPlacesFragment.restaurantList);
    }

    @Override
    public int getCount() {
        return this.restaurantList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.restaurantList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_for_restaurant_row, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Restaurant restaurant = restaurantList.get(position);
        viewHolder.name.setText(restaurant.getName());
        viewHolder.near.setText(restaurant.getVicinity());
        viewHolder.type.setText(restaurant.getType());

        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.bottom_to_top : R.anim.top_to_bottom);
        convertView.startAnimation(animation);

        return convertView;
    }

    private class ViewHolder {
        TextView name;
        TextView type;
        TextView near;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.textViewRestaurantName);
            type = (TextView) view.findViewById(R.id.textViewRestaurantType);
            near = (TextView) view.findViewById(R.id.textViewRestaurantNear);
        }
    }
}
