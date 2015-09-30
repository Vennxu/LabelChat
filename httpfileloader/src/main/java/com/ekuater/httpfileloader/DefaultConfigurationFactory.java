package com.ekuater.httpfileloader;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ekuater.httpfileloader.disc.DiskCache;
import com.ekuater.httpfileloader.disc.impl.UnlimitedDiskCache;
import com.ekuater.httpfileloader.disc.impl.ext.LruDiskCache;
import com.ekuater.httpfileloader.disc.naming.FileNameGenerator;
import com.ekuater.httpfileloader.disc.naming.HashCodeFileNameGenerator;
import com.ekuater.httpfileloader.assist.QueueProcessingType;
import com.ekuater.httpfileloader.assist.deque.LIFOLinkedBlockingDeque;
import com.ekuater.httpfileloader.download.BaseFileDownloader;
import com.ekuater.httpfileloader.download.FileDownloader;
import com.ekuater.httpfileloader.utils.L;
import com.ekuater.httpfileloader.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory for providing of default options for {@linkplain FileLoaderConfiguration configuration}
 */
public class DefaultConfigurationFactory {

    /**
     * Creates default implementation of task executor
     */
    public static Executor createExecutor(int threadPoolSize, int threadPriority,
                                          QueueProcessingType tasksProcessingType) {
        boolean lifo = tasksProcessingType == QueueProcessingType.LIFO;
        BlockingQueue<Runnable> taskQueue =
                lifo ? new LIFOLinkedBlockingDeque<Runnable>() : new LinkedBlockingQueue<Runnable>();
        return new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, taskQueue,
                createThreadFactory(threadPriority, "uil-pool-"));
    }

    /**
     * Creates default implementation of task distributor
     */
    public static Executor createTaskDistributor() {
        return Executors.newCachedThreadPool(createThreadFactory(Thread.NORM_PRIORITY, "uil-pool-d-"));
    }

    /**
     * Creates {@linkplain HashCodeFileNameGenerator default implementation} of FileNameGenerator
     */
    public static FileNameGenerator createFileNameGenerator() {
        return new HashCodeFileNameGenerator();
    }

    /**
     * Creates default implementation of {@link DiskCache} depends on incoming parameters
     */
    public static DiskCache createDiskCache(Context context, FileNameGenerator diskCacheFileNameGenerator,
                                            long diskCacheSize, int diskCacheFileCount) {
        File reserveCacheDir = createReserveDiskCacheDir(context);
        if (diskCacheSize > 0 || diskCacheFileCount > 0) {
            File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
            try {
                return new LruDiskCache(individualCacheDir, reserveCacheDir, diskCacheFileNameGenerator, diskCacheSize,
                        diskCacheFileCount);
            } catch (IOException e) {
                L.e(e);
                // continue and create unlimited cache
            }
        }
        File cacheDir = StorageUtils.getCacheDirectory(context);
        return new UnlimitedDiskCache(cacheDir, reserveCacheDir, diskCacheFileNameGenerator);
    }

    /**
     * Creates reserve disk cache folder which will be used if primary disk cache folder becomes unavailable
     */
    private static File createReserveDiskCacheDir(Context context) {
        File cacheDir = StorageUtils.getCacheDirectory(context, false);
        File individualDir = new File(cacheDir, "uil-images");
        if (individualDir.exists() || individualDir.mkdir()) {
            cacheDir = individualDir;
        }
        return cacheDir;
    }

    /**
     * Creates default implementation of {@link com.ekuater.httpfileloader.download.FileDownloader} - {@link com.ekuater.httpfileloader.download.BaseFileDownloader}
     */
    public static FileDownloader createImageDownloader(Context context) {
        return new BaseFileDownloader(context);
    }

    /**
     * Creates default implementation of {@linkplain ThreadFactory thread factory} for task executor
     */
    private static ThreadFactory createThreadFactory(int threadPriority, String threadNamePrefix) {
        return new DefaultThreadFactory(threadPriority, threadNamePrefix);
    }

    private static class DefaultThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final int threadPriority;

        DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
            this.threadPriority = threadPriority;
            group = Thread.currentThread().getThreadGroup();
            namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            t.setPriority(threadPriority);
            return t;
        }
    }
}
