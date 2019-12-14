package com.stacktivity.movepic.filemanager;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stacktivity.movepic.R;

import java.io.File;

import static com.stacktivity.movepic.utils.FileWorker.isImage;

final class FileViewHolder extends RecyclerView.ViewHolder {

    private final TextView filename;
    private FileManagerView.OnClickFileListener clickListener;

    FileViewHolder(View view, FileManagerView.OnClickFileListener onClickFileManagerItem) {
        super(view);
        this.clickListener = onClickFileManagerItem;
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
                    clickListener.onClickDirectory(file);
                } else {
                    if (isImage(file.getPath())) {
                        clickListener.onClickImage(file);
                    }
                }
            }
        });
    }
}