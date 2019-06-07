package com.stacktivity.movepic.filemanager;

import android.content.Context;
import android.support.v7.widget.Toolbar;

public interface FileManagerContract {
    interface Callback {
        void onSuccess(String folderPath);
        void onError();
    }

    interface View {
        Context getViewContext();
        void showCreateFolderDialog();
        void showFolderPath(String path);
        Toolbar getToolBar();
    }

    interface Presenter {
        FilesAdapter getFilesAdapter();
        void restorePath(String restoredPath);
        void onAddFolderButtonClick();
        void createFolder(String name);
        String getCurrentDirectory();
        void createNomedia();
        void onDirectoryChanged(String newPath);
    }
}
