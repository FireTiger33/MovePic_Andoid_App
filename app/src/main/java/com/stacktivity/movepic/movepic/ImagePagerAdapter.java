package com.stacktivity.movepic.movepic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.PagerAdapter;


class ImagePagerAdapter extends PagerAdapter {
    final private String tag = ImagePagerAdapter.class.getSimpleName();

    final private MovePicContract.Presenter mPresenter;

    private int[] imageContainerSize;
    private boolean swipe = false;

    ImagePagerAdapter(MovePicContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private Bitmap getBitmapCurrentImage() {
        return BitmapFactory.decodeFile(mPresenter.getImagePath(mPresenter.getCurrentImageNum()), null);
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
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        Log.d(tag, "instantiateItem");
        final ImageView imageView = new ImageView(container.getContext());
        int padding = 4;

        imageView.setPadding(padding, 0, padding, 0);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        container.addView(imageView, 0);

        return defaultInstantiateItem(container, imageView, position);
    }

    @SuppressLint("ClickableViewAccessibility")
    private Object defaultInstantiateItem(
            @NonNull final ViewGroup container, final ImageView imageView, final int position) {
        final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(
                container.getContext(), new MyGestureListener(imageView)
        );

        if (imageContainerSize == null) {
            new CountDownTimer(100, 100) {
                @Override
                public void onTick(long millisUntilFinished) {}
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

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (swipe) {
                        swipe = false;
                    } else {
                        returnImageToDefaultPosition(imageView);
                    }
                }
                return true;
            }
        });

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


    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private double SWIPE_MIN_DISTANCE = mPresenter.getSizeImageContainer()[1] * 0.25;
        private ImageView imageView;

        MyGestureListener(ImageView view) {
            imageView = view;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mPresenter.onImageClick();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            mPresenter.onImageDoubleClick(imageView, getBitmapCurrentImage(), event.getX(), event.getY());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int PROCESS_MIN_DISTANCE = 50;
            float mDeltaY = e2.getY() - e1.getY();
            float currentY = imageView.getY() + mDeltaY;
            imageView.setY(currentY);
            if (currentY < -PROCESS_MIN_DISTANCE) {
                mPresenter.setPercentToRemove(-currentY / SWIPE_MIN_DISTANCE);
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean res = true;
            if (imageView.getY() > SWIPE_MIN_DISTANCE) {  // Swipe down
                swipe = true;
                restoreImageWithAnimation();
                if (!mPresenter.restoreBufferedImage()) {
                    returnImageToDefaultPosition(imageView);
                }
            } else if (imageView.getY() < -SWIPE_MIN_DISTANCE) {  // Swipe up
                swipe = true;
                deleteImageWithAnimation(imageView);
                mPresenter.setPercentToRemove(0f);
                imageView = null;
            } else {
                returnImageToDefaultPosition(imageView);
                res = false;
            }

            return res;
        }
    }

    private void restoreImageWithAnimation() {
        // TODO animate for fine-tuning to position
    }

    private void deleteImageWithAnimation(final ImageView imageView) {
        float currentPos = imageView.getY();
        ValueAnimator animator = ValueAnimator.ofFloat(currentPos, currentPos - imageView.getHeight());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                imageView.setY((Float) animation.getAnimatedValue());
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mPresenter.deleteCurrentImageBuffered();
            }
        });
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    private void returnImageToDefaultPosition(final ImageView imageView) {
        mPresenter.setPercentToRemove(0f);
        ValueAnimator animator = ValueAnimator.ofFloat(imageView.getY(), 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                imageView.setY((Float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(300);
        animator.start();
    }
}
