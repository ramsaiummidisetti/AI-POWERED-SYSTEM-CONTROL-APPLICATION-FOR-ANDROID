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

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX > 2000) listener.onSwipeRight();
        else if (velocityX < -2000) listener.onSwipeLeft();
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        listener.onDoubleTap();
        return true;
    }
}
