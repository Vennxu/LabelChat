package com.ekuater.httpfileloader.listener;

import com.ekuater.httpfileloader.assist.FailReason;

import java.io.File;

/**
 * A convenient class to extend when you only want to listen for a subset of all the image loading events. This
 * implements all methods in the {@link FileLoadingListener} but does
 * nothing.
 */
public class SimpleFileLoadingListener implements FileLoadingListener {
    @Override
    public void onLoadingStarted(String fileUri) {
        // Empty implementation
    }

    @Override
    public void onLoadingFailed(String fileUri, FailReason failReason) {
        // Empty implementation
    }

    @Override
    public void onLoadingComplete(String fileUri, File file) {
        // Empty implementation
    }

    @Override
    public void onLoadingCancelled(String fileUri) {
        // Empty implementation
    }
}
