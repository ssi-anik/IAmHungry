package com.example.anik.iamhungry;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.anik.iamhungry.fragments.ShowPlacesFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class RestaurantInformation extends Activity implements View.OnTouchListener {

    String restaurant_latitude = "";
    String restaurant_longitude = "";
    String restaurant_formatted_address = "";
    String restaurant_name = "";
    String restaurant_type = "";
    String restaurant_place_id = "";
    String restaurant_formatted_phone_number = "";
    String restaurant_international_phone_number = "";
    String restaurant_open_now = "";
    String restaurant_rating = "";
    String restaurant_vicinity = "";
    private RelativeLayout baseLayout;
    private int previousFingerPosition = 0;
    private int baseLayoutPosition = 0;
    private int defaultViewHeight;
    private boolean isClosing = false;
    private boolean isScrollingUp = false;
    private boolean isScrollingDown = false;
    private TextView drag_suggestion_text_view;
    private String response = "";
    private TextView tvRestaurantName;
    private TextView tvRestaurantNear;
    private TextView tvRestaurantType;
    private TextView tvRestaurantOpen;
    private TextView tvRestaurantPhoneNumber;
    private TextView tvRestaurantRating;
    private Button buttonShowMeRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_information);

        tvRestaurantName = (TextView) findViewById(R.id.restaurantName);
        tvRestaurantNear = (TextView) findViewById(R.id.restaurantNear);
        tvRestaurantType = (TextView) findViewById(R.id.restaurantType);
        tvRestaurantOpen = (TextView) findViewById(R.id.restaurantOpen);
        tvRestaurantPhoneNumber = (TextView) findViewById(R.id.restaurantPhoneNumber);
        tvRestaurantRating = (TextView) findViewById(R.id.restaurantRating);
        buttonShowMeRoute = (Button) findViewById(R.id.buttonShowRoute);

        response = getIntent().getStringExtra("response");
        restaurant_type = getIntent().getStringExtra("restaurant_type");

        showRestaurantDetails();
        animateDraggableText();
        registerTouchEvent();

        buttonShowMeRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)", ShowPlacesFragment.latitude, ShowPlacesFragment.longitude, "You're feeling HUNGRY here", Double.parseDouble(restaurant_latitude), Double.parseDouble(restaurant_longitude), "Lessen your HUNGRINESS here");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });

    }

    private void showRestaurantDetails() {
        try {
            JSONObject object = new JSONObject(response);
            JSONObject restaurant = object.getJSONObject("result");

            restaurant_latitude = restaurant.getJSONObject("geometry").getJSONObject("location").getString("lat");
            restaurant_longitude = restaurant.getJSONObject("geometry").getJSONObject("location").getString("lng");
            restaurant_formatted_address = restaurant.has("formatted_address") ? restaurant.getString("formatted_address") : "";
            restaurant_name = restaurant.has("name") ? restaurant.getString("name") : "";
            restaurant_place_id = restaurant.has("place_id") ? restaurant.getString("place_id") : "";
            restaurant_formatted_phone_number = restaurant.has("formatted_phone_number") ? restaurant.getString("formatted_phone_number") : "";
            restaurant_international_phone_number = restaurant.has("international_phone_number") ? restaurant.getString("international_phone_number") : "";
            restaurant_open_now = restaurant.has("opening_hours") ? restaurant.getJSONObject("opening_hours").getString("open_now") : "";
            restaurant_rating = restaurant.has("rating") ? restaurant.getString("rating") : "";
            restaurant_vicinity = restaurant.has("vicinity") ? restaurant.getString("vicinity") : "";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (restaurant_name.isEmpty()) {
            tvRestaurantName.setText("Unfortunately, No name is found for this");
        } else {
            tvRestaurantName.setText(restaurant_name);
        }

        if (null == restaurant_type || restaurant_type.isEmpty()) {
            tvRestaurantType.setText(String.format("Restaurant type is not known."));
        } else {
            tvRestaurantType.setText(String.format("Restaurant type: %s", restaurant_type));
        }

        if (restaurant_formatted_address.isEmpty()) {
            tvRestaurantNear.setText("No sure near what. :(");
        } else {
            tvRestaurantNear.setText(String.format("Near: %s", restaurant_formatted_address));
        }

        if (restaurant_open_now.isEmpty()) {
            tvRestaurantOpen.setText(Html.fromHtml("Restaurant is now <u><b>CLOSE</b></u>."));
        } else {
            tvRestaurantOpen.setText(Html.fromHtml("Restaurant is now <u><b>OPEN</b></u>."));
        }

        if (restaurant_formatted_phone_number.isEmpty()) {
            //tvRestaurantPhoneNumber.setText("No phone number is found.");
            ((TextView) findViewById(R.id.restaurantPhoneNumberNotFound)).setVisibility(View.VISIBLE);
            tvRestaurantPhoneNumber.setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.restaurantPhoneNumberNotFound)).setVisibility(View.GONE);
            tvRestaurantPhoneNumber.setText(String.format("Phone number: %s", restaurant_international_phone_number));
        }

        if (restaurant_rating.isEmpty()) {
            tvRestaurantRating.setText("No rating is given.");
        } else {
            tvRestaurantRating.setText(String.format("Rating given: %s", restaurant_rating));
        }
    }

    private void animateDraggableText() {
        drag_suggestion_text_view = (TextView) findViewById(R.id.dragSuggestion);
        Animation animation = AnimationUtils.loadAnimation(RestaurantInformation.this, R.anim.bottom_to_top_for_header);
        drag_suggestion_text_view.startAnimation(animation);
    }

    private void registerTouchEvent() {
        baseLayout = (RelativeLayout) findViewById(R.id.base_popup_layout);
        baseLayout.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        // Get finger position on screen
        final int Y = (int) event.getRawY();

        // Switch on motion event type
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // save default base layout height
                defaultViewHeight = baseLayout.getHeight();

                // Init finger and view position
                previousFingerPosition = Y;
                baseLayoutPosition = (int) baseLayout.getY();
                break;

            case MotionEvent.ACTION_UP:
                // If user was doing a scroll up
                if (isScrollingUp) {
                    // Reset baselayout position
                    baseLayout.setY(0);
                    // We are not in scrolling up mode anymore
                    isScrollingUp = false;
                }

                // If user was doing a scroll down
                if (isScrollingDown) {
                    // Reset baselayout position
                    baseLayout.setY(0);
                    // Reset base layout size
                    baseLayout.getLayoutParams().height = defaultViewHeight;
                    baseLayout.requestLayout();
                    // We are not in scrolling down mode anymore
                    isScrollingDown = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isClosing) {
                    int currentYPosition = (int) baseLayout.getY();

                    // If we scroll up
                    if (previousFingerPosition > Y) {
                        // First time android rise an event for "up" move
                        if (!isScrollingUp) {
                            isScrollingUp = true;
                        }

                        // Has user scroll down before -> view is smaller than it's default size -> resize it instead of change it position
                        if (baseLayout.getHeight() < defaultViewHeight) {
                            baseLayout.getLayoutParams().height = baseLayout.getHeight() - (Y - previousFingerPosition);
                            baseLayout.requestLayout();
                        } else {
                            // Has user scroll enough to "auto close" popup ?
                            if ((baseLayoutPosition - currentYPosition) > defaultViewHeight / 4) {
                                closeUpAndDismissDialog(currentYPosition);
                                return true;
                            }

                            //
                        }
                        baseLayout.setY(baseLayout.getY() + (Y - previousFingerPosition));

                    }
                    // If we scroll down
                    else {

                        // First time android rise an event for "down" move
                        if (!isScrollingDown) {
                            isScrollingDown = true;
                        }

                        // Has user scroll enough to "auto close" popup ?
                        if (Math.abs(baseLayoutPosition - currentYPosition) > defaultViewHeight / 2) {
                            closeDownAndDismissDialog(currentYPosition);
                            return true;
                        }

                        // Change base layout size and position (must change position because view anchor is top left corner)
                        baseLayout.setY(baseLayout.getY() + (Y - previousFingerPosition));
                        baseLayout.getLayoutParams().height = baseLayout.getHeight() - (Y - previousFingerPosition);
                        baseLayout.requestLayout();
                    }

                    // Update position
                    previousFingerPosition = Y;
                }
                break;
        }
        return true;
    }

    public void closeUpAndDismissDialog(int currentPosition) {
        isClosing = true;
        ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(baseLayout, "y", currentPosition, -baseLayout.getHeight());
        positionAnimator.setDuration(400);
        positionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        positionAnimator.start();
    }

    public void closeDownAndDismissDialog(int currentPosition) {
        isClosing = true;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        ObjectAnimator positionAnimator = ObjectAnimator.ofFloat(baseLayout, "y", currentPosition, screenHeight + baseLayout.getHeight());
        positionAnimator.setDuration(400);
        positionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        positionAnimator.start();
    }

}
