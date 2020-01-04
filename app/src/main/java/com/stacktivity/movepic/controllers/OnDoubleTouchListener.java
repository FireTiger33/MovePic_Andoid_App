package com.stacktivity.movepic.controllers;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class OnDoubleTouchListener implements View.OnTouchListener {
    private static final long DOUBLE_CLICK_DELTA_TIME = 220;
    private long lastClickTime = 0;
    private CountDownTimer timer = null;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            long clickTime = System.currentTimeMillis();
            if (timer != null) timer.cancel();
            long delta = clickTime - lastClickTime;
            if (delta < DOUBLE_CLICK_DELTA_TIME) {
                Log.d("DTS", "" + delta);
                onDoubleClick(v, event);
            } else {
                timer = new CountDownTimer(DOUBLE_CLICK_DELTA_TIME, 100) {
                    @Override
                    public void onTick(long l) {}
                    @Override
                    public void onFinish() {
                        onClick(v, event);
                    }
                }.start();
            }
            lastClickTime = clickTime;
        } else {
            onOtherEvent(v, event);
        }

        return true;
    }

    public void onClick(View v, MotionEvent event){}

    public void onDoubleClick(View v, MotionEvent event){}

    public void onOtherEvent(View v, MotionEvent event){}
}
