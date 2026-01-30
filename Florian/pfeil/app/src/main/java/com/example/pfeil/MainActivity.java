package com.example.pfeil;

import android.os.Bundle;
import android.widget.TextView;
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

        TextView textSwipeToStay = findViewById(R.id.text_SwipetoStay);
        swipeArrowViewLtr.setOnColorChangedListener(new SwipeArrowView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                textSwipeToStay.setTextColor(color);
            }
        });

        // Configure the bottom arrow (RTL)
        SwipeArrowView swipeArrowViewRtl = findViewById(R.id.swipeArrowView_rtl);
        swipeArrowViewRtl.setTargetActivityClass(MyOffersActivity.class);
        swipeArrowViewRtl.setSwipeDirection(SwipeArrowView.Direction.RTL);

        TextView textSwipeToHost = findViewById(R.id.textSwipetohost);
        swipeArrowViewRtl.setOnColorChangedListener(new SwipeArrowView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                textSwipeToHost.setTextColor(color);
            }
        });
    }
}