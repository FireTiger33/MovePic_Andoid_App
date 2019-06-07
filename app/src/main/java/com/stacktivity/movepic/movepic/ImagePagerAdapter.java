package com.stacktivity.movepic.movepic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stacktivity.movepic.controllers.OnDoubleTouchListener;
import com.stacktivity.movepic.filemanager.FileManagerPresenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


class ImagePagerAdapter extends PagerAdapter {
    final private String tag = ImagePagerAdapter.class.getName();

    final private MovePicContract.Presenter mPresenter;
    final private Context mContext;

    private int[] imageContainerSize;

    private List<Bitmap> imagesBitmapBuffer = new ArrayList<>();
    private List<String> imagesPaths = new ArrayList<>();


    ImagePagerAdapter(Context context, String imagePath, MovePicContract.Presenter presenter) {
        mPresenter = presenter;
        mContext = context;

        File file = new File(imagePath);
        file = file.getParentFile();
        for (File currentFile: file.listFiles()) {
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

    int deletedImage(int pos) {
        Log.d(tag, "deleteImage: " + pos);
        imagesPaths.remove(pos);
        notifyDataSetChanged();

        return imagesPaths.size();
    }
}
