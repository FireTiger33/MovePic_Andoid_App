package com.stacktivity.movepic.filemanager;

import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.stacktivity.movepic.Router;

import java.io.File;
import java.io.IOException;

public class FileManagerPresenter implements FileManagerContract.Presenter {
    final static private String tag = FileManagerPresenter.class.getName();

    private final FileManagerContract.View mView;
    private FilesAdapter adapter;

    FileManagerPresenter(FileManagerContract.View view, Router router) {
        mView = view;
        adapter = new FilesAdapter(mView.getViewContext(), router);
    }

    static public boolean isImage(String filePath) {
        final String extension = MimeTypeMap
                .getFileExtensionFromUrl(Uri.parse(filePath).toString());
        final String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension);

        Log.d(tag, "extension: " + extension);
        Log.d(tag, "mimeType: " + mimeType);

        return mimeType != null && mimeType.contains("image");
    }


    @Override
    public FilesAdapter getFilesAdapter() {
        return adapter;
    }

    @Override
    public void restorePath(String restoredPath) {
        adapter.restoreDirectory(restoredPath);
    }

    @Override
    public void onAddFolderButtonClick() {
        Log.d(tag, "onAddFolderButtonClick");
        mView.showCreateFolderDialog();
    }

    @Override
    public void createFolder(String name) {
        File file = new File(adapter.getCurrentDirectory() + '/' + name);
        if (file.mkdir()) {
            Log.d(tag, "createFolder: " + file.getPath());
            final Toast successToast = Toast.makeText(mView.getViewContext(),
                    "Успех",
                    Toast.LENGTH_SHORT);
            successToast.show();
            adapter.refresh();
        }
    }

    @Override
    public String getCurrentDirectory() {
        return adapter.getCurrentDirectory();
    }

    @Override
    public void createNomedia() {
        File file = new File(adapter.getCurrentDirectory() + "/.nomedia");
        try {
            if (file.createNewFile()) {
                Log.d(tag, "createFolder: " + file.getPath());
                final Toast successToast = Toast.makeText(mView.getViewContext(),
                        "Успех",
                        Toast.LENGTH_SHORT);
                successToast.show();
                adapter.refresh();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
