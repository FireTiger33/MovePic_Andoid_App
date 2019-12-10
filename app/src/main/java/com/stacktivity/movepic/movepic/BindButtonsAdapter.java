package com.stacktivity.movepic.movepic;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stacktivity.movepic.R;


public class BindButtonsAdapter extends RecyclerView.Adapter<BindButtonViewHolder> {
    final private String tag = BindButtonsAdapter.class.getName();

    final private MovePicContract.Presenter mPresenter;


    BindButtonsAdapter(MovePicContract.Presenter presenter) {
        Log.d(tag, "createBindButtonsAdapter");
        mPresenter = presenter;
    }


    @NonNull
    @Override
    public BindButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindButtonViewHolder(
                LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bind_element, parent, false),
                mPresenter);
    }

    @Override
    public void onBindViewHolder(@NonNull BindButtonViewHolder buttonViewHolder, int pos) {
        Log.d(tag, "onBindViewHolder: pos = " + pos + "itemCount = " + getItemCount());

        mPresenter.onBindRepositoryPathAtBindButton(pos, buttonViewHolder);
    }

    @Override
    public int getItemCount() {
        return mPresenter.getBindButtonsCount();
    }

}
