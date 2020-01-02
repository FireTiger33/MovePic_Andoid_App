package com.stacktivity.movepic.movepic;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class BindButtonViewHolder extends RecyclerView.ViewHolder {
    final private String tag = BindButtonViewHolder.class.getSimpleName();

    final private MovePicContract.Presenter mPresenter;
    final private Button mButton;
    private String mShortPath;
    private long startButtonPressTime;

    @SuppressLint("ClickableViewAccessibility")
    BindButtonViewHolder(@NonNull final View itemView, final MovePicContract.Presenter presenter) {
        super(itemView);
        mPresenter = presenter;
        mButton = (Button) itemView;
        mButton.setTextColor(Color.BLUE);

        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startButtonPressTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (presenter.removeBindButtonsMode()) {
                            presenter.deleteBindButton(getAdapterPosition());
                        } else {
                            if (System.currentTimeMillis() - startButtonPressTime > 300) {
                                if (mButton.getText().equals(getStringPos())) {
                                    mButton.setText(getShortPath());
                                } else {
                                    mButton.setText(getStringPos());
                                }
                            } else {
                                mPresenter.onBindButtonClick(getAdapterPosition());
                            }
                        }
                        break;
                }

                return true;
            }
        });
    }

    void bind(String path) {
        mShortPath = ".." + path.split("/[A-Za-z0-9]+", 4)[3];  // TODO dynamic limit
        Log.d(tag, "bind: shortPath = " + mShortPath);
        mButton.setText(mShortPath);
    }

    private String getShortPath() {
        return mShortPath;
    }

    private String getStringPos() {
        return String.valueOf(getAdapterPosition());
    }
}
