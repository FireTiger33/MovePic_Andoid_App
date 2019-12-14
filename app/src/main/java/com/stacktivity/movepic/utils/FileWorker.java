package com.stacktivity.movepic.utils;


import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.stacktivity.movepic.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;

import static androidx.core.util.Preconditions.checkNotNull;

/**
 * Deals with standard work with files
 * 1) Copy/move files
 * 2) Sort files
 * 3) Check image format
 */
public class FileWorker {
    private static final String tag = FileWorker.class.getSimpleName();

    /**
     * Copy file
     * @param sourceFile source file
     * @param folderToCopy new path to place
     * @return 0 for success result, else R.string err id
     */
    public int copyFile(File sourceFile, String folderToCopy) {
         if (checkDirectoryAccess()) {
            return copyFile(sourceFile, new File(folderToCopy + sourceFile.getName()));
         }

        return R.string.storage_not_found;
    }


    static public boolean deleteFile(String path) {
        return new File(path).delete();
    }

    static public boolean isImage(String filePath) {
        final String extension = MimeTypeMap
                .getFileExtensionFromUrl(Uri.parse(filePath).toString());
        final String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension);

        Log.d(tag, "extension: " + extension);
        Log.d(tag, "mimeType: " + mimeType);

        return mimeType != null && mimeType.contains("image");
    }

    static public File[] sortFiles(@NonNull final File[] files) {
        checkNotNull(files);
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (f2.isDirectory() && !f1.isDirectory()) return 1;
                return f1.getName().compareTo(f2.getName());
            }
        });

        return files;
    }

    /**
     * Copy file without check access files
     * @param sourceFile source file
     * @param destFile new file
     * @return 0 if success else err id
     */
    private int copyFile(File sourceFile, File destFile) {
        Log.d(tag, "copyFile");
        if (!destFile.exists()) {
            Log.d(tag, "File not exists. Start create");
            try {
                if (!destFile.createNewFile()) {
                    return R.string.cant_create_file;
                } else Log.d(tag, "file created");
            } catch (IOException e) {
                e.printStackTrace();
                return R.string.unexpected_error;
            }
        } else return R.string.file_exists;
        Log.d(tag, "copyFrom: " + sourceFile.getPath() + " to "+ destFile.getPath());

        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destFile).getChannel()
        ){
            destination.transferFrom(source, 0, source.size());
            Log.d(tag, "copyFile: finish");
            return 0;
        } catch (FileNotFoundException e) {
            Log.e(tag, "copyFile: " + R.string.source_file_not_found);
            e.printStackTrace();
            return R.string.source_file_not_found;
        } catch (IOException e) {
            Log.e(tag, "copyFile: sourceChannel was snap closed");
            e.printStackTrace();
            return R.string.unexpected_error;
        }
    }


    private static boolean checkDirectoryAccess(/*String path*/) {  // TODO for API 29+
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
