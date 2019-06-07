package com.stacktivity.movepic.controllers;

import android.view.View;

public abstract class OnDoubleClickListener implements View.OnClickListener {
    private static final long DOUBLE_CLICK_DELTA_TIME = 300;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_DELTA_TIME) {
            onDoubleClick(v);
        } /*else {
            onSingleClick(v);
        }*/
        lastClickTime = clickTime;
    }

//    public abstract void onSingleClick(View v);

    public abstract void onDoubleClick(View v);
}
