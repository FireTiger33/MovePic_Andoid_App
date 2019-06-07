package com.stacktivity.movepic.filemanager;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.stacktivity.movepic.R;

import java.io.File;

final class FileViewHolder extends RecyclerView.ViewHolder {
    static final private String tag = FileViewHolder.class.getName();

    private final TextView filename;
    private FileManagerView.OnClickFileManagerItem clickManager;

    FileViewHolder(View view, FileManagerView.OnClickFileManagerItem onClickFileManagerItem) {
        super(view);
        this.clickManager = onClickFileManagerItem;
        filename = view.findViewById(R.id.filename);
    }

    @SuppressLint("SetTextI18n")
    void bind(final File file) {
        filename.setTypeface(null, Typeface.NORMAL);
        filename.setText(file.getName());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file.isDirectory()) {
                    clickManager.onClickDirectory(file);
                } else {
                    if (FileManagerPresenter.isImage(file.getPath())) {
                        clickManager.onClickImage(file, getAdapterPosition());
                    }
                }
            }
        });
    }
}