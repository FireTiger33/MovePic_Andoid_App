package com.stacktivity.movepic.data;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.stacktivity.movepic.movepic.MovePicContract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.stacktivity.movepic.utils.FileWorker.deleteFile;
import static com.stacktivity.movepic.utils.FileWorker.isImage;
import static com.stacktivity.movepic.utils.FileWorker.sortFiles;

public class MovePicRepository implements MovePicContract.Repository {
    private final String tag = MovePicRepository.class.getSimpleName();

    private SharedPreferences mPreferences;
    private static final String MOVEPICVIEW_PREFERENCES_BINDED_PATHS = "myBondedPaths";


    private ArrayList<String> pathsBondedButtons;

    private Bitmap[] imagesBitmapBuffer = new Bitmap[2];
    private String[] imagesPathBuffer = new String[2];

    private ArrayList<String> imagesPaths = new ArrayList<>();
    private int currentImageNum = 0;


    public MovePicRepository(SharedPreferences preferences, String pathOpenedImage) {
        mPreferences = preferences;
        loadPathsBondedButtons();
        loadImagePathsInFolder(pathOpenedImage);
    }

    @Override
    public String getBindPath(int pos) {
        return pathsBondedButtons.get(pos);
    }

    @Override
    public int getBindButtonsCount() {
        return pathsBondedButtons.size();
    }

    @Override
    public void addNewBindPath(String path) {
        pathsBondedButtons.add(path);
        saveChangedData();

    }

    @Override
    public void moveBindPath(int fromPos, int toPos, boolean saveChanged) {
        String path = pathsBondedButtons.get(fromPos);
        pathsBondedButtons.remove(fromPos);
        if (toPos > fromPos) {
            toPos -= 1;
        }
        pathsBondedButtons.add(toPos, path);
        if (saveChanged) {
            saveChangedData();
        }
    }

    @Override
    public void deleteBindPath(int pos) {
        pathsBondedButtons.remove(pos);
        saveChangedData();
    }

    @Override
    public int getCurrentImageNum() {
        return currentImageNum;
    }

    @Override
    public void setCurrentImageNum(int num) {
        currentImageNum = num;
    }

    @Override
    public String getPathImage(int pos) {
        return imagesPaths.get(pos);
    }

    @Override
    public int getCountImage() {
        return imagesPaths.size();
    }

    @Override
    public int deleteImage(int pos) {
        Log.d(tag, "deleteImage: " + pos);
        if (deleteFile(imagesPaths.get(pos))) {
            imagesPaths.remove(pos);
        }

        return imagesPaths.size();
    }

    @Override
    public int deleteImageBuffered(int pos) {
        Log.d(tag, "deleteImageBuffered: " + pos);
        saveImageToBuffer(pos);

        return deleteImage(pos);
    }

    @Override
    public int restoreLastDeletedImage(int currentImageNum) {
        final String fName = "restoreLastDeletedImage: ";

        if (imagesPathBuffer[1] == null) {
            Log.d(tag, "Нечего восстанавливать");
            return 2;
        }
        Log.d(tag, fName + imagesPathBuffer[1]);
        File file = new File(imagesPathBuffer[1]);
        OutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            String fileFormat = imagesPathBuffer[1].substring(imagesPathBuffer[1].lastIndexOf(".") + 1);
            Log.d(tag, "image format is: " + fileFormat);
            Bitmap.CompressFormat compressFormat = fileFormat.equalsIgnoreCase("PNG") ?
                    Bitmap.CompressFormat.PNG :
                    Bitmap.CompressFormat.JPEG;
            imagesBitmapBuffer[1].compress(compressFormat, 100, fOut);
            fOut.flush();
            Log.d(tag, fName + "complete");
            fOut.close();

            // Add image to adapter
            imagesPaths.add(currentImageNum, imagesPathBuffer[1]);

            deleteLastImageFromBuffer();

            return 0;
//            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(),  file.getName()); // регистрация в фотоальбоме
        } catch (FileNotFoundException e) {
            Log.d(tag, fName + "File could not be created.");
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
    }

    private void loadPathsBondedButtons() {
        if (mPreferences.contains(MOVEPICVIEW_PREFERENCES_BINDED_PATHS)) {
            String bindPathsJSON = mPreferences.getString(MOVEPICVIEW_PREFERENCES_BINDED_PATHS, null);
            BindPaths bindPaths = new Gson().fromJson(bindPathsJSON, BindPaths.class);
            pathsBondedButtons = new ArrayList<>(bindPaths.getPaths());
        } else {
            pathsBondedButtons = new ArrayList<>();
        }
    }

    private void loadImagePathsInFolder(String pathOpenedImage) {
        File imageFile = new File(pathOpenedImage);
        File imageDirectory = imageFile.getParentFile();

        for (File currentFile : sortFiles(imageDirectory.listFiles())) {
            String currentFilePath = currentFile.getPath();
            if (isImage(currentFilePath)) {
                if (currentFile.getName().equals(imageFile.getName())) {
                    currentImageNum = imagesPaths.size();
                }
                imagesPaths.add(currentFilePath);
            }
        }
    }

    private void saveChangedData() {
        BindPaths bindPaths = new BindPaths(MOVEPICVIEW_PREFERENCES_BINDED_PATHS, pathsBondedButtons);
        String bindPathsJSON = new Gson().toJson(bindPaths);
        boolean success = false;
        while (!success) {
            success = mPreferences.edit()
                    .putString(MOVEPICVIEW_PREFERENCES_BINDED_PATHS, bindPathsJSON)
                    .commit();
        }
    }

    private void saveImageToBuffer(int pos) {
        imagesBitmapBuffer[0] = imagesBitmapBuffer[1];
        imagesBitmapBuffer[1] = BitmapFactory.decodeFile(imagesPaths.get(pos));
        imagesPathBuffer[0] = imagesPathBuffer[1];
        imagesPathBuffer[1] = imagesPaths.get(pos);
    }

    private void deleteLastImageFromBuffer() {
        imagesBitmapBuffer[1] = imagesBitmapBuffer[0];
        imagesBitmapBuffer[0] = null;
        imagesPathBuffer[1] = imagesPathBuffer[0];
        imagesPathBuffer[0] = null;
    }

    @Override
    public void onDestroy() {
        pathsBondedButtons.clear();
        imagesBitmapBuffer = null;
        imagesPathBuffer = null;
        imagesPaths.clear();
    }
}
