package com.stacktivity.movepic.filemanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stacktivity.movepic.R;
import com.stacktivity.movepic.providers.MovePicProvider;

import java.io.File;

import static androidx.core.util.Preconditions.checkNotNull;


public class FileManagerView extends Fragment implements FileManagerContract.View {
    final private String tag = FileManagerView.class.getName();

    private TextView folderPathTextView;
    private FileManagerContract.Presenter mPresenter;
    private View createNewFolderDialogView;

    interface OnClickFileListener {
        void onClickDirectory(final File file);
        void onClickImage(final File file);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(tag, "onCreate");
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);

        /*Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_DIALOG_SESSION) && args.getBoolean(KEY_DIALOG_SESSION)) {
            isDialogSession = true;
        }*/
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_manager, container, false);

        folderPathTextView = checkNotNull(getActivity()).findViewById(R.id.folder_path);

        configFilesView(view);

        createNewFolderDialogView = inflater.inflate(R.layout.create_folder_dialog, null, false);

        return view;
    }

    @Override
    public void showFolderPath(String path) {
        folderPathTextView.setText(path);
    }

    @Override
    public void showMovePicScreen(String imagePath) {
        try {
            MovePicProvider provider = (MovePicProvider) getActivity();
            checkNotNull(provider);
            ((MovePicProvider) getActivity()).showMovePicScreen(imagePath);
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void setPresenter(FileManagerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
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

    private void configFilesView(View view) {
        Log.d(tag, "configFilesView");
        RecyclerView filesView = view.findViewById(R.id.files);
        filesView.setAdapter(mPresenter.getFilesAdapter());
        filesView.setLayoutManager(new LinearLayoutManager(getContext()));
        showFolderPath(mPresenter.getCurrentDirectoryPath());
    }
}
