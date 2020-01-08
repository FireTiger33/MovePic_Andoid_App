package com.stacktivity.movepic.movepic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.controllers.OnDoubleClickListener;
import com.stacktivity.movepic.controllers.OnDoubleTouchListener;
import com.stacktivity.movepic.providers.FileManagerDialogProvider;
import com.stacktivity.movepic.utils.ToolbarDemonstrator;


import static androidx.core.util.Preconditions.checkNotNull;
import static com.stacktivity.movepic.filemanager.FileManagerContract.KEY_DIALOG_SESSION;
import static com.stacktivity.movepic.filemanager.FileManagerContract.RC_FILE_MANAGER_DIALOG;


public class MovePicView extends Fragment implements MovePicContract.View {
    final private String tag = MovePicView.class.getName();

    private MovePicContract.Presenter mPresenter;
    private ToolbarDemonstrator toolbarDemonstrator;

    private FrameLayout imageContainer;
    private ImageView expandedImageView;
    private TextView viewCurrentImageNum;
    private float expandedImageCurrentX, expandedImageCurrentY;
//    private WebView expandedImageWebView;
    private ViewPager imageViewPager;
    private Animator mImageZoomAnimator;
    private final int mShortAnimationDuration = 200;
    private boolean fullscreen = false;


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movepic_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mPresenter.deleteCurrentImageBuffered();
                break;
            case R.id.action_add:
                showFileManagerDialog();
                break;
            case R.id.action_edit:
                mPresenter.changeBindButtonMode();
                if (mPresenter.removeBindButtonsMode()) {
                    item.setTitle(R.string.apply_changes);
                } else {
                    item.setTitle(R.string.remove_buttons);
                }
                break;
            case R.id.action_restore_image:
                mPresenter.onButtonRestoreImageClicked();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(tag, "result" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == RC_FILE_MANAGER_DIALOG) {
                mPresenter.addBindButton(data.getStringExtra(KEY_DIALOG_SESSION));
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(tag, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movepic, container, false);

        createImageViewer(view);
        createBindButtonsContainer(view);

        viewCurrentImageNum = view.findViewById(R.id.current_image_num);
        viewCurrentImageNum.setText(getCurrentImagePositionFromAll());

        if (savedInstanceState != null) {
            fullscreen = savedInstanceState.getBoolean("fullscreen", false);

            if (fullscreen) {
                toolbarDemonstrator.hideActionBar();
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("fullscreen", fullscreen);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter = null;
        imageContainer = null;
        expandedImageView = null;
        imageViewPager = null;
        mImageZoomAnimator = null;
    }

    private void createImageViewer(View view) {
        Log.d(tag, "createImageViewer");
        imageContainer = view.findViewById(R.id.image_container);
        expandedImageView = view.findViewById(R.id.expanded_image);

        imageViewPager = view.findViewById(R.id.imageViewPager);
        imageViewPager.setAdapter(mPresenter.getImageAdapter());
        imageViewPager.setCurrentItem(mPresenter.getCurrentImageNum());
        imageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageScrollStateChanged(int state) { }

            @Override
            public void onPageSelected(int position) {
                Log.d(tag, "Current image " + position);
                mPresenter.onImagePageHasChange(position);
                viewCurrentImageNum.setText(getCurrentImagePositionFromAll());
            }
        });
    }

    private String getCurrentImagePositionFromAll() {
        return imageViewPager.getCurrentItem()+1 + "/" + mPresenter.getCountImages();
    }

    private void createBindButtonsContainer(View view) {
        RecyclerView bindButtonsContainer = view.findViewById(R.id.bind_buttons_container);
        bindButtonsContainer.setAdapter(mPresenter.getBindButtonsAdapter());
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        bindButtonsContainer.setLayoutManager(lm);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT, 0)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                mPresenter.moveBindButton(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
        }).attachToRecyclerView(bindButtonsContainer);
    }

    @Override
    public void setPresenter(MovePicContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void setToolbarDemonstrator(ToolbarDemonstrator demonstrator) {
        toolbarDemonstrator = demonstrator;
    }

    @Override
    public void showToast(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showImage(int numImage) {
        imageViewPager.setCurrentItem(numImage);
    }


    @Override
    public void showFullscreenMode() {
        fullscreen = !fullscreen;

        int screenFlag;
        int visibilityFlag;
        FragmentActivity activity = checkNotNull(getActivity());

        if (fullscreen) {
            screenFlag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            visibilityFlag = View.INVISIBLE;
        } else {
            screenFlag = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            visibilityFlag = View.VISIBLE;
        }

        activity.getWindow().setFlags(screenFlag, screenFlag);
        viewCurrentImageNum.setVisibility(visibilityFlag);

        if (toolbarDemonstrator != null) {
            if (fullscreen) {
                toolbarDemonstrator.hideActionBar();
            } else {
                toolbarDemonstrator.showActionBar();
            }
        }
    }

    @Override
    public int[] getSizeImageContainer() {
        int[] size = new int[2];
        size[0] = imageContainer.getWidth();
        size[1] = imageContainer.getHeight();
//        Log.d(tag, "getSizeImageContainer: size = " + size[0] + "x" + size[1]);

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

        // Проверяем состояние Toolbar'a и прячем его
        final boolean toolbarWillBeShown = toolbarDemonstrator.actionBarIsShown();
        if (toolbarWillBeShown) {
            toolbarDemonstrator.hideActionBar();
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
                } else if (pivotX > getSizeImageContainer()[0]) {
                    pivotX = getSizeImageContainer()[0];
                }
                if (pivotY < 0) {
                    pivotY = 0;
                } else if (pivotY > getSizeImageContainer()[1]) {
                    pivotY = getSizeImageContainer()[1];
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

                if (toolbarWillBeShown) {
                    toolbarDemonstrator.showActionBar();
                }
            }



            @Override
            public void onOtherEvent(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    expandedImageCurrentX = event.getRawX();
                    expandedImageCurrentY = event.getRawY();
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

    @Override
    public void showFileManagerDialog() {
        FileManagerDialogProvider provider = (FileManagerDialogProvider) getActivity();
        checkNotNull(provider);
        provider.showFileManagerDialog();
    }

    @Override
    public void close() {
        checkNotNull(getActivity()).onBackPressed();
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
