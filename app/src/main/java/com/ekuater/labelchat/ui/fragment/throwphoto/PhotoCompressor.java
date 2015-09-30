package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.util.BmpUtils;
import com.ekuater.labelchat.util.L;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/1/7.
 *
 * @author LinYong
 */
public final class PhotoCompressor extends AsyncTask<File[], File, File[]> {

    private static final String TAG = PhotoCompressor.class.getSimpleName();
    private static final int COMPRESS_LIMIT_WIDTH = 600;
    private static final int COMPRESS_LIMIT_HEIGHT = 800;
    private static final int COMPRESS_SIZE_LIMIT = 100; // KB

    public interface OnCompressListener {
        public void onStart();

        public void onFinish(File[] compressedPhotoFiles);
    }

    public static void compress(File[] photoFiles, OnCompressListener listener) {
        PhotoCompressor compressor = new PhotoCompressor(listener);
        compressor.executeOnExecutor(THREAD_POOL_EXECUTOR, photoFiles);
    }

    private interface CompressListenerNotifier {
        public void notify(OnCompressListener listener);
    }

    private final OnCompressListener mListener;

    private PhotoCompressor(OnCompressListener listener) {
        mListener = listener;
    }

    private void notifyCompressListener(CompressListenerNotifier notifier) {
        if (mListener != null) {
            notifier.notify(mListener);
        }
    }

    private void notifyCompressStart() {
        notifyCompressListener(new StartCompressNotifier());
    }

    private void notifyCompressFinish(File[] compressedPhotoFiles) {
        notifyCompressListener(new FinishCompressNotifier(compressedPhotoFiles));
    }

    private File genCompressPhotoFile() {
        return EnvConfig.genTempFile("jpg");
    }

    @Override
    protected void onPreExecute() {
        notifyCompressStart();
    }

    @Override
    protected File[] doInBackground(File[]... params) {
        File[] photoFiles = params[0];
        List<File> compressFileList = new ArrayList<File>();

        if (photoFiles != null && photoFiles.length > 0) {
            for (File photoFile : photoFiles) {
                L.v(TAG, "Compress photo:" + photoFile.toString());
                try {
                    Bitmap compressBmp = BmpUtils.zoomDownBitmap(photoFile,
                            COMPRESS_LIMIT_WIDTH, COMPRESS_LIMIT_HEIGHT);
                    compressBmp = BmpUtils.compressBitmapBySize(compressBmp, COMPRESS_SIZE_LIMIT);
                    File compressFile = genCompressPhotoFile();
                    BmpUtils.saveBitmapToFile(compressBmp, compressFile);
                    compressFileList.add(compressFile);
                } catch (Exception e) {
                    L.w(TAG, e);
                }
            }
        }

        final int length = compressFileList.size();
        return (length > 0) ? compressFileList.toArray(new File[length]) : null;
    }

    @Override
    protected void onPostExecute(File[] files) {
        notifyCompressFinish(files);
    }

    private static class StartCompressNotifier implements CompressListenerNotifier {
        @Override
        public void notify(OnCompressListener listener) {
            listener.onStart();
        }
    }

    public static class FinishCompressNotifier implements CompressListenerNotifier {

        private final File[] compressedPhotoFiles;

        public FinishCompressNotifier(File[] compressedPhotoFiles) {
            this.compressedPhotoFiles = compressedPhotoFiles;
        }

        @Override
        public void notify(OnCompressListener listener) {
            listener.onFinish(compressedPhotoFiles);
        }
    }
}
