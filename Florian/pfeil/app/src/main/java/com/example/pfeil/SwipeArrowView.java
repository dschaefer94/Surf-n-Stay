package com.example.pfeil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SwipeArrowView extends View {

    private Paint arrowPaint;
    private Path arrowPath = new Path();
    private Paint filledPaint;
    private PathMeasure pathMeasure;
    private float currentProgress = 0f;
    private Path filledPath = new Path();

    private float startX;

    public SwipeArrowView(Context context) {
        super(context);
        init();
    }

    public SwipeArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.BLACK);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(10f);

        filledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        filledPaint.setStyle(Paint.Style.STROKE);
        filledPaint.setStrokeWidth(10f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        arrowPath.reset();
        arrowPath.moveTo(0.1f * w, h / 2f);
        arrowPath.quadTo(0.5f * w, 0.4f * h, 0.9f * w, h / 2f);
        pathMeasure = new PathMeasure(arrowPath, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Immer den schwarzen Hintergrundpfad malen
        canvas.drawPath(arrowPath, arrowPaint);

        // Temporären Pinsel für die Pfeilspitze deklarieren
        Paint arrowheadPaint;

        // 2. Prüfen, ob eine Wisch-Geste aktiv ist
        if (currentProgress > 0) {
            // Farbe basierend auf dem Fortschritt berechnen
            int startColor = Color.RED;
            int endColor = Color.GREEN;
            float red = Color.red(startColor) * (1 - currentProgress) + Color.red(endColor) * currentProgress;
            float green = Color.green(startColor) * (1 - currentProgress) + Color.green(endColor) * currentProgress;
            float blue = Color.blue(startColor) * (1 - currentProgress) + Color.blue(endColor) * currentProgress;
            int currentColor = Color.rgb((int) red, (int) green, (int) blue);

            // Füll-Pinsel die Farbe zuweisen und Fortschrittsbalken malen
            filledPaint.setColor(currentColor);
            filledPath.reset();
            float stopD = currentProgress * pathMeasure.getLength();
            pathMeasure.getSegment(0, stopD, filledPath, true);
            canvas.drawPath(filledPath, filledPaint);

            // Den Füll-Pinsel auch für die Pfeilspitze verwenden
            arrowheadPaint = filledPaint;
        } else {
            // Wenn nicht gewischt wird, den schwarzen Pinsel für die Spitze nehmen
            arrowheadPaint = arrowPaint;
        }

        // 3. Pfeilspitze am Ende mit dem zuvor bestimmten Pinsel malen
        float[] pos = new float[2];
        float[] tan = new float[2];
        pathMeasure.getPosTan(pathMeasure.getLength(), pos, tan);
        float angleDegrees = (float) Math.toDegrees(Math.atan2(tan[1], tan[0]));

        canvas.save();
        canvas.rotate(angleDegrees, pos[0], pos[1]);
        float arrowSize = 30f;
        canvas.drawLine(pos[0], pos[1], pos[0] - arrowSize, pos[1] - arrowSize / 2, arrowheadPaint);
        canvas.drawLine(pos[0], pos[1], pos[0] - arrowSize, pos[1] + arrowSize / 2, arrowheadPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                if (currentProgress > 0) {
                    currentProgress = 0;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - startX;
                currentProgress = distance / (getWidth() * 0.8f);
                currentProgress = Math.max(0f, Math.min(currentProgress, 1f));
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (currentProgress >= 0.95f) {
                    currentProgress = 1f;
                    invalidate();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Context context = getContext();
                        Intent intent = new Intent(context, SecondActivity.class);
                        context.startActivity(intent);

                        currentProgress = 0f;
                        postInvalidate();
                    }, 200);

                } else {
                    currentProgress = 0f;
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
}
