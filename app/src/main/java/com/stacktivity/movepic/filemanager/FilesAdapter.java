package com.stacktivity.movepic.filemanager;

import android.content.Context;
import android.os.Environment;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.Router;

import java.io.File;

import static com.stacktivity.movepic.filemanager.FileManagerPresenter.sortFiles;

/**
 * Адаптер для {@link RecyclerView}, отобрадающий список файлов в указанной директории.
 * Начинает обзор с корня внешнего хранилища. Поддерживает навигацию по директориям.
 * Для упрощения кода опущены проверки наличия (примонтированности) внешнего хранилища.
 */
class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> implements FileManagerView.OnClickFileManagerItem{
    final private String tag = FilesAdapter.class.getName();

    final private short TYPE_FILE = 0;
    final private short TYPE_FOLDER = 1;

    private final FileManagerContract.Presenter mPresenter;
    private final Router router;
    private final LayoutInflater layoutInflater;
    private File initialFile;

    private File[] files;
    private File currentFile;

    private FileManagerView.OnClickFileManagerItem onClickFileItem = this;

    FilesAdapter(final FileManagerContract.Presenter presenter, final Context context, Router router) {
        mPresenter = presenter;
        layoutInflater = LayoutInflater.from(context);
        this.router = router;
        init();
    }

    void init() {
        initialFile = Environment.getExternalStorageDirectory();
        setDirectory(initialFile);
    }

    void restoreDirectory(String path) {
        setDirectory(new File(path));
    }

    void refresh() {
        restoreDirectory(getCurrentDirectory());
    }

    private void setDirectory(final File file) {
        this.currentFile = file;
        this.files = file.listFiles();
        sortFiles(this.files);
        notifyDataSetChanged();
    }

    boolean goBack() {
        if (currentFile.equals(Environment.getExternalStorageDirectory())) {
            return false;
        }
        File parent = currentFile.getParentFile();
        if (parent != null) {
            mPresenter.onDirectoryChanged(parent.getPath());
            setDirectory(parent);
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        Log.d(tag, "onCreateViewHolder: viewType = " + viewType);
        View view;
        if (viewType == TYPE_FOLDER) {
            view = layoutInflater.inflate(R.layout.folder_item, parent, false);
        } else {
            view = layoutInflater.inflate(R.layout.file_item, parent, false);
        }
        return new FileViewHolder(view, onClickFileItem);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(files[position]);
    }

    @Override
    public int getItemCount() {
        return files != null ? files.length : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (files[position].isDirectory()) return TYPE_FOLDER;
        return TYPE_FILE;
    }

    @Override
    public void onClickDirectory(File file) {
        Log.d(tag, "onClickDirectory");
        setDirectory(file);
        mPresenter.onDirectoryChanged(file.getPath());
    }

    @Override
    public void onClickImage(File file, int position) {
        Log.d(tag, "onClickImage");
        int pos = 0;
        for (File currentFile: files) {
            if (FileManagerPresenter.isImage(currentFile.getPath())) {
                if (file == currentFile) {
                    break;
                } else {
                    ++pos;
                }
            }
        }
        router.showMovePicScreen(file.getPath(), pos);
    }

    String getCurrentDirectory() {
        return currentFile.getPath();
    }
}


