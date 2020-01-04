package com.stacktivity.movepic.movepic;

import android.annotation.SuppressLint;
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


class ImagePagerAdapter extends PagerAdapter {
    final private String tag = ImagePagerAdapter.class.getName();

    final private MovePicContract.Presenter mPresenter;

    private int[] imageContainerSize;


    ImagePagerAdapter(MovePicContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private Bitmap getBitmap(int pos) {
        return BitmapFactory.decodeFile(mPresenter.getImagePath(pos));
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

    @Override
    public int getCount() {
        return mPresenter.getCountImages();
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
        final ImageView imageView = new ImageView(container.getContext());
        int padding = 4;

        imageView.setOnTouchListener(new OnDoubleTouchListener() {
            @Override
            public void onClick(View v, MotionEvent event) {
                mPresenter.onImageClick(imageView, getBitmap(position));
            }

            @Override
            public void onDoubleClick(View v, MotionEvent event) {
                mPresenter.onImageDoubleClick(imageView, getBitmap(position), event.getX(), event.getY());
            }
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
                    imageView.setImageBitmap(getSimplifiedBitmap(mPresenter.getImagePath(position),
                            imageContainerSize[0] / 2, imageContainerSize[1] / 2));
                }
            }.start();
        } else {
            imageView.setImageBitmap(getSimplifiedBitmap(mPresenter.getImagePath(position),
                    imageContainerSize[0] / 2, imageContainerSize[1] / 2));
        }
        container.addView(imageView, 0);

        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        (container).removeView((ImageView) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

}
