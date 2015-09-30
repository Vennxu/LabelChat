package com.ekuater.httpfileloader;

import android.content.Context;
import android.content.res.Resources;

import com.ekuater.httpfileloader.download.FileDownloader;
import com.ekuater.httpfileloader.disc.DiskCache;
import com.ekuater.httpfileloader.disc.naming.FileNameGenerator;
import com.ekuater.httpfileloader.assist.FlushedInputStream;
import com.ekuater.httpfileloader.assist.QueueProcessingType;
import com.ekuater.httpfileloader.utils.L;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * Presents configuration for {@link FileLoader}
 *
 * @see FileLoader
 * @see DiskCache
 * @see FileLoadOptions
 * @see com.ekuater.httpfileloader.download.FileDownloader
 * @see FileNameGenerator
 * @since 1.0.0
 */
public final class FileLoaderConfiguration {

    final Resources resources;

    final Executor taskExecutor;
    final Executor taskExecutorForCachedFiles;
    final boolean customExecutor;
    final boolean customExecutorForCachedImages;

    final int threadPoolSize;
    final int threadPriority;
    final QueueProcessingType tasksProcessingType;

    final DiskCache diskCache;
    final FileDownloader downloader;
    final FileLoadOptions defaultFileLoadOptions;

    final FileDownloader networkDeniedDownloader;
    final FileDownloader slowNetworkDownloader;

    private FileLoaderConfiguration(final Builder builder) {
        resources = builder.context.getResources();
        taskExecutor = builder.taskExecutor;
        taskExecutorForCachedFiles = builder.taskExecutorForCachedFiles;
        threadPoolSize = builder.threadPoolSize;
        threadPriority = builder.threadPriority;
        tasksProcessingType = builder.tasksProcessingType;
        diskCache = builder.diskCache;
        defaultFileLoadOptions = builder.defaultFileLoadOptions;
        downloader = builder.downloader;

        customExecutor = builder.customExecutor;
        customExecutorForCachedImages = builder.customExecutorForCachedImages;

        networkDeniedDownloader = new NetworkDeniedFileDownloader(downloader);
        slowNetworkDownloader = new SlowNetworkFileDownloader(downloader);

        L.writeDebugLogs(builder.writeLogs);
    }

    /**
     * Creates default configuration for {@link FileLoader} <br />
     */
    public static FileLoaderConfiguration createDefault(Context context) {
        return new Builder(context).build();
    }

    /**
     * Builder for {@link FileLoaderConfiguration}
     *
     * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
     */
    public static class Builder {

        private static final String WARNING_OVERLAP_DISK_CACHE_PARAMS = "diskCache(), diskCacheSize() and diskCacheFileCount calls overlap each other";
        private static final String WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR = "diskCache() and diskCacheFileNameGenerator() calls overlap each other";
        private static final String WARNING_OVERLAP_EXECUTOR = "threadPoolSize(), threadPriority() and tasksProcessingOrder() calls "
                + "can overlap taskExecutor() and taskExecutorForCachedFiles() calls.";

        /**
         * {@value}
         */
        public static final int DEFAULT_THREAD_POOL_SIZE = 3;
        /**
         * {@value}
         */
        public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 2;
        /**
         * {@value}
         */
        public static final QueueProcessingType DEFAULT_TASK_PROCESSING_TYPE = QueueProcessingType.FIFO;

        private Context context;

        private Executor taskExecutor = null;
        private Executor taskExecutorForCachedFiles = null;
        private boolean customExecutor = false;
        private boolean customExecutorForCachedImages = false;

        private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        private int threadPriority = DEFAULT_THREAD_PRIORITY;
        private QueueProcessingType tasksProcessingType = DEFAULT_TASK_PROCESSING_TYPE;

        private long diskCacheSize = 0;
        private int diskCacheFileCount = 0;

        private DiskCache diskCache = null;
        private FileNameGenerator diskCacheFileNameGenerator = null;
        private FileDownloader downloader = null;
        private FileLoadOptions defaultFileLoadOptions = null;

