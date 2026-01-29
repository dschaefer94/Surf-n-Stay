package com.example.pfeil;

import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyOffersActivity extends AppCompatActivity {

    private TextView publishedTab;
    private TextView unpublishedTab;
    private LinearLayout cardContainer;
    private List<Offer> allOffers;
    private static final int DUMMY_ITEM_COUNT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);

        publishedTab = findViewById(R.id.publishedTab);
        unpublishedTab = findViewById(R.id.unpublishedTab);
        cardContainer = findViewById(R.id.cardContainer);

        setupTabs();
        loadOffers();
    }

    private void loadOffers() {
        // Dummy data - replace with your actual database query
        allOffers = new ArrayList<>();
        allOffers.add(new Offer("HVIDE SANDE", "31.01 - 01.02", "A Hug & 2 Jokes", 1, true, false, false, false, true, true));
        allOffers.add(new Offer("SØNDERVIG", "02.02 - 03.02", "A Smile", 2, false, true, true, false, true, false));
        allOffers.add(new Offer("RINGKØBING", "04.02 - 05.02", "A High Five", 6, true, true, false, true, false, true));

        // Initial display
        filterAndDisplayOffers(false);
    }

    private void filterAndDisplayOffers(boolean isPublished) {
        cardContainer.removeAllViews();
        List<Offer> filteredOffers = allOffers.stream()
                .filter(offer -> offer.isPublished() == isPublished)
                .collect(Collectors.toList());

        for (Offer offer : filteredOffers) {
            addCardToLayout(offer);
        }
    }

    private void updateItemAppearance(@NonNull RecyclerView recyclerView) {
        final float maxScale = 1.5f;
        final float minScale = 1.0f;
        float recyclerViewCenterX = recyclerView.getWidth() / 2f;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            if (child == null) continue;

            float childCenterX = (child.getLeft() + child.getRight()) / 2f;
            float distance = Math.abs(recyclerViewCenterX - childCenterX);
            float effectZone = recyclerView.getWidth() / 2f;

            if (effectZone <= 0) continue;

            float scaleFactor = Math.max(0, 1 - (distance / effectZone));
            float scale = minScale + (maxScale - minScale) * scaleFactor;
            child.setScaleX(scale);
            child.setScaleY(scale);

            TextView textView = child.findViewById(R.id.tvNumber);
            if (textView != null) {
                if (scale > maxScale - 0.05f) {
                    textView.setTextColor(Color.parseColor("#FFA500")); // Orange
                } else {
                    textView.setTextColor(Color.BLACK); // Default color
                }
            }
        }
    }

    private void addCardToLayout(Offer offer) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.card_item, cardContainer, false);

        TextView locationHeader = cardView.findViewById(R.id.locationHeader);
        ConstraintLayout expandableContent = cardView.findViewById(R.id.expandableContent); // Corrected this line

        locationHeader.setText(offer.getLocation() + " - " + offer.getDateRange());

        locationHeader.setOnClickListener(v -> {
            if (expandableContent.getVisibility() == View.VISIBLE) {
                expandableContent.setVisibility(View.GONE);
                locationHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
            } else {
                expandableContent.setVisibility(View.VISIBLE);
                locationHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
            }
        });

        // Setup RecyclerView for beds
        RecyclerView recyclerView = cardView.findViewById(R.id.recyclerView_beds);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        List<String> data = new ArrayList<>();

        for (int i = 0; i < DUMMY_ITEM_COUNT; i++) {
            data.add("");
        }
        for (int i = 1; i <= 15; i++) {
            data.add("" + i);
        }
        for (int i = 0; i < DUMMY_ITEM_COUNT; i++) {
            data.add("");
        }

        WheelAdapter adapter = new WheelAdapter(data);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        int spacingInPixels = 80;
        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
        recyclerView.post(() -> {
            View firstChild = recyclerView.getChildAt(0);
            if (firstChild == null) return;
            int itemWidth = firstChild.getWidth();
            int padding = (recyclerView.getWidth() / 2) - (itemWidth / 2);
            recyclerView.setClipToPadding(false);
            recyclerView.setPadding(padding, 0, padding, 0);
            int position = offer.getBeds() - 1 + DUMMY_ITEM_COUNT;
            if (position >= 0 && position < adapter.getItemCount()) {
                layoutManager.scrollToPosition(position);
                recyclerView.post(() -> updateItemAppearance(recyclerView));
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

        if (offer.isPublished()) {
            GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });

            recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    return gestureDetector.onTouchEvent(e);
                }
                @Override
                public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}
                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
            });
        }

        // Setup Toggle Buttons
        setupToggleButton(cardView.findViewById(R.id.containerSauna), cardView.findViewById(R.id.toggleSauna), offer.hasSauna(), !offer.isPublished());
        setupToggleButton(cardView.findViewById(R.id.containerKamin), cardView.findViewById(R.id.toggleKamin), offer.hasKamin(), !offer.isPublished());
        setupToggleButton(cardView.findViewById(R.id.containerSmoker), cardView.findViewById(R.id.toggleSmoker), offer.hasSmoker(), !offer.isPublished());
        setupToggleButton(cardView.findViewById(R.id.containerAnimals), cardView.findViewById(R.id.toggleAnimals), offer.hasAnimals(), !offer.isPublished());
        setupToggleButton(cardView.findViewById(R.id.containerWifi), cardView.findViewById(R.id.toggleWifi), offer.hasWifi(), !offer.isPublished());

        cardContainer.addView(cardView);
    }

    private void setupToggleButton(View container, TextView button, boolean isInitiallyYes, boolean isEnabled) {
        final boolean[] isYes = {isInitiallyYes};
        updateToggleButton(button, isYes[0]);
        if (isEnabled) {
            container.setOnClickListener(v -> {
                isYes[0] = !isYes[0];
                updateToggleButton(button, isYes[0]);
            });
        }
    }

    private void updateToggleButton(TextView button, boolean isYes) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
        if (isYes) {
            button.setText("YES");
            button.setBackgroundResource(R.drawable.toggle_yes_background);
            params.horizontalBias = 1.0f;
        } else {
            button.setText("NO");
            button.setBackgroundResource(R.drawable.toggle_no_background);
            params.horizontalBias = 0.0f;
        }
        button.setLayoutParams(params);
    }
    
    private void setupTabs() {
        publishedTab.setOnClickListener(v -> {
            selectTab(publishedTab);
            unselectTab(unpublishedTab);
            Toast.makeText(this, "Fetching published data...", Toast.LENGTH_SHORT).show();
            filterAndDisplayOffers(true);
        });

        unpublishedTab.setOnClickListener(v -> {
            selectTab(unpublishedTab);
            unselectTab(publishedTab);
            Toast.makeText(this, "Fetching unpublished data...", Toast.LENGTH_SHORT).show();
            filterAndDisplayOffers(false);
        });
        
        // Initial state
        selectTab(unpublishedTab);
        unselectTab(publishedTab);
    }

    private void selectTab(TextView tab) {
        tab.setBackgroundColor(Color.parseColor("#0097A7"));
        tab.setTextColor(Color.WHITE);
    }



    private void unselectTab(TextView tab) {
        tab.setBackgroundColor(Color.parseColor("#333333"));
        tab.setTextColor(Color.parseColor("#888888"));
    }
}