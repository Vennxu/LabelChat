package com.ekuater.httpfileloader.listener;

/**
 * Listener for image loading progress.
 */
public interface FileLoadingProgressListener {

    /**
     * Is called when image loading progress changed.
     *
     * @param fileUri Image URI
     * @param current Downloaded size in bytes
     * @param total   Total size in bytes
     */
    void onProgressUpdate(String fileUri, int current, int total);
}
