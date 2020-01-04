package com.stacktivity.movepic.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

public class ToolbarDemonstrator {

    private Toolbar mToolbar;
    private int mToolbarHeight;
    private ValueAnimator mVaActionBar;
    private ActionBar mActionBar;
    private int animateDuration;

    /**
     * @param toolbar {@link Toolbar} that was passed to the {@link androidx.appcompat.app.AppCompatActivity}.setSupportActionBar()
     * @param actionBar {@link ActionBar} obtained from method {@link androidx.appcompat.app.AppCompatActivity}.getSupportActionBar()
     */
    public ToolbarDemonstrator(Toolbar toolbar, ActionBar actionBar, int animateDurationMs) {
        mToolbar = toolbar;
        mActionBar = actionBar;
        animateDuration = animateDurationMs;
    }

    public void hideActionBar() {
        // initialize `mToolbarHeight`
        if (mToolbarHeight == 0) {
            mToolbarHeight = mToolbar.getHeight();
        }

        if (mVaActionBar != null && mVaActionBar.isRunning()) {
            // we are already animating a transition - block here
            return;
        }

        // animate `Toolbar's` height to zero.
        mVaActionBar = ValueAnimator.ofInt(mToolbarHeight , 0);
        mVaActionBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // update LayoutParams
                mToolbar.getLayoutParams().height
                        = (Integer)animation.getAnimatedValue();
                mToolbar.requestLayout();
            }
        });

        mVaActionBar.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (mActionBar != null) { // sanity check
                    mActionBar.hide();
                }
            }
        });

        mVaActionBar.setDuration(animateDuration);
        mVaActionBar.start();
    }

    public void showActionBar() {
        if (mVaActionBar != null && mVaActionBar.isRunning()) {
            // we are already animating a transition - block here
            return;
        }

        // restore `Toolbar's` height
        mVaActionBar = ValueAnimator.ofInt(0 , mToolbarHeight);
        mVaActionBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // update LayoutParams
                mToolbar.getLayoutParams().height
                        = (Integer)animation.getAnimatedValue();
                mToolbar.requestLayout();
            }
        });

        mVaActionBar.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                if (mActionBar != null) { // sanity check
                    mActionBar.show();
                }
            }
        });

        mVaActionBar.setDuration(animateDuration);
        mVaActionBar.start();
    }
}
