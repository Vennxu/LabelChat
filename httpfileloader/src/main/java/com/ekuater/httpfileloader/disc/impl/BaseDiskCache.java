package com.ekuater.httpfileloader.disc.impl;

import com.ekuater.httpfileloader.DefaultConfigurationFactory;
import com.ekuater.httpfileloader.disc.DiskCache;
import com.ekuater.httpfileloader.disc.naming.FileNameGenerator;
import com.ekuater.httpfileloader.utils.IoUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base disk cache.
 *
 * @see FileNameGenerator
 */
public abstract class BaseDiskCache implements DiskCache {

    /**
     * {@value
     */
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 Kb

    private static final String ERROR_ARG_NULL = " argument must be not null";
    private static final String TEMP_IMAGE_POSTFIX = ".tmp";

    protected final File cacheDir;
    protected final File reserveCacheDir;

    protected final FileNameGenerator fileNameGenerator;

    protected int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * @param cacheDir Directory for file caching
     */
    public BaseDiskCache(File cacheDir) {
        this(cacheDir, null);
    }

    /**
     * @param cacheDir        Directory for file caching
     * @param reserveCacheDir null-ok; Reserve directory for file caching. It's used when the primary directory isn't available.
     */
    public BaseDiskCache(File cacheDir, File reserveCacheDir) {
        this(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator());
    }

    /**
     * @param cacheDir          Directory for file caching
     * @param reserveCacheDir   null-ok; Reserve directory for file caching. It's used when the primary directory isn't available.
     * @param fileNameGenerator {@linkplain com.ekuater.httpfileloader.disc.naming.FileNameGenerator
     *                          Name generator} for cached files
     */
    public BaseDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir" + ERROR_ARG_NULL);
        }
        if (fileNameGenerator == null) {
            throw new IllegalArgumentException("fileNameGenerator" + ERROR_ARG_NULL);
        }

        this.cacheDir = cacheDir;
        this.reserveCacheDir = reserveCacheDir;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    public File getDirectory() {
        return cacheDir;
    }

    @Override
    public File get(String fileUri) {
        return getFile(fileUri);
    }

    @Override
    public boolean save(String fileUri, InputStream inputStream, IoUtils.CopyListener listener) throws IOException {
        File imageFile = getFile(fileUri);
        File tmpFile = new File(imageFile.getAbsolutePath() + TEMP_IMAGE_POSTFIX);
        boolean loaded = false;
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), bufferSize);
            try {
                loaded = IoUtils.copyStream(inputStream, os, listener, bufferSize);
            } finally {
                IoUtils.closeSilently(os);
            }
        } finally {
            if (loaded && !tmpFile.renameTo(imageFile)) {
                loaded = false;
            }
            if (!loaded) {
                //noinspection ResultOfMethodCallIgnored
                tmpFile.delete();
            }
        }
        return loaded;
    }

    @Override
    public boolean remove(String fileUri) {
        return getFile(fileUri).delete();
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
    }

    /**
     * Returns file object (not null) for incoming image URI. File object can reference to non-existing file.
     */
    protected File getFile(String imageUri) {
        String fileName = fileNameGenerator.generate(imageUri);
        File dir = cacheDir;
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            if (reserveCacheDir != null && (reserveCacheDir.exists() || reserveCacheDir.mkdirs())) {
                dir = reserveCacheDir;
            }
        }
        return new File(dir, fileName);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}