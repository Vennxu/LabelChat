package com.ekuater.httpfileloader.disc;

import com.ekuater.httpfileloader.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for disk cache
 */
public interface DiskCache {
    /**
     * Returns root directory of disk cache
     *
     * @return Root directory of disk cache
     */
    File getDirectory();

    /**
     * Returns file of cached image
     *
     * @param fileUri Original image URI
     * @return File of cached image or <b>null</b> if image wasn't cached
     */
    File get(String fileUri);

    /**
     * Saves image stream in disk cache.
     * Incoming image stream shouldn't be closed in this method.
     *
     * @param fileUri     Original image URI
     * @param inputStream Input stream of image (shouldn't be closed in this method)
     * @param listener    Listener for saving progress, can be ignored if you don't use
     *                    {@linkplain com.ekuater.httpfileloader.listener.FileLoadingProgressListener
     *                    progress listener} in ImageLoader calls
     * @return <b>true</b> - if image was saved successfully; <b>false</b> - if image wasn't saved in disk cache.
     * @throws java.io.IOException
     */
    boolean save(String fileUri, InputStream inputStream, IoUtils.CopyListener listener) throws IOException;

    /**
     * Removes image file associated with incoming URI
     *
     * @param fileUri Image URI
     * @return <b>true</b> - if image file is deleted successfully; <b>false</b> - if image file doesn't exist for
     * incoming URI or image file can't be deleted.
     */
    boolean remove(String fileUri);

    /**
     * Closes disk cache, releases resources.
     */
    void close();

    /**
     * Clears disk cache.
     */
    void clear();
}
