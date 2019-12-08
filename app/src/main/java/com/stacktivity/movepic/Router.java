package com.stacktivity.movepic;


import com.stacktivity.movepic.filemanager.FileManagerContract;

public interface Router {
    /**
     * Opens a screen for working with images.
     * @param itemNum is the file number on the screen
     */
    void showMovePicScreen(final String pathPic, final int itemNum);
    /**
     * Opens the file manager window
     */
    void showFileManagerDialog(FileManagerContract.Callback callback);
    /**
     * Call method onSuccess for callback obj passed in method showFileManagerDialog
     * and return control to previous fragment
     * @param folderPath path selected folder
     */
    void folderSelected(String folderPath);
    /**
     * Called to return to the previous screen
     */
    void back();

    void showNavigationView();
}
