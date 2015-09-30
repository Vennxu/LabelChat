package com.ekuater.httpfileloader.listener;

import com.ekuater.httpfileloader.assist.FailReason;

import java.io.File;

/**
 * Listener for image loading process.
 */
public interface FileLoadingListener {

    /**
     * Is called when image loading task was started
     *
     * @param fileUri Loading  URI
     */
    void onLoadingStarted(String fileUri);

    /**
     * Is called when an error was occurred during image loading
     *
     * @param fileUri    Loading file URI
     * @param failReason why file loading was failed
     */
    void onLoadingFailed(String fileUri, FailReason failReason);

    /**
     * Is called when image is loaded successfully (and displayed in View if one was specified)
     *
     * @param fileUri Loaded file URI
     * @param file    Bitmap of loaded and decoded image
     */
    void onLoadingComplete(String fileUri, File file);

    /**
     * Is called when image loading task was cancelled because View for image was reused in newer task
     *
     * @param fileUri Loading file URI
     */
    void onLoadingCancelled(String fileUri);
}
