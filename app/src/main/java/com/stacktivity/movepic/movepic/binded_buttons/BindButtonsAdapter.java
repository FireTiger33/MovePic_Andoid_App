package com.stacktivity.movepic.movepic.binded_buttons;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.movepic.MovePicContract;

import java.util.ArrayList;
import java.util.List;


public class BindButtonsAdapter extends RecyclerView.Adapter<BaseButtonViewHolder> {
    final private String tag = BindButtonsAdapter.class.getName();

    final private MovePicContract.Presenter mPresenter;

    private ArrayList<String> paths;


    public BindButtonsAdapter(MovePicContract.Presenter presenter) {
        Log.d(tag, "createBindButtonsAdapter");
        mPresenter = presenter;
        paths = new ArrayList<>();
    }

    public void restorePaths(List<String> paths) {
        this.paths.addAll(paths);
    }

    @NonNull
    @Override
    public BaseButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindButtonViewHolder(
                LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bind_element, parent, false),
                mPresenter);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseButtonViewHolder baseButtonViewHolder, int pos) {
        Log.d(tag, "onBindViewHolder: pos = " + pos + "itemCount = " + getItemCount());
       /* if (pos < getItemCount()) {
            Log.d(tag, "bind");*/
            baseButtonViewHolder.bind(paths.get(pos), pos);
//        }
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    public void addElement(String path) {
        Log.d(tag, "addElement: " + path);
        paths.add(path);
        this.notifyItemInserted(getItemCount()-1);
    }

    public ArrayList<String> getPaths() {
        return paths;
    }

    public String getPath(int pos) {
        return paths.get(pos);
    }
}
