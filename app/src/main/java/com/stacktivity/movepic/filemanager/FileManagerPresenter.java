package com.stacktivity.movepic.filemanager;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.data.FileManagerRepository;

import java.io.File;
import java.io.IOException;


public class FileManagerPresenter implements FileManagerContract.Presenter, FileManagerView.OnClickFileListener,
        LifecycleObserver {
    final static private String tag = FileManagerPresenter.class.getName();

    private final FileManagerContract.View mView;
    private final FileManagerRepository mRepository;
    private final FilesAdapter adapter;
    private final boolean isDialogSession;

    FileManagerPresenter(FileManagerContract.View view, Lifecycle viewLifecycle,
                         FileManagerRepository repository, boolean isDialogSession) {
        mView = view;
        mRepository = repository;
        adapter = new FilesAdapter(this, this);
        this.isDialogSession = isDialogSession;
        mView.setPresenter(this);
        viewLifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void refreshData() {
        Log.d(tag, "refreshData");
        mRepository.refreshFiles();
        adapter.notifyDataSetChanged();
    }

    @Override
    public FilesAdapter getFilesAdapter() {
        return adapter;
    }

    @Override
    public void onAddFolderButtonClick() {
        Log.d(tag, "onAddFolderButtonClick");
        mView.showCreateFolderDialog();
    }

    @Override
    public void createFolder(String name) {
        File file = new File(getCurrentDirectoryPath() + '/' + name);
        if (file.mkdir()) {
            Log.d(tag, "createFolder: " + file.getPath());
            mView.showMessage(R.string.success);
            refreshData();
        }
    }

    @Override
    public String getCurrentDirectoryPath() {
        return mRepository.getCurrentDirectory().getPath();
    }

    @Override
    public File getFileInCurrentDirectory(int pos) {
        return mRepository.getFile(pos);
    }

    @Override
    public int getFilesCount() {
        return mRepository.getFilesCount();
    }

    @Override
    public void createNomedia() {
        File file = new File(getCurrentDirectoryPath() + "/.nomedia");
        try {
            if (file.createNewFile()) {
                Log.d(tag, "createFolder: " + file.getPath());
                mView.showMessage(R.string.success);
                refreshData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean goBack() {
        File currentDirectory = mRepository.getCurrentDirectory();
        if (mRepository.getCurrentDirectory().equals(mRepository.getDefaultDirectory())) {
            return false;
        }
        File parent = currentDirectory.getParentFile();
        if (parent != null) {
            onClickDirectory(parent);
            return true;
        }
        return false;
    }

    @Override
    public void onClickDirectory(File file) {
        Log.d(tag, "onClickDirectory");
        mView.showFolderPath(file.getPath());
        if (isDialogSession) {
            mRepository.setNewDirectory(file, false);
        } else {
            mRepository.setNewDirectory(file, true);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClickImage(File file) {
        mView.showMovePicScreen(file.getPath());
    }
}
