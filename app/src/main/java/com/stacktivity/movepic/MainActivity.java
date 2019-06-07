package com.stacktivity.movepic;

import android.app.ActionBar;
import android.app.TaskStackBuilder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.stacktivity.movepic.filemanager.FileManagerContract;
import com.stacktivity.movepic.filemanager.FileManagerView;
import com.stacktivity.movepic.movepic.MovePicContract;
import com.stacktivity.movepic.movepic.MovePicView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Router, NavigationView.OnNavigationItemSelectedListener {
    final private String tag = MainActivity.class.getName();


    private final String TAG_MOVEPIC = "MovePic";
    private final String TAG_FILEMANAGER = "FileManager";

    private Toolbar mToolBar;

    final private FragmentManager fragmentManager = getSupportFragmentManager();

    private FileManagerContract.Callback fileManagerCallback;

    private FileManagerView fileManagerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(mToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mToolBar.setVisibility(View.GONE);
        //initNavView();

        /*fragmentManager.beginTransaction()
                .replace(R.id.main_activity, new MainView())
                .commit();*/
        fileManagerView = new FileManagerView();
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, fileManagerView, TAG_FILEMANAGER)
                .commit();
    }

    private void initNavView() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (fileManagerView.isVisible()) {
            Log.d(tag, "onBackPressed: fileManager isVisible");
            if (fileManagerView.isParentDirectory()) {
                super.onBackPressed();
            }
        } else super.onBackPressed();
        if (fragmentManager.getBackStackEntryCount() == 0) {
            mToolBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showMovePicScreen(final String pathPic, final int itemNum) {
        Log.d(tag, "showMovePicScreen");
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

        fragmentManager.beginTransaction()
                .replace(R.id.main_container, fileManagerView)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void back() {
//        onBackPressed();
        fragmentManager.popBackStack();
    }

    @Override
    public void folderSelected(String folderPath) {
        fileManagerCallback.onSuccess(folderPath);
        fragmentManager.popBackStack();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
