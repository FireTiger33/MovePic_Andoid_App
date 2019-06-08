package com.stacktivity.movepic.movepic;

import android.content.Context;
import android.graphics.Bitmap;

import com.stacktivity.movepic.movepic.binded_buttons.BindButtonsAdapter;

import java.io.File;

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


        void deleteCurrentImage();
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
    }
}
