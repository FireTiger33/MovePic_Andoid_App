package com.stacktivity.movepic.utils;


import android.os.Environment;
import android.util.Log;

import com.stacktivity.movepic.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Deals with standard work with files
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

    /**
     * Safely move file
     * @param sourceFile source file
     * @param folderToSave new path to place
     * @return 0 for success result else err id
     */
    public int moveFile(File sourceFile, String folderToSave) {
        if (checkDirectoryAccess()) {
            int returnVal;
            if ((returnVal = copyFile(sourceFile, new File(folderToSave + sourceFile.getName()))) == 0) {
                if (sourceFile.delete()) {
                    return 0;
                }
                return R.string.file_delete_err;
            } else return returnVal;

        }

        return R.string.storage_not_found;
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
