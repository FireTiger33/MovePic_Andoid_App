package com.stacktivity.movepic.filemanager;


import java.io.File;

public interface FileManagerContract {
    String KEY_DIALOG_SESSION = "dialogSession";
    int RC_FILE_MANAGER_DIALOG = 456;

    interface View {
        void setPresenter(Presenter presenter);
        void showMessage(String msg);
        void showMessage(int resId);
        void showCreateFolderDialog();
        void showFolderPath(String path);
        void showMovePicScreen(String imagePath);
    }

    interface Presenter {
        FilesAdapter getFilesAdapter();
        void onAddFolderButtonClick();
        void createFolder(String name);
        String getCurrentDirectoryPath();
        File getFileInCurrentDirectory(int pos);
        int getFilesCount();
        void createNomedia();
        boolean goBack();
    }

    interface Repository {
        File getDefaultDirectory();
        File getCurrentDirectory();
        File getFile(int pos);
        int getFilesCount();
        String getSavedDirectoryPath();
        void setNewDirectory(File directory, boolean save);
        void refreshFiles();
    }
}
