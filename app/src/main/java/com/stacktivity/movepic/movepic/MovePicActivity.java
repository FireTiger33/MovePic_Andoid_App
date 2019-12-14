package com.stacktivity.movepic.movepic;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.data.MovePicRepository;
import com.stacktivity.movepic.filemanager.FileManagerActivity;
import com.stacktivity.movepic.providers.FileManagerDialogProvider;
import com.stacktivity.movepic.utils.ActivityUtils;

import java.util.Objects;

import static com.stacktivity.movepic.filemanager.FileManagerContract.KEY_DIALOG_SESSION;
import static com.stacktivity.movepic.filemanager.FileManagerContract.RC_FILE_MANAGER_DIALOG;
import static com.stacktivity.movepic.movepic.MovePicContract.KEY_PATH_IMAGE;


public class MovePicActivity extends AppCompatActivity implements FileManagerDialogProvider {
    private final String tag = MovePicActivity.class.getSimpleName();

    private MovePicPresenter presenter;

    private Toolbar mToolBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movepic);

        // Configure bottom ToolBar
        mToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(mToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        String pathImage = getIntent().getStringExtra(KEY_PATH_IMAGE);
        showMovePicScreen(pathImage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putString(KEY_PATH_IMAGE, presenter.getImagePath(presenter.getCurrentImageNum()));
    }

    public void showMovePicScreen(String pathPic) {
        // Activity configuration
        mToolBar.setVisibility(View.VISIBLE);

        // Create fragment
        MovePicView movePicView;
        try {
            movePicView = (MovePicView) getSupportFragmentManager()
                    .findFragmentById(R.id.main_container);
        } catch (ClassCastException e) {
            movePicView = null;
        }
        if (movePicView == null) {
            Log.d(tag, "create MovePic fragment");
            movePicView = new MovePicView();
            ActivityUtils.replaceFragmentInActivity(
                    getSupportFragmentManager(), movePicView, R.id.main_container);
        }

        // Create repository
        String MOVEPIC_PREFERENCES = "MovePicPreferences";
        SharedPreferences preferences = getSharedPreferences(MOVEPIC_PREFERENCES, Context.MODE_PRIVATE);
        MovePicRepository repository = new MovePicRepository(preferences, pathPic);

        // Create presenter
        presenter = new MovePicPresenter(movePicView, repository);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        presenter = null;
        mToolBar = null;
    }

    @Override
    public void showFileManagerDialog() {
        Intent intent = new Intent(this, FileManagerActivity.class);
        intent.putExtra(KEY_DIALOG_SESSION, true);
        startActivityForResult(intent, RC_FILE_MANAGER_DIALOG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(tag, "result" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == RC_FILE_MANAGER_DIALOG) {
                presenter.addBindButton(data.getStringExtra(KEY_DIALOG_SESSION));
            }
        }

    }
}
