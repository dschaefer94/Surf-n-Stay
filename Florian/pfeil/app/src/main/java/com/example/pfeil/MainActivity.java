package com.example.pfeil;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure the top arrow (LTR)
        SwipeArrowView swipeArrowViewLtr = findViewById(R.id.swipeArrowView_ltr);
        swipeArrowViewLtr.setTargetActivityClass(SecondActivity.class);
        swipeArrowViewLtr.setSwipeDirection(SwipeArrowView.Direction.LTR);


        // Configure the bottom arrow (RTL)
        SwipeArrowView swipeArrowViewRtl = findViewById(R.id.swipeArrowView_rtl);
        swipeArrowViewRtl.setTargetActivityClass(MyOffersActivity.class);
        swipeArrowViewRtl.setSwipeDirection(SwipeArrowView.Direction.RTL);
    }
}