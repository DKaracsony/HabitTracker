package com.example.dk_habittracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.github.mikephil.charting.charts.BarChart;

public class ScrollableBarChart extends BarChart {

    private float startX, startY;
    private boolean isDragging = false;

    public ScrollableBarChart(Context context) {
        super(context);
    }

    public ScrollableBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollableBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float swipeThreshold = 30f;
        ViewParent parent = getParent();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                isDragging = false;
                parent.requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - startX;
                float dy = event.getY() - startY;

                if (!isDragging && Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > swipeThreshold) {
                    isDragging = true;
                    boolean isSwipingRight = dx > 0;
                    parent.requestDisallowInterceptTouchEvent(!(isSwipingRight && getLowestVisibleX() <= 0f));
                }
                break;

            case MotionEvent.ACTION_UP:
                parent.requestDisallowInterceptTouchEvent(false);
                if (!isDragging) performClick();
                isDragging = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                parent.requestDisallowInterceptTouchEvent(false);
                isDragging = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
