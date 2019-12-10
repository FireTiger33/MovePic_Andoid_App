package com.stacktivity.movepic.movepic;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;

public interface MovePicContract {
    String TAG_PATHPIC = "PathPic";
    String TAG_ITEM_NUM = "ItemNum";

    interface View {
        Context getViewContext();
        int getCurrentItemNum();
        int[] getSizeImageContainer();
        void zoomImageFromThumb(android.view.View imageView, Bitmap fullImage,
                                float centerX, float centerY);
//        void zoomImageFromThumbPath(String imagePath, float centerX, float centerY);
    }

    interface Presenter {
        File getCurrentImageFile();
        Bitmap getCurrentImageBitmap();
        String getCurrentImageName();
        int getCurrentImageNum();
        BindButtonsAdapter getBindButtonsAdapter();
        ImagePagerAdapter getImageAdapter();


        void deleteCurrentImageFromAdapter();
        void deleteCurrentImageBuffered();

        /**
         * Attempts to restore the last deleted image.
         * For any result, one of the toasts is shown:
         * 1) Picture successfully restored
         * 2) Recovery error
         * 3) Buffer is empty
         * 4) Unknown error
         */
        void onButtonRestoreImageClicked();
        void onBindButtonClick(int pos);
        void onImageDoubleClick(android.view.View imageView, Bitmap fullImage, float x, float y);
        int[] getSizeImageContainer();

        void addBindButton();


        // Repositories methods

        /**
         * Retrieve saved path from repository and passes to ViewHolder
         * @param pos button position
         * @param viewHolder button viewHolder}
         */
        void onBindRepositoryPathAtBindButton(int pos, BindButtonViewHolder viewHolder);

        int getBindButtonsCount();
    }

    interface Repository {

        ArrayList<String> getAllPaths();
        /**
         * @param pos num in list
         * @return path for BindButton
         */
        String getBindPath(int pos);

        int getBindButtonsCount();

        /**
         * Add path to end of list
         */
        void addNewPath(String path);

    }
}
