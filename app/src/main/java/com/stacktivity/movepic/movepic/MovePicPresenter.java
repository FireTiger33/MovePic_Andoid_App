package com.stacktivity.movepic.movepic;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.stacktivity.movepic.data.MovePicRepository;
import com.stacktivity.movepic.utils.FileWorker;

import java.io.File;


public class MovePicPresenter implements MovePicContract.Presenter {
    final private String tag = MovePicPresenter.class.getName();

    private final MovePicContract.View mView;
    private final MovePicContract.Repository repository;
    private final ImagePagerAdapter imageAdapter;
    private final BindButtonsAdapter bindButtonsAdapter;

    private boolean isRemoveBindButtonsMode = false;

    MovePicPresenter(MovePicContract.View view, MovePicRepository repository) {
        Log.d(tag, "constructor");
        mView = view;
        this.repository = repository;
        imageAdapter = new ImagePagerAdapter(this);
        bindButtonsAdapter = new BindButtonsAdapter(this);
        mView.setPresenter(this);
    }

    @Override
    public String getImagePath(int num) {
        return repository.getPathImage(num);
    }

    @Override
    public int getCurrentImageNum() {
        return repository.getCurrentImageNum();
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

    @Override
    public void onImagePageHasChange(int pos) {
        repository.setCurrentImageNum(pos);
    }

    @Override
    public void setPercentToRemove(double val) {
        if (val > 1f) val = 1f;
        mView.setBasketAreaAlpha(val * 0.7);
    }

    private void deleteCurrentImage() {
        Log.d(tag, "deleteCurrentImage");
        int left = repository.deleteImage(getCurrentImageNum());
        if (left < 1) {
            close();
        } else {
            imageAdapter.notifyDataSetChanged();
            mView.updateCurrentImageNum();
        }
    }

    @Override
    public void deleteCurrentImageBuffered() {
        Log.d(tag, "deleteCurrentImageBuffered");
        int left = repository.deleteImageBuffered(getCurrentImageNum());
        if (left < 1) {
            close();
        } else {
            imageAdapter.notifyDataSetChanged();
            mView.updateCurrentImageNum();
        }
    }

    @Override
    public boolean restoreBufferedImage() {
        int res = repository.restoreLastDeletedImage(getCurrentImageNum());
        switch (res) {
            case 0: mView.showToast("Picture successfully restored");
                    imageAdapter.notifyDataSetChanged();
                    mView.updateCurrentImageNum();
                    break;
            case 1: mView.showToast("Recovery error");
                    break;
            case 2: mView.showToast("Buffer is empty");
                    break;
            default: mView.showToast("Unknown error");
                    break;
        }

        return res == 0;
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
    public void changeBindButtonMode() {
        isRemoveBindButtonsMode = !isRemoveBindButtonsMode;
    }

    @Override
    public void onImageClick() {
        mView.showFullscreenMode();
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
    public void addBindButton(String directoryPath) {
        repository.addNewBindPath(directoryPath);
        bindButtonsAdapter.notifyItemInserted(repository.getBindButtonsCount() - 1);
    }

    @Override
    public void deleteBindButton(int pos) {
        repository.deleteBindPath(pos);
        bindButtonsAdapter.notifyItemRemoved(pos);
    }

    @Override
    public void moveBindButton(int fromPos, int toPos) {
        repository.moveBindPath(fromPos, toPos, true);
        bindButtonsAdapter.notifyItemMoved(fromPos, toPos);
    }

    @Override
    public boolean removeBindButtonsMode() {
        return isRemoveBindButtonsMode;
    }

    @Override
    public void onBindRepositoryPathAtBindButton(int pos, BindButtonViewHolder viewHolder) {
        viewHolder.bind(repository.getBindPath(pos));
    }

    @Override
    public int getBindButtonsCount() {
        return repository.getBindButtonsCount();
    }

    private void close() {
        mView.close();
    }

    @Override
    public void onDestroy() {
        repository.onDestroy();
    }
}
