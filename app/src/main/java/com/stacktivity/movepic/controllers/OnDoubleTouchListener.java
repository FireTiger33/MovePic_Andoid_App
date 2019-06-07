package com.stacktivity.movepic.controllers;

import android.view.MotionEvent;
import android.view.View;

public abstract class OnDoubleTouchListener implements View.OnTouchListener {
    private static final long DOUBLE_CLICK_DELTA_TIME = 300;
    private long lastClickTime = 0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_DELTA_TIME) {
                onDoubleClick(v, event);
            } else {
                onOtherEvent(v, event);
            }
            lastClickTime = clickTime;
        } else {
            onOtherEvent(v, event);
        }

        return true;
    }

    public abstract void onDoubleClick(View v, MotionEvent event);

    public abstract void onOtherEvent(View v, MotionEvent event);
}
