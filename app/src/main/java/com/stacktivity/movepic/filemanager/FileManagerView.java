package com.stacktivity.movepic.filemanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.Router;

import java.io.File;
import java.util.Objects;


public class FileManagerView extends Fragment implements FileManagerContract.View,
        NavigationView.OnNavigationItemSelectedListener {
    final private String tag = FileManagerView.class.getName();

    final static public String KEY_FILEMANAGER_DIALOG = "FileManagerDialog";
    private static final String FILE_MANAGER_PREFERENCES = "FileManagerPreferences";
    final private String KEY_PATH = "Path";

    private boolean isDialogSession;
    private SharedPreferences mPreferences;
    private String restoredPath;

    private View mView;
    private View mTopToolBar;
    private TextView folderPathTextView;
    private FileManagerContract.Presenter mPresenter;
    private Router router;
    private View createNewFolderDialogView;
    private int PERMISSION_CODE = 1234;

    @Override
    public Context getViewContext() {
        return getContext();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    interface OnClickFileManagerItem {
        void onClickDirectory(final File file);
        void onClickImage(final File file, int imagePosition);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(tag, "onCreateOptionsMenu");
        inflater.inflate(R.menu.file_manager_dialog_menu, menu);

        if (!isDialogSession) {
            menu.findItem(R.id.action_select).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                router.folderSelected(mPresenter.getCurrentDirectory());
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(tag, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_FILEMANAGER_DIALOG) && args.getBoolean(KEY_FILEMANAGER_DIALOG)) {
            isDialogSession = true;
        }

        router = (Router) getActivity();
        mPresenter = new FileManagerPresenter(this, (Router) getActivity());
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_file_manager, container, false);

        mPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(FILE_MANAGER_PREFERENCES, Context.MODE_PRIVATE);
        if (mPreferences.contains(KEY_PATH)) {
            restoredPath = mPreferences.getString(KEY_PATH, null);
            mPresenter.restorePath(restoredPath);
        }
        Log.d(tag, "restoredPath: " + restoredPath);

        mTopToolBar = mView.findViewById(R.id.top_tool_bar);
        if (isDialogSession) {
            showCloseButton();
        } else {
            showOpenNavigationViewButton();
        }

        folderPathTextView = mTopToolBar.findViewById(R.id.folder_path);

        configFilesView(mView);

        checkPermission(mView);

        createNewFolderDialogView = inflater.inflate(R.layout.create_folder_dialog, null, false);

        return mView;
    }

    private void configFilesView(View view) {
        Log.d(tag, "configFilesView");
        RecyclerView recyclerView = view.findViewById(R.id.files);
        recyclerView.setAdapter(mPresenter.getFilesAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        showFolderPath(mPresenter.getCurrentDirectory());
    }

    private void checkPermission(View view) {
        Log.d(tag, "checkPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    private void showCloseButton() {
        Button closeButton = mTopToolBar.findViewById(R.id.action_close);
        closeButton.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.back();
            }
        });
    }

    private void showOpenNavigationViewButton() {
        mTopToolBar.findViewById(R.id.action_close).setVisibility(View.GONE);
        Button openNavViewButton = mView.findViewById(R.id.action_open_navigation_view);
        openNavViewButton.setVisibility(View.VISIBLE);
        openNavViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                router.showNavigationView();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mPresenter.getFilesAdapter().init();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void showCreateFolderDialog() {
        Log.d(tag, "showCreateFolderDialog");
        final AlertDialog.Builder createNewFolderDialog = new AlertDialog.Builder(getContext());
        final EditText folderName = createNewFolderDialogView.findViewById(R.id.folder_name);
        if (createNewFolderDialogView.getParent() != null) {
            ((ViewGroup) createNewFolderDialogView.getParent()).removeView(createNewFolderDialogView);
        }
        createNewFolderDialog.setView(createNewFolderDialogView);
        createNewFolderDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (folderName.getText() != null) {
                    mPresenter.createFolder(folderName.getText().toString());
                }
            }
        });
        createNewFolderDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                folderName.setText("");
            }
        });
        createNewFolderDialog.show();
    }

    @Override
    public void showFolderPath(String path) {
        folderPathTextView.setText(path);
    }

    @Override
    public Toolbar getToolBar() {
        return (Toolbar) mTopToolBar;
    }

    public boolean isParentDirectory() {
        return !mPresenter.getFilesAdapter().goBack();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(tag, "onPause");

        mPreferences.edit().putString(KEY_PATH, mPresenter.getCurrentDirectory()).apply();
    }
}
