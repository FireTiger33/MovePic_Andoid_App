package com.stacktivity.movepic;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.stacktivity.movepic.filemanager.FileManagerContract;
import com.stacktivity.movepic.filemanager.FileManagerView;
import com.stacktivity.movepic.movepic.MovePicContract;
import com.stacktivity.movepic.movepic.MovePicView;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Router {
    final private String tag = MainActivity.class.getName();


    private final String TAG_MOVEPIC = "MovePic";
    private final String TAG_FILEMANAGER = "FileManager";

    private Toolbar mToolBar;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    final private FragmentManager fragmentManager = getSupportFragmentManager();

    private FileManagerContract.Callback fileManagerCallback;

    private FileManagerView fileManagerView;
    private FileManagerView fileManagerViewDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(mToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
//        mToolBar.setVisibility(View.GONE);

        /*fragmentManager.beginTransaction()
                .replace(R.id.main_activity, new MainView())
                .commit();*/
        boolean permissionGranted = checkPermission();
    }

    private void createFileManagerView() {
        fileManagerView = new FileManagerView();
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, fileManagerView, TAG_FILEMANAGER)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //mPresenter.getFilesAdapter().init();
            createFileManagerView();
            initNavView();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean checkPermission() {
        Log.d(tag, "checkPermission");
        boolean returnVal = true;
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            returnVal = false;
        }

        String[] permissions = new String[1];
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        requestPermissions(permissions,
                1
        );

        return returnVal;

    }

    private void initNavView() {
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, fileManagerView.getToolBar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(fileManagerView);
    }

    @Override
    public void onBackPressed() {
        if (fileManagerView.isVisible()) {
            if (!fileManagerView.goBack()) {
                super.onBackPressed();
            }
        } else if (fileManagerViewDialog != null && fileManagerViewDialog.isVisible()) {
            if (!fileManagerViewDialog.goBack()) {
                super.onBackPressed();
            }
        } else super.onBackPressed();
        if (fragmentManager.getBackStackEntryCount() == 0) {
//            mToolBar.setVisibility(View.GONE);
            unlockNavView();
        }
    }

    @Override
    public void showMovePicScreen(final String pathPic, final int itemNum) {
        Log.d(tag, "showMovePicScreen");
        lockNavView();

        Bundle args = new Bundle();
        args.putString(MovePicContract.TAG_PATHPIC, pathPic);
        args.putInt(MovePicContract.TAG_ITEM_NUM, itemNum);

        MovePicView movePicView = new MovePicView();
        movePicView.setArguments(args);

        mToolBar.setVisibility(View.VISIBLE);

        fragmentManager.beginTransaction()
                .replace(R.id.main_container, movePicView, TAG_MOVEPIC)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showFileManagerDialog(final FileManagerContract.Callback callback) {
        fileManagerCallback = callback;

        Bundle bundle = new Bundle();
        bundle.putBoolean(FileManagerView.KEY_FILEMANAGER_DIALOG, true);

        fileManagerViewDialog = new FileManagerView();
        fileManagerViewDialog.setArguments(bundle);

        fragmentManager.beginTransaction()
                .replace(R.id.main_container, fileManagerViewDialog, TAG_FILEMANAGER)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void back() {
        fragmentManager.popBackStack();
        if (fileManagerView.isVisible()) {
            onBackPressed();
        }
    }

    @Override
    public void folderSelected(String folderPath) {
        fileManagerViewDialog.onDestroy();
        fileManagerViewDialog = null;
        fileManagerCallback.onSuccess(folderPath);
        fragmentManager.popBackStack();
    }

    @Override
    public void showNavigationView() {
        drawer.openDrawer(navigationView);
    }


    private boolean checkFileManagerCanGoBack(@Nullable FileManagerView view) {
        boolean returnVal = false;
        if (view != null) {
            if (view.isVisible()) {
                Log.d(tag, "fileManager isVisible");
                returnVal = view.goBack();
                Log.d(tag, "returnVal is " + returnVal);
            }
        }

        return !returnVal;
    }

    private void lockNavView() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView.setVisibility(View.GONE);
    }

    private void unlockNavView() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        navigationView.setVisibility(View.VISIBLE);
    }
}
