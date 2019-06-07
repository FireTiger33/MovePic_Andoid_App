package com.stacktivity.movepic.movepic.binded_buttons;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.Router;
import com.stacktivity.movepic.movepic.MovePicContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BindButtonsAdapter extends RecyclerView.Adapter<BaseButtonViewHolder> {
    final private String tag = BindButtonsAdapter.class.getName();

    /*final private short TYPE_BIND_BUTTON = 0;
    final private short TYPE_ADD_BUTTON = 1;*/

    final private Router mRouter;
    final private MovePicContract.Presenter mPresenter;
    final private LayoutInflater inflater;

    private ArrayList<String> paths;


    public BindButtonsAdapter(MovePicContract.Presenter presenter, Context context, Router router) {
        Log.d(tag, "createBindButtonsAdapter");
        inflater = LayoutInflater.from(context);
        mRouter = router;
        mPresenter = presenter;
        paths = new ArrayList<>();
    }

    public void restorePaths(List<String> paths) {
        this.paths.addAll(paths);
    }

    @NonNull
    @Override
    public BaseButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
//        if (viewType == TYPE_BIND_BUTTON) {
            view = inflater.inflate(R.layout.bind_element, parent, false);
            return new BindButtonViewHolder(view, mPresenter);
        /*} else if (viewType == TYPE_ADD_BUTTON){
            view = inflater.inflate(R.layout.btn_add, parent, false);
            return new ButtonAddViewHolder(view, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRouter.showFileManagerDialog(new FileManagerContract.Callback() {
                        @Override
                        public void onSuccess(String folderPath) {
                            addElement(folderPath);
                        }

                        @Override
                        public void onError() {

                        }
                    });
                }
            });
        } else {
            view = inflater.inflate(R.layout.bind_element, parent, false);
            return new BaseButtonViewHolder(view);
        }*/
    }

    @Override
    public void onBindViewHolder(@NonNull BaseButtonViewHolder baseButtonViewHolder, int pos) {
        Log.d(tag, "onBindViewHolder: pos = " + pos + "itemCount = " + getItemCount());
       /* if (pos < getItemCount()) {
            Log.d(tag, "bind");*/
            baseButtonViewHolder.bind(paths.get(pos), pos);
//        }
    }

    /*@Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) return TYPE_ADD_BUTTON;
        return TYPE_BIND_BUTTON;
    }
*/
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
