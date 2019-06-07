package com.stacktivity.movepic;

import com.stacktivity.movepic.filemanager.FileManagerContract;

public interface Router {
    void showMovePicScreen(final String pathPic, final int itemNum);
    void showFileManagerDialog(FileManagerContract.Callback callback);
    void back();
    void folderSelected(String folderPath);
}
