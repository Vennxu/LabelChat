package com.ekuater.httpfileloader;

import android.os.Handler;

/**
 * Contains options for image display.
 */
public final class FileLoadOptions {

    private final Object extraForDownloader;
    private final Handler handler;
    private final boolean isSyncLoading;

    private FileLoadOptions(Builder builder) {
        extraForDownloader = builder.extraForDownloader;
        handler = builder.handler;
        isSyncLoading = builder.isSyncLoading;
    }

    public Object getExtraForDownloader() {
        return extraForDownloader;
    }

    public Handler getHandler() {
        return handler;
    }

    boolean isSyncLoading() {
        return isSyncLoading;
    }

    /**
     * Builder for {@link FileLoadOptions}
     *
     * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
     */
    public static class Builder {

        private Object extraForDownloader = null;
        private Handler handler = null;
        private boolean isSyncLoading = false;

        public Builder() {
        }

        /**
         * Sets auxiliary object which will be passed to
         */
        public Builder extraForDownloader(Object extra) {
            this.extraForDownloader = extra;
            return this;
        }

        Builder syncLoading(boolean isSyncLoading) {
            this.isSyncLoading = isSyncLoading;
            return this;
        }

        /**
         * Sets custom {@linkplain Handler handler} for displaying images and firing events.
         */
        public Builder handler(Handler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Sets all options equal to incoming options
         */
        public Builder cloneFrom(FileLoadOptions options) {
            extraForDownloader = options.extraForDownloader;
            handler = options.handler;
            isSyncLoading = options.isSyncLoading;
            return this;
        }

        /**
         * Builds configured {@link FileLoadOptions} object
         */
        public FileLoadOptions build() {
            return new FileLoadOptions(this);
        }
    }

    /**
     * Creates options appropriate for single displaying:
     * These option are appropriate for simple single-use image (from drawables or from Internet) displaying.
     */
    public static FileLoadOptions createSimple() {
        return new Builder().build();
    }
}
