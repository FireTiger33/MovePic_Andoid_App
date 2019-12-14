package com.stacktivity.movepic.data;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.stacktivity.movepic.filemanager.FileManagerContract;

import java.io.File;

import static com.stacktivity.movepic.utils.FileWorker.sortFiles;

public class FileManagerRepository implements FileManagerContract.Repository {
    private final String tag = FileManagerRepository.class.getSimpleName();

    private final SharedPreferences mPreferences;
    private final String KEY_PATH = "directory";

    private String savedDirectoryPath;

    private File[] files;
    private File currentDirectory;

    public FileManagerRepository(SharedPreferences preferences) {
        mPreferences = preferences;
        loadLastOpenedPath();
        loadFiles();
    }

    public FileManagerRepository(String lastOpenedPath) {
        mPreferences = null;
        savedDirectoryPath = lastOpenedPath;
        loadFiles();
    }

    public File getDefaultDirectory() {  // TODO
        return Environment.getExternalStorageDirectory();
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    @Override
    public File getFile(int pos) {
        return files[pos];
    }

    @Override
    public int getFilesCount() {
        return files.length;
    }

    public String getSavedDirectoryPath() {
        return savedDirectoryPath;
    }

    @Override
    public void dataHasBeenChanged() {
        loadFiles();
    }

    public void setNewDirectory(File directory, boolean save) {
        Log.d(tag, "setNewDirectory: " + directory.getPath());
        savedDirectoryPath = directory.getPath();
        loadFiles();
        if (save && mPreferences != null) {
            boolean success = false;
            while (!success) {
                success = mPreferences.edit()
                        .putString(KEY_PATH, savedDirectoryPath)
                        .commit();
            }
        }
    }

    private void loadLastOpenedPath() {
        savedDirectoryPath = mPreferences.getString(KEY_PATH, getDefaultDirectory().getPath());
    }

    private void loadFiles() {
        currentDirectory = new File(savedDirectoryPath);
        files = sortFiles(currentDirectory.listFiles());
    }
}
