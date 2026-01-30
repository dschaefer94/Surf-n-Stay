package com.example.pfeil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SecondActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 4711;
    private static final int DUMMY_ITEM_COUNT = 2; // For padding the wheel

    private List<Offer> allOffers = new ArrayList<>();
    private RecyclerView rvOffers;
    private OfferAdapter offerAdapter;

    private RecyclerView rvDistanceSpinner;
    private int selectedDistanceKm = 15; // Default distance
    private LocationManager locationManager;
    private Location activeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        initializeUI();
        setupDistanceSpinner();
        loadOffersFromServer();
        getCurrentLocation();
    }

    private void initializeUI() {
        rvOffers = findViewById(R.id.rvOffers);
        rvOffers.setLayoutManager(new LinearLayoutManager(this));
        offerAdapter = new OfferAdapter(new ArrayList<>());
        rvOffers.setAdapter(offerAdapter);

        rvDistanceSpinner = findViewById(R.id.rvDistanceSpinner);
    }

    private void setupDistanceSpinner() {
        rvDistanceSpinner.setOverScrollMode(View.OVER_SCROLL_NEVER);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < DUMMY_ITEM_COUNT; i++) data.add("");
        for (int i = 1; i <= 30; i++) data.add(String.valueOf(i)); // Distances 1-30 km
        for (int i = 0; i < DUMMY_ITEM_COUNT; i++) data.add("");

        WheelAdapter adapter = new WheelAdapter(data);
        rvDistanceSpinner.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvDistanceSpinner.setLayoutManager(layoutManager);
        new PagerSnapHelper().attachToRecyclerView(rvDistanceSpinner);

        rvDistanceSpinner.post(() -> {
            View firstChild = rvDistanceSpinner.getChildAt(0);
            if (firstChild == null) return;
            int itemWidth = firstChild.getWidth();
            int padding = (rvDistanceSpinner.getWidth() / 2) - (itemWidth / 2);
            rvDistanceSpinner.setClipToPadding(false);
            rvDistanceSpinner.setPadding(padding, 0, padding, 0);

            int initialPosition = selectedDistanceKm - 1 + DUMMY_ITEM_COUNT;
            if (initialPosition >= 0 && initialPosition < adapter.getItemCount()) {
                layoutManager.scrollToPosition(initialPosition);
            }
        });

        rvDistanceSpinner.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    int centerPosition = findCenterPosition(layoutManager);
                    String selectedValue = adapter.getItem(centerPosition);
                    if (selectedValue != null && !selectedValue.isEmpty()) {
                        selectedDistanceKm = Integer.parseInt(selectedValue);
                        filterAndDisplayOffers();
                    }
                }
            }
        });
    }
    
    private int findCenterPosition(LinearLayoutManager layoutManager) {
        return layoutManager.findFirstVisibleItemPosition() + (layoutManager.getChildCount() / 2);
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
                textView.setTextColor(scale > maxScale - 0.05f ? Color.parseColor("#FFA500") : Color.WHITE);
            }
        }
    }

    private void loadOffersFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:8080/offers");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String jsonString = s.hasNext() ? s.next() : "";
                s.close();
                conn.disconnect();

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Offer>>() {}.getType();
                List<Offer> loadedOffers = gson.fromJson(jsonString, listType);

                runOnUiThread(() -> {
                    if (loadedOffers != null) {
                        allOffers.clear();
                        allOffers.addAll(loadedOffers);
                        updateDistances();
                        filterAndDisplayOffers();
                    }
                });
            } catch (Exception e) {
                Log.e("NETWORK_ERROR", "Fehler beim Laden der Angebote", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to load offers.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    
    private void getCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        activeLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (activeLocation != null) {
            updateDistances();
            filterAndDisplayOffers();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required to filter by distance.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateDistances() {
        if (activeLocation == null) return;
        for (Offer offer : allOffers) {
            float[] results = new float[1];
            Location.distanceBetween(
                activeLocation.getLatitude(), activeLocation.getLongitude(),
                offer.getLat(), offer.getLon(),
                results
            );
            offer.distance = results[0];
        }
    }

    private void filterAndDisplayOffers() {
        List<Offer> filteredOffers = allOffers.stream()
                .filter(Offer::isPublished)
                //.filter(offer -> activeLocation == null || offer.distance <= selectedDistanceKm * 1000)
                .collect(Collectors.toList());

        Collections.sort(filteredOffers, (a, b) -> Double.compare(a.distance, b.distance));
        
        offerAdapter.setAngebote(filteredOffers);
    }
}