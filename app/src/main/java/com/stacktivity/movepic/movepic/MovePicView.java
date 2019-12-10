package com.stacktivity.movepic.movepic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.Router;
import com.stacktivity.movepic.controllers.OnDoubleTouchListener;

import java.util.Objects;

public class MovePicView extends Fragment implements MovePicContract.View {
    final private String tag = MovePicView.class.getName();

    private MovePicContract.Presenter mPresenter;

    private FrameLayout imageContainer;
    private ImageView expandedImageView;
    private float expandedImageCurrentX, expandedImageCurrentY;
    private int mDisplayWidth, mDisplayHeight;
//    private WebView expandedImageWebView;
    private ViewPager imageViewPager;
    private Animator mImageZoomAnimator;
    // Устанавливаем "короткое" время анимации.
    private int mShortAnimationDuration = 200;
    private int firstImageNum;

    private RecyclerView bindButtonsContainer;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movepic_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mPresenter.deleteCurrentImageBuffered();
                break;
            case R.id.action_add:
                mPresenter.addBindButton();
                break;
            case R.id.action_restore_image:
                mPresenter.onButtonRestoreImageClicked();
                break;
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) Objects.requireNonNull(getContext()).getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        mDisplayWidth = displayMetrics.widthPixels;
        mDisplayHeight = displayMetrics.heightPixels;
        Log.d(tag, "DisplayWidth = " + mDisplayWidth + ", DisplayHeight = " + mDisplayHeight);
        Bundle args = getArguments();
        if (args != null) {
            String pathFirstIMG = args.getString(MovePicContract.TAG_PATHPIC);
            mPresenter = new MovePicPresenter(this, (Router) getActivity(), pathFirstIMG);
            firstImageNum = args.getInt(MovePicContract.TAG_ITEM_NUM);
        } else {
            Log.e(tag, "can't get pathIMG");
            // TODO
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movepic, container, false);

        createImageViewer(view);
        createBindButtonsContainer(view);

        return view;
    }

    private void createImageViewer(View view) {
        imageContainer = view.findViewById(R.id.image_container);
        expandedImageView = view.findViewById(R.id.expanded_image);

        imageViewPager = view.findViewById(R.id.imageViewPager);
        imageViewPager.setAdapter(mPresenter.getImageAdapter());
        imageViewPager.setCurrentItem(firstImageNum);
    }

    private void createBindButtonsContainer(View view) {
        bindButtonsContainer = view.findViewById(R.id.bind_buttons_container);
        bindButtonsContainer.setAdapter(mPresenter.getBindButtonsAdapter());
        layoutManager = new LinearLayoutManager(getContext());
        ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);
        bindButtonsContainer.setLayoutManager(layoutManager);
    }

    @Override
    public Context getViewContext() {
        return getContext();
    }

    @Override
    public int getCurrentItemNum() {
        return imageViewPager.getCurrentItem();
    }

    @Override
    public int[] getSizeImageContainer() {
        int[] size = new int[2];
        size[0] = imageContainer.getWidth();
        size[1] = imageContainer.getHeight();
        Log.d(tag, "getSizeImageContainer: size = " + size[0] + "x" + size[1]);

        return size;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void zoomImageFromThumb(final View thumbView, Bitmap fullImage, float centerX, float centerY) {
        // Если происходит анимация в данный момент, немедленно ее прекращаем
        if (mImageZoomAnimator != null) {
            mImageZoomAnimator.cancel();
        }

        // Загружаем изображение с большим разрешением в ImageView
        expandedImageView.setImageBitmap(fullImage);

        // Рассчитываем начальные и конечные границы изображения.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // Начальные границы берем от прямоугольника компонента миниатюры,
        // а конечные от прямоугольника компонента полноразмерной картинки.
        // Также установим смещение границ для полноразмерной картинки в точку начала анимации.
        thumbView.getGlobalVisibleRect(startBounds);
        imageContainer.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Настроим начальные границы, чтобы у них было такое же соотношение как у конечных.
        // Это предотвратит нежелательное растягивание во время анимации.
        // также рассчитаем начальный коэффициент масштабирования (конечный коэффициент всегда равен 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Расширяем границы старта горизонтально
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Расширяем границы старта вертикально
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Прячем миниатюры и показываем полную картинку. Если анимация только началась,
        // полная картинка займет место миниатюры
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Устанавливаем опорные точки для SCALE_X и SCALE_Y (по умолчанию они в центре)
        expandedImageView.setPivotX(centerX);
        expandedImageView.setPivotY(centerY);

        // Создаем и запускаем параллельную анимацию для всех четырех свойств
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 2f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 2f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mImageZoomAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mImageZoomAnimator = null;
            }
        });
        set.start();
        mImageZoomAnimator = set;

        // При щелчке на полноразмерную картинку надо проделать обратную анимацию
        final float startScaleFinal = startScale;
        expandedImageView.setOnTouchListener(new OnDoubleTouchListener() {
            private void setNewPivot(float pivotX, float pivotY) {
                if (pivotX < 0) {
                    pivotX = 0;
                } else if (pivotX > mDisplayWidth) {
                    pivotX = mDisplayWidth;
                }
                if (pivotY < 0) {
                    pivotY = 0;
                } else if (pivotY > mDisplayHeight) {
                    pivotY = mDisplayHeight;
                }

                if (expandedImageView.getPivotX() != pivotX) {
                    expandedImageView.setPivotX(pivotX);
                }
                if (expandedImageView.getPivotY() != pivotY) {
                    expandedImageView.setPivotY(pivotY);
                }
            }

            @Override
            public void onDoubleClick(View view, MotionEvent event) {
                if (mImageZoomAnimator != null) {
                    mImageZoomAnimator.cancel();
                }

                // Анимируем все четыре свойства параллельно в обратную сторону
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mImageZoomAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mImageZoomAnimator = null;
                    }
                });
                set.start();
                mImageZoomAnimator = set;
            }

            @Override
            public void onOtherEvent(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    expandedImageCurrentX = event.getRawX();
                    expandedImageCurrentY = event.getRawY();
                    Log.d(tag, "expandedImageOnClick: X: " + expandedImageCurrentX + " Y: " + expandedImageCurrentY);
                }
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float x = event.getRawX();
                    float y = event.getRawY();
                    // Update how much the touch moved
                    float mDeltaX = x - expandedImageCurrentX;
                    float mDeltaY = y - expandedImageCurrentY;
//                    Log.d(tag, "expandedImageMove: deltaX = " + mDeltaX + ", deltaY = " + mDeltaY);
                    setNewPivot(expandedImageView.getPivotX() - mDeltaX,
                            expandedImageView.getPivotY() - mDeltaY);
//                    Log.d(tag, "PivotX: " + expandedImageView.getPivotX() + " PivotY: " + expandedImageView.getPivotY());
                    expandedImageCurrentX = x;
                    expandedImageCurrentY = y;
                }
            }
        });
    }

    /*@Override
    public void zoomImageFromThumbPath(String imagePath, float centerX, float centerY) {
        expandedImageWebView.loadUrl("file:///" + imagePath);
        expandedImageWebView.setVisibility(View.VISIBLE);
        expandedImageWebView.setOnClickListener(new OnDoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                expandedImageView.setVisibility(View.GONE);
            }
        });
    }*/
}