        private boolean writeLogs = false;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * Sets custom {@linkplain Executor executor} for tasks of loading and displaying images.<br />
         * <br />
         * <b>NOTE:</b> If you set custom executor then following configuration options will not be considered for this
         * executor:
         * <ul>
         * <li>{@link #threadPoolSize(int)}</li>
         * <li>{@link #threadPriority(int)}</li>
         * <li>{@link #tasksProcessingOrder(QueueProcessingType)}</li>
         * </ul>
         *
         * @see #taskExecutorForCachedFiles(Executor)
         */
        public Builder taskExecutor(Executor executor) {
            if (threadPoolSize != DEFAULT_THREAD_POOL_SIZE || threadPriority != DEFAULT_THREAD_PRIORITY || tasksProcessingType != DEFAULT_TASK_PROCESSING_TYPE) {
                L.w(WARNING_OVERLAP_EXECUTOR);
            }

            this.taskExecutor = executor;
            return this;
        }

        /**
         * Sets custom {@linkplain Executor executor} for tasks of displaying <b>cached on disk</b> images (these tasks
         * are executed quickly so UIL prefer to use separate executor for them).<br />
         * <br />
         * If you set the same executor for {@linkplain #taskExecutor(Executor) general tasks} and
         * tasks about cached images (this method) then these tasks will be in the
         * same thread pool. So short-lived tasks can wait a long time for their turn.<br />
         * <br />
         * <b>NOTE:</b> If you set custom executor then following configuration options will not be considered for this
         * executor:
         * <ul>
         * <li>{@link #threadPoolSize(int)}</li>
         * <li>{@link #threadPriority(int)}</li>
         * <li>{@link #tasksProcessingOrder(QueueProcessingType)}</li>
         * </ul>
         *
         * @see #taskExecutor(Executor)
         */
        public Builder taskExecutorForCachedFiles(Executor executorForCachedFiles) {
            if (threadPoolSize != DEFAULT_THREAD_POOL_SIZE
                    || threadPriority != DEFAULT_THREAD_PRIORITY
                    || tasksProcessingType != DEFAULT_TASK_PROCESSING_TYPE) {
                L.w(WARNING_OVERLAP_EXECUTOR);
            }

            this.taskExecutorForCachedFiles = executorForCachedFiles;
            return this;
        }

        /**
         * Sets thread pool size for image display tasks.<br />
         * Default value - {@link #DEFAULT_THREAD_POOL_SIZE this}
         */
        public Builder threadPoolSize(int threadPoolSize) {
            if (taskExecutor != null || taskExecutorForCachedFiles != null) {
                L.w(WARNING_OVERLAP_EXECUTOR);
            }

            this.threadPoolSize = threadPoolSize;
            return this;
        }

        /**
         * Sets the priority for image loading threads. Should be <b>NOT</b> greater than {@link Thread#MAX_PRIORITY} or
         * less than {@link Thread#MIN_PRIORITY}<br />
         * Default value - {@link #DEFAULT_THREAD_PRIORITY this}
         */
        public Builder threadPriority(int threadPriority) {
            if (taskExecutor != null || taskExecutorForCachedFiles != null) {
                L.w(WARNING_OVERLAP_EXECUTOR);
            }

            if (threadPriority < Thread.MIN_PRIORITY) {
                this.threadPriority = Thread.MIN_PRIORITY;
            } else {
                if (threadPriority > Thread.MAX_PRIORITY) {
                    this.threadPriority = Thread.MAX_PRIORITY;
                } else {
                    this.threadPriority = threadPriority;
                }
            }
            return this;
        }

        /**
         * Sets type of queue processing for tasks for loading and displaying images.<br />
         * Default value - {@link QueueProcessingType#FIFO}
         */
        public Builder tasksProcessingOrder(QueueProcessingType tasksProcessingType) {
            if (taskExecutor != null || taskExecutorForCachedFiles != null) {
                L.w(WARNING_OVERLAP_EXECUTOR);
            }

            this.tasksProcessingType = tasksProcessingType;
            return this;
        }

        /**
         * Sets maximum disk cache size for images (in bytes).<br />
         * By default: disk cache is unlimited.<br />
         * <b>NOTE:</b> If you use this method then
         * {@link com.ekuater.httpfileloader.disc.impl.ext.LruDiskCache LruDiskCache}
         * will be used as disk cache. You can use {@link #diskCache(DiskCache)} method for introduction your own
         * implementation of {@link DiskCache}
         */
        public Builder diskCacheSize(int maxCacheSize) {
            if (maxCacheSize <= 0)
                throw new IllegalArgumentException("maxCacheSize must be a positive number");

            if (diskCache != null) {
                L.w(WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }

            this.diskCacheSize = maxCacheSize;
            return this;
        }

        /**
         * Sets maximum file count in disk cache directory.<br />
         * By default: disk cache is unlimited.<br />
         * <b>NOTE:</b> If you use this method then
         * {@link com.ekuater.httpfileloader.disc.impl.ext.LruDiskCache LruDiskCache}
         * will be used as disk cache. You can use {@link #diskCache(DiskCache)} method for introduction your own
         * implementation of {@link DiskCache}
         */
        public Builder diskCacheFileCount(int maxFileCount) {
            if (maxFileCount <= 0)
                throw new IllegalArgumentException("maxFileCount must be a positive number");

            if (diskCache != null) {
                L.w(WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }

            this.diskCacheFileCount = maxFileCount;
            return this;
        }

        /**
         * Sets name generator for files cached in disk cache.<br />
         * Default value -
         * {@link DefaultConfigurationFactory#createFileNameGenerator()
         * DefaultConfigurationFactory.createFileNameGenerator()}
         */
        public Builder diskCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
            if (diskCache != null) {
                L.w(WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
            }

            this.diskCacheFileNameGenerator = fileNameGenerator;
            return this;
        }

        /**
         * Sets disk cache for images.<br />
         * Default value - {@link com.ekuater.httpfileloader.disc.impl.UnlimitedDiskCache
         * UnlimitedDiskCache}. Cache directory is defined by
         * {@link com.ekuater.httpfileloader.utils.StorageUtils#getCacheDirectory(Context)
         * StorageUtils.getCacheDirectory(Context)}.<br />
         * <br />
         * <b>NOTE:</b> If you set custom disk cache then following configuration option will not be considered:
         * <ul>
         * <li>{@link #diskCacheSize(int)}</li>
         * <li>{@link #diskCacheFileCount(int)}</li>
         * <li>{@link #diskCacheFileNameGenerator(FileNameGenerator)}</li>
         * </ul>
         */
        public Builder diskCache(DiskCache diskCache) {
            if (diskCacheSize > 0 || diskCacheFileCount > 0) {
                L.w(WARNING_OVERLAP_DISK_CACHE_PARAMS);
            }
            if (diskCacheFileNameGenerator != null) {
                L.w(WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
            }

            this.diskCache = diskCache;
            return this;
        }

        /**
         * Sets utility which will be responsible for downloading of image.<br />
         * Default value -
         * {@link DefaultConfigurationFactory#createImageDownloader(Context)
         * DefaultConfigurationFactory.createImageDownloader()}
         */
        public Builder fileDownloader(FileDownloader fileDownloader) {
            this.downloader = fileDownloader;
            return this;
        }

        /**
         * Sets default {@linkplain FileLoadOptions display image options} for image displaying. These options will
         * be used for every
         * without passing custom {@linkplain FileLoadOptions options}<br />
         * Default value - {@link FileLoadOptions#createSimple() Simple options}
         */
        public Builder defaultFileLoadOptions(FileLoadOptions defaultFileLoadOptions) {
            this.defaultFileLoadOptions = defaultFileLoadOptions;
            return this;
        }

        /**
         * Enables detail logging of {@link FileLoader} work. To prevent detail logs don't call this method.
         * Consider {@link com.ekuater.httpfileloader.utils.L#disableLogging()} to disable
         * ImageLoader logging completely (even error logs)
         */
        public Builder writeDebugLogs() {
            this.writeLogs = true;
            return this;
        }

        /**
         * Builds configured {@link FileLoaderConfiguration} object
         */
        public FileLoaderConfiguration build() {
            initEmptyFieldsWithDefaultValues();
            return new FileLoaderConfiguration(this);
        }

        private void initEmptyFieldsWithDefaultValues() {
            if (taskExecutor == null) {
                taskExecutor = DefaultConfigurationFactory
                        .createExecutor(threadPoolSize, threadPriority, tasksProcessingType);
            } else {
                customExecutor = true;
            }
            if (taskExecutorForCachedFiles == null) {
                taskExecutorForCachedFiles = DefaultConfigurationFactory
                        .createExecutor(threadPoolSize, threadPriority, tasksProcessingType);
            } else {
                customExecutorForCachedImages = true;
            }
            if (diskCache == null) {
                if (diskCacheFileNameGenerator == null) {
                    diskCacheFileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
                }
                diskCache = DefaultConfigurationFactory
                        .createDiskCache(context, diskCacheFileNameGenerator, diskCacheSize, diskCacheFileCount);
            }
            if (downloader == null) {
                downloader = DefaultConfigurationFactory.createImageDownloader(context);
            }
            if (defaultFileLoadOptions == null) {
                defaultFileLoadOptions = FileLoadOptions.createSimple();
            }
        }
    }

    /**
     * Decorator. Prevents downloads from network (throws {@link IllegalStateException exception}).<br />
     * In most cases this downloader shouldn't be used directly.
     */
    private static class NetworkDeniedFileDownloader implements FileDownloader {

        private final FileDownloader wrappedDownloader;

        public NetworkDeniedFileDownloader(FileDownloader wrappedDownloader) {
            this.wrappedDownloader = wrappedDownloader;
        }

        @Override
        public InputStream getStream(String fileUri, Object extra) throws IOException {
            switch (Scheme.ofUri(fileUri)) {
                case HTTP:
                case HTTPS:
                    throw new IllegalStateException();
                default:
                    return wrappedDownloader.getStream(fileUri, extra);
            }
        }
    }

    /**
     * Decorator. Handles <a href="http://code.google.com/p/android/issues/detail?id=6066">this problem</a> on slow networks
     * using {@link com.ekuater.httpfileloader.assist.FlushedInputStream}.
     */
    private static class SlowNetworkFileDownloader implements FileDownloader {

        private final FileDownloader wrappedDownloader;

        public SlowNetworkFileDownloader(FileDownloader wrappedDownloader) {
            this.wrappedDownloader = wrappedDownloader;
        }

        @Override
        public InputStream getStream(String fileUri, Object extra) throws IOException {
            InputStream imageStream = wrappedDownloader.getStream(fileUri, extra);
            switch (Scheme.ofUri(fileUri)) {
                case HTTP:
                case HTTPS:
                    return new FlushedInputStream(imageStream);
                default:
                    return imageStream;
            }
        }
    }
}
