package com.example.pfeil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class MyOffersActivity extends AppCompatActivity {

    private enum ToggleFeature {
        SAUNA, KAMIN, SMOKER, ANIMALS, WIFI
    }

    private static final int LOCATION_REQUEST_CODE = 1728813;
    private static final int DUMMY_ITEM_COUNT = 2;

    private TextView publishedTab;
    private TextView unpublishedTab;
    private LinearLayout cardContainer;
    private List<Offer> allOffers = new ArrayList<>();
    private LocationManager locationManager;
    private View currentlyExpandedCard = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);

        publishedTab = findViewById(R.id.publishedTab);
        unpublishedTab = findViewById(R.id.unpublishedTab);
        cardContainer = findViewById(R.id.cardContainer);

        setupTabs();
        loadOffersFromServer();
    }

    private void loadOffersFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/offers");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP error code: " + responseCode);
                }

                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String jsonString = s.hasNext() ? s.next() : "";
                s.close();
                conn.disconnect();

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Offer>>() {}.getType();
                allOffers = gson.fromJson(jsonString, listType);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Offers loaded: " + allOffers.size(), Toast.LENGTH_SHORT).show();
                    processOffersWithLocation();
                });

            } catch (FileNotFoundException e) {
                Log.e("NETWORK_ERROR", "Error loading offers: Server not reachable or endpoint not found.", e);
                runOnUiThread(() -> Toast.makeText(this, "Server not reachable. Is the local server running on port 8080?", Toast.LENGTH_LONG).show());
            }
            catch (Exception e) {
                Log.e("NETWORK_ERROR", "Error loading offers", e);
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void processOffersWithLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastKnownLocation != null) {
            calculateDistances(lastKnownLocation);
            sortOffersByDistance();
        } else {
            Toast.makeText(this, "Location not available, showing unsorted list.", Toast.LENGTH_SHORT).show();
        }
        
        // Initial display (assuming unpublished is the default)
        filterAndDisplayOffers(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            processOffersWithLocation();
        } else {
            Toast.makeText(this, "Offers cannot be sorted without location permission.", Toast.LENGTH_LONG).show();
            // Display offers anyway, just unsorted
            filterAndDisplayOffers(false);
        }
    }

    private void calculateDistances(Location currentLocation) {
        for (Offer offer : allOffers) {
            if (offer.lat < -90.0 || offer.lat > 90.0 || offer.lon < -180.0 || offer.lon > 180.0) {
                Log.w("MyOffersActivity", "Invalid coordinates for offer #" + offer.getId());
                offer.distance = Float.MAX_VALUE;
                continue;
            }
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    offer.lat, offer.lon, results);
            offer.distance = results[0];
        }
    }

    private void sortOffersByDistance() {
        Collections.sort(allOffers, (a, b) -> Double.compare(a.distance, b.distance));
    }

    private void filterAndDisplayOffers(boolean isPublished) {
        cardContainer.removeAllViews();
        currentlyExpandedCard = null;
        if (allOffers == null) return;
        
        List<Offer> filteredOffers = allOffers.stream()
                .filter(offer -> offer.isPublished() == isPublished)
                .collect(Collectors.toList());

        for (Offer offer : filteredOffers) {
            addCardToLayout(offer);
        }
    }

    private void addCardToLayout(Offer offer) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.card_item, cardContainer, false);

        TextView locationHeader = cardView.findViewById(R.id.locationHeader);
        ConstraintLayout expandableContent = cardView.findViewById(R.id.expandableContent);

        TextView priceTextView = cardView.findViewById(R.id.tvPrice);
        priceTextView.setText(offer.getPrice());
        
        TextView stayDateTextView = cardView.findViewById(R.id.tvStayDate);
        stayDateTextView.setText(offer.getDateRange());

        ImageView calendarIcon = cardView.findViewById(R.id.ivCalendarIcon);
        calendarIcon.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("Select a date range");
            final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                offer.selectedStartDate = selection.first;
                offer.selectedEndDate = selection.second;

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String startDate = sdf.format(new Date(selection.first));
                String endDate = sdf.format(new Date(selection.second));
                
                stayDateTextView.setText(startDate + " - " + endDate);
            });

            datePicker.show(getSupportFragmentManager(), datePicker.toString());
        });


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            if (offer.lat < -90.0 || offer.lat > 90.0 || offer.lon < -180.0 || offer.lon > 180.0) {
                Log.e("MyOffersActivity", "Invalid coordinates for offer #" + offer.getId());
                locationHeader.setText("Unknown Location - " + offer.getDateRange());
            } else {
                List<Address> addresses = geocoder.getFromLocation(offer.lat, offer.lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressLine = address.getAddressLine(0);
                    locationHeader.setText(addressLine + " - " + offer.getDateRange());
                } else {
                    locationHeader.setText("Unknown Location - " + offer.getDateRange());
                }
            }
        } catch (IOException e) {
            locationHeader.setText("Unknown Location - " + offer.getDateRange());
            Log.e("GEOCODER_ERROR", "Error getting address from coordinates", e);
        }

        locationHeader.setOnClickListener(v -> {
            if (expandableContent.getVisibility() == View.VISIBLE) {
                expandableContent.setVisibility(View.GONE);
                locationHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
                currentlyExpandedCard = null;
            } else {
                if (currentlyExpandedCard != null) {
                    ConstraintLayout oldExpandableContent = currentlyExpandedCard.findViewById(R.id.expandableContent);
                    oldExpandableContent.setVisibility(View.GONE);
                    TextView oldLocationHeader = currentlyExpandedCard.findViewById(R.id.locationHeader);
                    oldLocationHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
                }
                expandableContent.setVisibility(View.VISIBLE);
                locationHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
                currentlyExpandedCard = cardView;
            }
        });

        RecyclerView recyclerView = cardView.findViewById(R.id.recyclerView_beds);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setNestedScrollingEnabled(false);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < DUMMY_ITEM_COUNT; i++) data.add("");
        for (int i = 1; i <= 15; i++) data.add("" + i); // Assuming max 15 beds
        for (int i = 0; i < DUMMY_ITEM_COUNT; i++) data.add("");

        WheelAdapter adapter = new WheelAdapter(data);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        new PagerSnapHelper().attachToRecyclerView(recyclerView);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to avoid it being called multiple times
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                View firstChild = recyclerView.getChildAt(0);
                if (firstChild == null) return;
                int itemWidth = firstChild.getWidth();
                int padding = (recyclerView.getWidth() / 2) - (itemWidth / 2);
                recyclerView.setClipToPadding(false);
                recyclerView.setPadding(padding, 0, padding, 0);
                int position = offer.getBeds() - 1 + DUMMY_ITEM_COUNT;
                if (position >= 0 && position < adapter.getItemCount()) {
                    layoutManager.scrollToPosition(position);
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateItemAppearance(recyclerView);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateItemAppearance(recyclerView);
                }
            }
        });

        setupToggleButton(cardView, offer, ToggleFeature.SAUNA);
        setupToggleButton(cardView, offer, ToggleFeature.KAMIN);
        setupToggleButton(cardView, offer, ToggleFeature.SMOKER);
        setupToggleButton(cardView, offer, ToggleFeature.ANIMALS);
        setupToggleButton(cardView, offer, ToggleFeature.WIFI);
        
        cardContainer.addView(cardView);
    }
    
    private void updateItemAppearance(@NonNull RecyclerView recyclerView) {
        final float maxScale = 1.5f;
        float recyclerViewCenterX = recyclerView.getWidth() / 2f;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            if (child == null) continue;

            float childCenterX = (child.getLeft() + child.getRight()) / 2f;
            float distance = Math.abs(recyclerViewCenterX - childCenterX);
            float effectZone = recyclerView.getWidth() / 2f;

            if (effectZone <= 0) continue;

            float scaleFactor = Math.max(0, 1 - (distance / effectZone));
            float scale = 1.0f + (maxScale - 1.0f) * scaleFactor;
            child.setScaleX(scale);
            child.setScaleY(scale);

            TextView textView = child.findViewById(R.id.tvNumber);
            if (textView != null) {
                textView.setTextColor(scale > maxScale - 0.05f ? Color.parseColor("#FFA500") : Color.BLACK);
            }
        }
    }
    
    private void setupToggleButton(View cardView, Offer offer, ToggleFeature feature) {
        View container;
        TextView button;
        boolean isInitiallyYes;

        switch (feature) {
            case SAUNA:
                container = cardView.findViewById(R.id.containerSauna);
                button = cardView.findViewById(R.id.toggleSauna);
                isInitiallyYes = offer.hasSauna();
                break;
            case KAMIN:
                container = cardView.findViewById(R.id.containerKamin);
                button = cardView.findViewById(R.id.toggleKamin);
                isInitiallyYes = offer.hasKamin();
                break;
            case SMOKER:
                container = cardView.findViewById(R.id.containerSmoker);
                button = cardView.findViewById(R.id.toggleSmoker);
                isInitiallyYes = offer.hasSmoker();
                Log.d("SmokerValue", "Offer #" + offer.getId() + " hasSmoker: " + isInitiallyYes);
                break;
            case ANIMALS:
                container = cardView.findViewById(R.id.containerAnimals);
                button = cardView.findViewById(R.id.toggleAnimals);
                isInitiallyYes = offer.hasAnimals();
                break;
            case WIFI:
                container = cardView.findViewById(R.id.containerWifi);
                button = cardView.findViewById(R.id.toggleWifi);
                isInitiallyYes = offer.hasWifi();
                break;
            default:
                return;
        }

        updateToggleButton(button, isInitiallyYes);
        boolean isEnabled = !offer.isPublished();
        button.setEnabled(isEnabled);
        container.setAlpha(isEnabled ? 1.0f : 0.5f);

        if (isEnabled) {
            final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    boolean isCurrentlyYes = "YES".equals(button.getText().toString());
                    boolean newStatus = !isCurrentlyYes;
                    updateToggleButton(button, newStatus);
                    switch (feature) {
                        case SAUNA:
                            offer.setHasSauna(newStatus);
                            break;
                        case KAMIN:
                            offer.setHasKamin(newStatus);
                            break;
                        case SMOKER:
                            offer.setHasSmoker(newStatus);
                            break;
                        case ANIMALS:
                            offer.setHasAnimals(newStatus);
                            break;
                        case WIFI:
                            offer.setHasWifi(newStatus);
                            break;
                    }
                    return true;
                }
            });

            button.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        } else {
            button.setOnTouchListener(null);
        }
    }

    private void updateToggleButton(TextView button, boolean isYes) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        if (isYes) {
            button.setText("YES");
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.button_toggle_yes));
            button.setTextColor(Color.WHITE);
            params.horizontalBias = 1.0f;
        } else {
            button.setText("NO");
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.button_toggle_no));
            button.setTextColor(Color.parseColor("#FFA500"));
            params.horizontalBias = 0.0f;
        }
        button.setLayoutParams(params);
    }
    
    private void setupTabs() {
        selectTab(unpublishedTab);
        unselectTab(publishedTab);

        unpublishedTab.setOnClickListener(v -> {
            selectTab(unpublishedTab);
            unselectTab(publishedTab);
            filterAndDisplayOffers(false);
        });

        publishedTab.setOnClickListener(v -> {
            selectTab(publishedTab);
            unselectTab(unpublishedTab);
            filterAndDisplayOffers(true);
        });
    }

    private void selectTab(TextView tab) {
        tab.setBackground(ContextCompat.getDrawable(this, R.drawable.tab_selected_background));
        tab.setTextColor(Color.WHITE);
    }

    private void unselectTab(TextView tab) {
        tab.setBackground(ContextCompat.getDrawable(this, R.drawable.tab_unselected_background));
        tab.setTextColor(Color.parseColor("#FFA500"));
    }
}
