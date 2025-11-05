package com.example.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureHandler extends GestureDetector.SimpleOnGestureListener {

    public interface GestureListener {
        void onSwipeLeft();
        void onSwipeRight();
        void onDoubleTap();
    }

    private final GestureListener listener;

    public GestureHandler(GestureListener listener) {
        this.listener = listener;
    }

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) listener.onSwipeRight();
            else listener.onSwipeLeft();
            return true;
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        listener.onDoubleTap();
        return true;
    }
}
