package com.stacktivity.movepic.filemanager;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.stacktivity.movepic.R;
import com.stacktivity.movepic.data.FileManagerRepository;
import com.stacktivity.movepic.movepic.MovePicActivity;
import com.stacktivity.movepic.providers.FileManagerNavigationListener;
import com.stacktivity.movepic.providers.MovePicProvider;
import com.stacktivity.movepic.utils.ActivityUtils;

import java.util.Objects;

import static com.stacktivity.movepic.filemanager.FileManagerContract.KEY_DIALOG_SESSION;
import static com.stacktivity.movepic.filemanager.FileManagerContract.RC_FILE_MANAGER_DIALOG;
import static com.stacktivity.movepic.movepic.MovePicContract.KEY_PATH_IMAGE;


public class FileManagerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FileManagerNavigationListener, MovePicProvider {

    private final String tag = FileManagerActivity.class.getSimpleName();

    private final String KEY_SAVED_PATH = "path";

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private FileManagerView fileManagerView;
    private FileManagerPresenter mPresenter;

    private boolean isDialogSession;
    private String lastPath = null;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filemanager);

        // Check for dialog session
        Intent intent = getIntent();
        isDialogSession = intent.getBooleanExtra(KEY_DIALOG_SESSION, false);
        Log.d(tag, "is dialog session: " + isDialogSession);

        // Configure bottom ToolBar
        Toolbar mBottomToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(mBottomToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Configure top ToolBar
        Toolbar mTopToolBar = findViewById(R.id.top_tool_bar);
        if (isDialogSession) {
            showCloseButton(mTopToolBar);
        } else {
            showOpenNavigationViewButton(mTopToolBar);
        }

        // Check last directory before screen rotation
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SAVED_PATH)) {
                lastPath = savedInstanceState.getString(KEY_SAVED_PATH);
            }
        }

        // Check WRITE_EXTERNAL_STORAGE_PERMISSION
        boolean permissionGranted = checkPermission();
        if (permissionGranted) {
            createView();
        }

        // TODO AlertDialog if permission denied
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SAVED_PATH, mPresenter.getCurrentDirectoryPath());
    }

    void createView() {
        if (fileManagerView != null) {
            return;
        }
        createFileManagerView();
        initNavView();
        if (isDialogSession) {
            lockNavView();
        }
    }

    /**
     * Create all elements that make up MVP architecture for FileManager screen:
     * 1) Repository
     * 2) View - fragment, can resolved from FragmentManager
     * 3) Presenter(View, Repository)
     */
    private void createFileManagerView() {
        // Create view
        fileManagerView = (FileManagerView) getSupportFragmentManager()
                .findFragmentById(R.id.main_container);
        if (fileManagerView == null) {
            fileManagerView = new FileManagerView();
            Bundle args = new Bundle();
            args.putBoolean(KEY_DIALOG_SESSION, isDialogSession);
            fileManagerView.setArguments(args);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    fileManagerView, R.id.main_container);
        }

        // Create repository
        FileManagerRepository repository;
        if (lastPath == null) {
            repository = new FileManagerRepository(
                    getSharedPreferences("fileManager", MODE_PRIVATE));
        } else {
            repository = new FileManagerRepository(lastPath);
        }

        // Create presenter
        mPresenter = new FileManagerPresenter(fileManagerView, fileManagerView.getLifecycle(),
                repository, isDialogSession);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createView();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Check WRITE_EXTERNAL_STORAGE_PERMISSION
     * @return true if permission already granted
     */
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

    /**
     * NavigationView initialisation
     */
    private void initNavView() {
        drawer = findViewById(R.id.drawer_layout);
        /* // Add open NavigationView standard button
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mTopToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_manager_dialog_menu, menu);
        if (!isDialogSession) {
            menu.findItem(R.id.action_select).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                Intent intent = new Intent();
                intent.putExtra(KEY_DIALOG_SESSION, mPresenter.getCurrentDirectoryPath());
                setResult(RC_FILE_MANAGER_DIALOG, intent);
                finish();
                break;
            case R.id.action_add:
                Log.d(tag, "onAddFolderBtnClick");
                mPresenter.onAddFolderButtonClick();
                break;
            case R.id.action_add_nomedia:
                mPresenter.createNomedia();
                break;
            case R.id.action_search:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        /*if (fileManagerView.isVisible()) {
            if (!fileManagerView.goBack()) {
                super.onBackPressed();
            }
        } else if (fileManagerViewDialog != null && fileManagerViewDialog.isVisible()) {
            if (!fileManagerViewDialog.goBack()) {
                super.onBackPressed();
            }
        } else super.onBackPressed();*/

        if (fileManagerView.isVisible()) {
            if (!mPresenter.goBack()) {
                super.onBackPressed();
            }
        } else super.onBackPressed();
//        if (fragmentManager.getBackStackEntryCount() == 0) {
//            mToolBar.setVisibility(View.GONE);
//            unlockNavView();
//        }
    }

    @Override
    public void showMovePicScreen(String pathPic) {
        Intent intent = new Intent(this, MovePicActivity.class);
        intent.putExtra(KEY_PATH_IMAGE, pathPic);
        startActivity(intent);
    }

    @Override
    public void back() {
        onBackPressed();
    }

    @Override
    public void showNavigationView() {
        drawer.openDrawer(navigationView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private void showOpenNavigationViewButton(View toolbar) {
        toolbar.findViewById(R.id.action_close).setVisibility(View.GONE);
        Button openNavViewButton = findViewById(R.id.action_open_navigation_view);
        openNavViewButton.setVisibility(View.VISIBLE);
        openNavViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showNavigationView();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showCloseButton(View toolbar) {
        Button closeButton = toolbar.findViewById(R.id.action_close);
        closeButton.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void lockNavView() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        navigationView.setVisibility(View.GONE);
    }

    /*private void unlockNavView() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        navigationView.setVisibility(View.VISIBLE);
    }*/
}
