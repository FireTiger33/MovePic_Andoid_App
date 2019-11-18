package com.stacktivity.movepic.movepic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.stacktivity.movepic.controllers.OnDoubleTouchListener;
import com.stacktivity.movepic.filemanager.FileManagerPresenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.stacktivity.movepic.filemanager.FileManagerPresenter.sortFiles;


class ImagePagerAdapter extends PagerAdapter {
    final private String tag = ImagePagerAdapter.class.getName();

    final private MovePicContract.Presenter mPresenter;
    final private Context mContext;

    private int[] imageContainerSize;

    private Bitmap[] imagesBitmapBuffer = new Bitmap[2];
    private String[] imagesPathBuffer = new String[2];

    private List<String> imagesPaths = new ArrayList<>();


    ImagePagerAdapter(Context context, String imagePath, MovePicContract.Presenter presenter) {
        mPresenter = presenter;
        mContext = context;

        File file = new File(imagePath);
        file = file.getParentFile();

        for (File currentFile: sortFiles(file.listFiles())) {
            String currentFilePath = currentFile.getPath();
            if (FileManagerPresenter.isImage(currentFilePath)) {
                imagesPaths.add(currentFilePath);
            }
        }
    }

    File getFile(int pos) {
        return new File(imagesPaths.get(pos));
    }

    Bitmap getBitmap(int pos) {
        return BitmapFactory.decodeFile(imagesPaths.get(pos));
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Начальная высота и ширина изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Рассчитываем наибольшее значение inSampleSize, которое равно степени двойки
            // и сохраняем высоту и ширину, когда они больше необходимых
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap getSimplifiedBitmap(String imagePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        // Подсчитываем inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Теперь вызываем декодер с установленной опцией inSampleSize
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    String getName(int pos) {
        String[] arr = imagesPaths.get(pos).split("[A-Za-z0-9.]*/");
        return arr[arr.length-1];
    }

    @Override
    public int getCount() {
        return imagesPaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (object);
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        Log.d(tag, "instantiateItem");
        final ImageView imageView = new ImageView(mContext);
        int padding = 4;

        imageView.setOnTouchListener(new OnDoubleTouchListener() {
            @Override
            public void onDoubleClick(View v, MotionEvent event) {
                mPresenter.onImageDoubleClick(imageView, getBitmap(position), event.getX(), event.getY());
            }
            @Override
            public void onOtherEvent(View v, MotionEvent event) { }
        });

        imageView.setPadding(padding, 0, padding, 0);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (imageContainerSize == null) {
            new CountDownTimer(100, 100) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    imageContainerSize = mPresenter.getSizeImageContainer();
                    imageView.setImageBitmap(getSimplifiedBitmap(imagesPaths.get(position),
                            imageContainerSize[0]/2, imageContainerSize[1]/2));
                }
            }.start();
        } else {
            imageView.setImageBitmap(getSimplifiedBitmap(imagesPaths.get(position),
                    imageContainerSize[0]/2, imageContainerSize[1]/2));
        }
        container.addView(imageView, 0);

        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        (container).removeView((ImageView) object);
    }

    @Override public int getItemPosition(@NonNull Object object){
        return PagerAdapter.POSITION_NONE;
    }

    /**
     * Removes the current image from the memory and the adapter itself.
     * @return number of remaining images in the folder
     */
    int deleteImage(int pos) {
        Log.d(tag, "deleteImage: " + pos);
        String path = imagesPaths.get(pos);
        if (new File(path).delete()) {
            imagesPaths.remove(pos);
            Log.d(tag, "deleteImage: Image " + path + " deleted");
        }
        notifyDataSetChanged();

        return imagesPaths.size();
    }

    private void saveImageToBuffer(int pos) {
        imagesBitmapBuffer[0] = imagesBitmapBuffer[1];
        imagesBitmapBuffer[1] = this.getBitmap(pos);
        imagesPathBuffer[0] = imagesPathBuffer[1];
        imagesPathBuffer[1] = imagesPaths.get(pos);
    }

    /**
     * Removes the current image from the memory and the adapter itself.
     * Saves Bitmap and path to buffer[2] for possible recovery.
     * @return number of remaining images in the folder
     */
    int deleteImageBuffered(int pos) {
        Log.d(tag, "deleteImageBuffered: " + pos);
        saveImageToBuffer(pos);

        return deleteImage(pos);
    }

    /**
     * Experimental function to restore the last deleted image from RAM.
     * File recovery is performed in one of two formats: JPEG or PNG.
     * @return 0 if success; 1 if error; 2 if buffer is empty
     */
    int restoreLastDeletedImage() {
        final String fName = "restoreLastDeletedImage: ";

        if (imagesPathBuffer[1] == null) {
            Log.d(tag, "Нечего восстанавливать");
            return 2;
        }
        Log.d(tag, fName+ imagesPathBuffer[1]);
        File file = new File(imagesPathBuffer[1]);
        OutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            String fileFormat = imagesPathBuffer[1].substring(imagesPathBuffer[1].lastIndexOf(".")+1);
            Log.d(tag, "image format is: " + fileFormat);
            Bitmap.CompressFormat compressFormat = fileFormat.equalsIgnoreCase("PNG")?
                    Bitmap.CompressFormat.PNG:
                    Bitmap.CompressFormat.JPEG;
            imagesBitmapBuffer[1].compress(compressFormat, 100, fOut);
            fOut.flush();
            Log.d(tag, fName +"complete");
            fOut.close();

            // Add image to adapter
            imagesPaths.add(mPresenter.getCurrentImageNum(), imagesPathBuffer[1]);
            notifyDataSetChanged();

            deleteLastItemFromBuffer();

            return 0;
//            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(),  file.getName()); // регистрация в фотоальбоме
        } catch (FileNotFoundException e) {
            Log.d(tag, fName + "File could not be created.");
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            Log.d(tag, fName + "I/O error");
            e.printStackTrace();
            return 1;
        }
    }

    private void deleteLastItemFromBuffer() {
        imagesBitmapBuffer[1] = imagesBitmapBuffer[0];
        imagesBitmapBuffer[0] = null;
        imagesPathBuffer[1] = imagesPathBuffer[0];
        imagesPathBuffer[0] = null;
    }
}
