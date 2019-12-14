package com.stacktivity.movepic.filemanager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stacktivity.movepic.R;


/**
 * Адаптер для {@link RecyclerView}, отображающий список файлов в указанной директории.
 * Начинает обзор с корня внешнего хранилища. Поддерживает навигацию по директориям.
 */
class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> {
    final private String tag = FilesAdapter.class.getName();

    final private short TYPE_FILE = 0;
    final private short TYPE_FOLDER = 1;

    private final FileManagerView.OnClickFileListener mClickFileListener;
    private final FileManagerContract.Presenter mPresenter;

    FilesAdapter(final FileManagerContract.Presenter presenter,
                 final FileManagerView.OnClickFileListener clickFileListener) {
        mPresenter = presenter;
        mClickFileListener = clickFileListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_FOLDER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.folder_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.file_item, parent, false);
        }
        return new FileViewHolder(view, mClickFileListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(mPresenter.getFileInCurrentDirectory(position));
    }

    @Override
    public int getItemCount() {
        Log.d(tag, "ItemsCount: " + mPresenter.getFilesCount());
        return mPresenter.getFilesCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (mPresenter.getFileInCurrentDirectory(position).isDirectory()) return TYPE_FOLDER;
        return TYPE_FILE;
    }

}


