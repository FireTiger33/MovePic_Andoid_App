package com.stacktivity.movepic.movepic;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.stacktivity.movepic.Router;
import com.stacktivity.movepic.data.MovePicRepository;
import com.stacktivity.movepic.filemanager.FileManagerContract;
import com.stacktivity.movepic.utils.FileWorker;

import java.io.File;


public class MovePicPresenter implements MovePicContract.Presenter {
    final private String tag = MovePicPresenter.class.getName();

    final private MovePicContract.View mView;
    private MovePicContract.Repository repository;
    final private Router mRouter;
    private ImagePagerAdapter imageAdapter;
    final private BindButtonsAdapter bindButtonsAdapter;

    public MovePicPresenter(MovePicContract.View view, MovePicRepository repository, Router router) {
        Log.d(tag, "constructor");
        mView = view;
        mView.setPresenter(this);
        this.repository = repository;
        mRouter = router;
        imageAdapter = new ImagePagerAdapter(this);
        bindButtonsAdapter = new BindButtonsAdapter(this);
    }

    @Override
    public String getImagePath(int num) {
        return repository.getPathImage(num);
    }

    @Override
    public String getCurrentImageName() {
        String[] arr = getImagePath(mView.getCurrentItemNum()).split("[A-Za-z0-9.]*/");
        return arr[arr.length - 1];
    }

    @Override
    public int getCurrentImageNum() {
        return mView.getCurrentItemNum();
    }

    @Override
    public int getCountImages() {
        return repository.getCountImage();
    }

    @Override
    public BindButtonsAdapter getBindButtonsAdapter() {
        return bindButtonsAdapter;
    }

    @Override
    public ImagePagerAdapter getImageAdapter() {
        return imageAdapter;
    }

    private void deleteCurrentImage() {
        Log.d(tag, "deleteCurrentImage");
        int left = repository.deleteImage(getCurrentImageNum());
        if (left < 1) {
            mRouter.back();
        } else {
            imageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void deleteCurrentImageBuffered() {
        Log.d(tag, "deleteCurrentImageBuffered");
        int left = repository.deleteImageBuffered(getCurrentImageNum());
        if (left < 1) {
            mRouter.back();
        } else {
            imageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onButtonRestoreImageClicked() {
        int res = repository.restoreLastDeletedImage(getCurrentImageNum());
        switch (res) {
            case 0: mView.showToast("Picture successfully restored");
                    imageAdapter.notifyDataSetChanged();
                    break;
            case 1: mView.showToast("Recovery error");
                    break;
            case 2: mView.showToast("Buffer is empty");
                    break;
            default: mView.showToast("Unknown error");
                    break;
        }
    }

    @Override
    public void onBindButtonClick(int pos) {
        Log.d(tag, "onBindButtonClick: " + pos);
        File sourceFile = new File(getImagePath(getCurrentImageNum()));
        FileWorker fileWorker = new FileWorker();
        int moveErr = fileWorker.copyFile(sourceFile, repository.getBindPath(pos) + '/');
        if (moveErr != 0) {
            mView.showToast(moveErr);
        } else {
            deleteCurrentImage();
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
                repository.addNewBindPath(folderPath);
                bindButtonsAdapter.notifyItemInserted(repository.getBindButtonsCount() - 1);
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void onBindRepositoryPathAtBindButton(int pos, BindButtonViewHolder viewHolder) {
        viewHolder.bind(repository.getBindPath(pos), pos);
    }

    @Override
    public int getBindButtonsCount() {
        return repository.getBindButtonsCount();
    }
}
