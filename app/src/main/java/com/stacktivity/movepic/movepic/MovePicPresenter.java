package com.stacktivity.movepic.movepic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stacktivity.movepic.Router;
import com.stacktivity.movepic.data.BindPaths;
import com.stacktivity.movepic.filemanager.FileManagerContract;
import com.stacktivity.movepic.movepic.binded_buttons.BindButtonsAdapter;
import com.stacktivity.movepic.utils.FileWorker;

import java.io.File;
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
        return imageAdapter.getFile(getCurrentImageNum());
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
    public int getCurrentImageNum() {
        return mView.getCurrentItemNum();
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
    public void deleteCurrentImageFromAdapter() {
        Log.d(tag, "deleteCurrentImageFromAdapter");
        int left = imageAdapter.deleteImage(getCurrentImageNum());
        if (left < 1) {
            mRouter.back();
        }
    }

    @Override
    public void deleteCurrentImageBuffered() {
        Log.d(tag, "deleteCurrentImageBuffered");
        int left = imageAdapter.deleteImageBuffered(getCurrentImageNum());
        if (left < 1) {
            mRouter.back();
        }
    }

    @Override
    public void onButtonRestoreImageClicked() {
        int res = imageAdapter.restoreLastDeletedImage();
        final Toast toast;
        switch (res) {
            case 0: toast = Toast.makeText(mView.getViewContext(),
                    "Picture successfully restored",
                    Toast.LENGTH_SHORT);
                    break;
            case 1: toast = Toast.makeText(mView.getViewContext(),
                    "Recovery error",
                    Toast.LENGTH_SHORT);
                    break;
            case 2: toast = Toast.makeText(mView.getViewContext(),
                    "Buffer is empty",
                    Toast.LENGTH_SHORT);
                    break;
            default: toast = Toast.makeText(mView.getViewContext(),
                    "Unknown error",
                    Toast.LENGTH_SHORT);
                    break;
        }
        toast.show();
    }

    @Override
    public void onBindButtonClick(int pos) {
        Log.d(tag, "onBindButtonClick: " + pos);
        File sourceFile = getCurrentImageFile();
        FileWorker fileWorker = new FileWorker();
        int moveErr = fileWorker.moveFile(sourceFile, bindButtonsAdapter.getPath(pos) + '/');
        if (moveErr != 0) {
            Toast.makeText(mView.getViewContext(), moveErr, Toast.LENGTH_SHORT).show();
        } else {
            deleteCurrentImageFromAdapter();
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
}
