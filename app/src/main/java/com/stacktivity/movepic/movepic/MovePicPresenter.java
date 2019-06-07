package com.stacktivity.movepic.movepic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stacktivity.movepic.Router;
import com.stacktivity.movepic.data.BindPaths;
import com.stacktivity.movepic.filemanager.FileManagerContract;
import com.stacktivity.movepic.movepic.binded_buttons.BindButtonsAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class MovePicPresenter implements MovePicContract.Presenter {
    final private String tag = MovePicPresenter.class.getName();

    final private MovePicContract.View mView;
    final private Router mRouter;
    private ImagePagerAdapter imageAdapter;
    final private BindButtonsAdapter bindButtonsAdapter;
    private ArrayList<String> bindPathsList;

    private static final String MOVEPICVIEW_PREFERENCES = "MovePicViewPreferences";
    private static final String MOVEPICVIEW_PREFERENCES_BINDED_PATHS = "myBindedPaths";
    private SharedPreferences mPreferences;

    MovePicPresenter(MovePicContract.View view, Context context, Router router, String pathFirstIMG) {
        Log.d(tag, "constructor");
        mView = view;
        mRouter = router;
        imageAdapter = new ImagePagerAdapter(context, pathFirstIMG, this);
        bindPathsList = new ArrayList<>();
        bindButtonsAdapter = new BindButtonsAdapter(this, context, router);
        mPreferences = mView.getViewContext().getSharedPreferences(MOVEPICVIEW_PREFERENCES, Context.MODE_PRIVATE);
        if (mPreferences.contains(MOVEPICVIEW_PREFERENCES_BINDED_PATHS)) {
            String bindPathsJSON = mPreferences.getString(MOVEPICVIEW_PREFERENCES_BINDED_PATHS, null);
            BindPaths bindPaths = new Gson().fromJson(bindPathsJSON, BindPaths.class);
            bindButtonsAdapter.restorePaths(bindPaths.getPaths());
        }
    }

    @Override
    public File getCurrentImageFile() {
        return imageAdapter.getFile(mView.getCurrentItemNum());
    }

    @Override
    public Bitmap getCurrentImageBitmap() {
        return imageAdapter.getBitmap(mView.getCurrentItemNum());
    }

    @Override
    public String getCurrentImageName() {
        return imageAdapter.getName(mView.getCurrentItemNum());
    }

    @Override
    public BindButtonsAdapter getBindButtonsAdapter() {
        return bindButtonsAdapter;
    }

    @Override
    public ImagePagerAdapter getImageAdapter() {
        return imageAdapter;
    }

    @Override
    public void deleteCurrentImage() {
        deleteImage(getCurrentImageFile());
    }

    @Override
    public void onBindButtonClick(int pos) {
        Log.d(tag, "onBindButtonClick: " + pos);
        File sourceFile = getCurrentImageFile();
        boolean copyComplete = writeFileTo(sourceFile, bindButtonsAdapter.getPath(pos) + '/');
        if (copyComplete) {
            deleteImage(sourceFile);
        }
    }

    @Override
    public void onImageDoubleClick(View imageView, Bitmap fullImage, float x, float y) {
        mView.zoomImageFromThumb(imageView, fullImage, x, y);
    }

    @Override
    public int[] getSizeImageContainer() {
        return mView.getSizeImageContainer();
    }

    @Override
    public void addBindButton() {
        mRouter.showFileManagerDialog(new FileManagerContract.Callback() {
            @Override
            public void onSuccess(String folderPath) {
                bindButtonsAdapter.addElement(folderPath);
                bindPathsList.add(folderPath);
                BindPaths bindPaths = new BindPaths(MOVEPICVIEW_PREFERENCES_BINDED_PATHS, bindPathsList);
                String bindPathsJSON = new Gson().toJson(bindPaths);
                boolean success = false;
                while (!success) {
                    success = mPreferences.edit()
                            .putString(MOVEPICVIEW_PREFERENCES_BINDED_PATHS, bindPathsJSON)
                            .commit();
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    private boolean writeFileTo(File sourceFile, String folderToSave) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(tag, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return false;
        }

        /*// test correctly name
        String justName = sourceFile.getName();
        String name = getCurrentImageName();
        Log.d(tag, "just name = " + justName);
        Log.d(tag, "name = " + name);
        if (!name.equals(justName)) {
            Log.e(tag, "writeFileTo: different names");
        }*/

        return copyFile(sourceFile, new File(folderToSave + sourceFile.getName()));
    }

    private boolean copyFile(File sourceFile, File destFile) {
        Log.d(tag, "copyFile");
        if (!destFile.exists()) {
            try {
                if (!destFile.createNewFile()) {
                    final Toast toast = Toast.makeText(mView.getViewContext(),
                            "Невозможно переместить.\nФайл с таким именем уже существует",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    return false;
                }
            } catch (IOException e) {
                Log.e(tag, "copyFile: Invalid destFile path");
            }
        }
        Log.d(tag, "copyFrom: " + sourceFile.getPath() + " to "+ destFile.getPath());

        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()
        ){
            destination.transferFrom(source, 0, source.size());
            Log.d(tag, "copyFile: finish");
            return true;
        } catch (FileNotFoundException e) {
            Log.e(tag, "copyFile: SourceFile not found");
            return false;
        } catch (IOException e) {
            Log.e(tag, "copyFile: sourceChannel was snap closed");
            e.printStackTrace();
            return false;
        }
    }

    private void deleteImage(File imageFile) {
        if (imageFile.delete()) {
            int left = imageAdapter.deletedImage(mView.getCurrentItemNum());
            if (left < 1) {
                mRouter.back();
            }
            Log.d(tag, "copyFile: sourceFile deleted");
        }
    }

    /*private String SavePicture(Bitmap bitmap, String fileToSave) {
        OutputStream fOut;
        try {
            File file = new File(fileToSave); // создать уникальное имя для файла основываясь на дате сохранения
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // сохранять картинку в jpeg-формате с 85% сжатия.
            fOut.flush();
            Log.d(tag, "чёта записали");
            fOut.close();
//            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(),  file.getName()); // регистрация в фотоальбоме
        }
        catch (Exception e) // здесь необходим блок отслеживания реальных ошибок и исключений, общий Exception приведен в качестве примера
        {
            return e.getMessage();
        }
        return "";
    }*/
}
