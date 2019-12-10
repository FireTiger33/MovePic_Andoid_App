package com.stacktivity.movepic.movepic;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stacktivity.movepic.R;


class BindButtonViewHolder extends RecyclerView.ViewHolder{
    final private String tag = BindButtonViewHolder.class.getName();

    final private MovePicContract.Presenter mPresenter;
    final private Button mButton;
    private String mShortPath;
    private int mPos;
    private long startButtonPressTime;

    @SuppressLint("ClickableViewAccessibility")
    BindButtonViewHolder(@NonNull View itemView, MovePicContract.Presenter presenter) {
        super(itemView);
        mPresenter = presenter;
        mButton = itemView.findViewById(R.id.bind_button);
        mButton.setTextColor(Color.BLUE);
        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(tag, "MotionEvent: " + event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startButtonPressTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - startButtonPressTime > 300) {
                            if (mButton.getText().equals(getStringPos())) {
                                mButton.setText(getShortPath());
                            } else {
                                mButton.setText(getStringPos());
                            }
                        } else {
                            mPresenter.onBindButtonClick(mPos);
                        }
                        break;
                }

                return true;
            }
        });
    }

    void bind(String path, int pos) {
        Log.d(tag, "bind");
        mShortPath = ".." + path.split("/[A-Za-z0-9]*", 4)[3];  // TODO dynamic limit
        Log.d(tag, "bind: shortPath = " + mShortPath);
        mPos = pos;
        if (mButton.getText() == "") {
            mButton.setText(getStringPos());
        }
    }

    private String getShortPath() {
        return mShortPath;
    }

    private String getStringPos() {
        return String.valueOf(mPos);
    }
}