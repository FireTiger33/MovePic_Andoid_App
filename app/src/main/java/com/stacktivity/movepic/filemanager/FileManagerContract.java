package com.stacktivity.movepic.filemanager;

import android.content.Context;

public interface FileManagerContract {
    interface Callback {
        void onSuccess(String folderPath);
        void onError();
    }

    interface View {
        Context getViewContext();
        void showCreateFolderDialog();
    }

    interface Presenter {
        FilesAdapter getFilesAdapter();
        void restorePath(String restoredPath);
        void onAddFolderButtonClick();
        void createFolder(String name);
        String getCurrentDirectory();
        void createNomedia();
    }
}
