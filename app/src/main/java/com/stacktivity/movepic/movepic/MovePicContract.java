package com.stacktivity.movepic.movepic;

import android.graphics.Bitmap;


public interface MovePicContract {
    String KEY_PATH_IMAGE = "imagePath";

    interface View {
        void setPresenter(MovePicContract.Presenter presenter);
        void showToast(int resId);
        void showToast(String msg);
        void showImage(int numImage);
        int[] getSizeImageContainer();
        void zoomImageFromThumb(android.view.View imageView, Bitmap fullImage,
                                float centerX, float centerY);
//        void zoomImageFromThumbPath(String imagePath, float centerX, float centerY);
        void showFileManagerDialog();
        void close();
    }

    interface Presenter {
        String getImagePath(int num);
        int getCurrentImageNum();  // TODO private
        int getCountImages();

        // Used by View
        BindButtonsAdapter getBindButtonsAdapter();
        ImagePagerAdapter getImageAdapter();

        void onImagePageHasChange(int pos);

        /**
         * Removes the current image from the memory and the adapter itself.
         * Saves Bitmap and path to buffer[2] for possible recovery.
         */
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
        void changeBindButtonMode();

        // Used by BindButtonsViewHolder
        void onBindButtonClick(int pos);

        void onImageDoubleClick(android.view.View imageView, Bitmap fullImage, float x, float y);
        int[] getSizeImageContainer();

        void addBindButton(String directoryPath);
        void deleteBindButton(int pos);
        void moveBindButton(int fromPos, int toPos);
        boolean removeBindButtonsMode();

        /**
         * Retrieve saved path from repository and passes to ViewHolder
         * @param pos button position
         * @param viewHolder button viewHolder}
         */
        void onBindRepositoryPathAtBindButton(int pos, BindButtonViewHolder viewHolder);

        int getBindButtonsCount();

        void onDestroy();
    }

    interface Repository {

        /**
         * @param pos num in list
         * @return path for BindButton
         */
        String getBindPath(int pos);

        int getBindButtonsCount();

        /**
         * Add path to end of paths bonded buttons list
         */
        void addNewBindPath(String path);
        void moveBindPath(int fromPos, int toPos, boolean saveChanged);
        void deleteBindPath(int pos);

        int getCurrentImageNum();
        void setCurrentImageNum(int num);

        /**
         * @param pos num image
         * @return required image path
         */
        String getPathImage(int pos);

        int getCountImage();

        /**
         * Delete image from memory and list.
         * @param pos image num
         * @return remaining count images
         */
        int deleteImage(int pos);

        /**
         * Remove image from memory and list.
         * Save {@link Bitmap} and {@link String} path to buffer[2] for possible recovery.
         *
         * @return number remaining images.
         */
        int deleteImageBuffered(int pos);

        /**
         * Experimental function to restore the last deleted image from {@link Bitmap}.
         * File recovery is performed in one of two formats: JPEG or PNG.
         *
         * @return 0 if success; 1 if error; 2 if buffer is empty
         */
        int restoreLastDeletedImage(int currentImageNum);

        void onDestroy();
    }
}
