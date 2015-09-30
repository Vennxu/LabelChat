package com.ekuater.httpfileloader;

import com.ekuater.httpfileloader.assist.FailReason;
import com.ekuater.httpfileloader.assist.FlushedInputStream;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link FileLoader} engine which responsible for {@linkplain LoadFileTask display task} execution.
 */
class FileLoaderEngine {

    final FileLoaderConfiguration configuration;

    private Executor taskExecutor;
    private Executor taskExecutorForCachedFiles;
    private Executor taskDistributor;

    private final Map<String, ReentrantLock> uriLocks = new WeakHashMap<>();
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean networkDenied = new AtomicBoolean(false);
    private final AtomicBoolean slowNetwork = new AtomicBoolean(false);

    private final Object pauseLock = new Object();

    FileLoaderEngine(FileLoaderConfiguration configuration) {
        this.configuration = configuration;

        taskExecutor = configuration.taskExecutor;
        taskExecutorForCachedFiles = configuration.taskExecutorForCachedFiles;

        taskDistributor = DefaultConfigurationFactory.createTaskDistributor();
    }

    /**
     * Submits task to execution pool
     */
    void submit(final LoadFileTask task) {
        taskDistributor.execute(new Runnable() {
            @Override
            public void run() {
                File file = configuration.diskCache.get(task.getLoadingUri());
                boolean isImageCachedOnDisk = file != null && file.exists();
                initExecutorsIfNeed();
                if (isImageCachedOnDisk) {
                    taskExecutorForCachedFiles.execute(task);
                } else {
                    taskExecutor.execute(task);
                }
            }
        });
    }

    private void initExecutorsIfNeed() {
        if (!configuration.customExecutor && ((ExecutorService) taskExecutor).isShutdown()) {
            taskExecutor = createTaskExecutor();
        }
        if (!configuration.customExecutorForCachedImages && ((ExecutorService) taskExecutorForCachedFiles)
                .isShutdown()) {
            taskExecutorForCachedFiles = createTaskExecutor();
        }
    }

    private Executor createTaskExecutor() {
        return DefaultConfigurationFactory
                .createExecutor(configuration.threadPoolSize, configuration.threadPriority,
                        configuration.tasksProcessingType);
    }

    /**
     * Denies or allows engine to download images from the network.<br /> <br /> If downloads are denied and if image
     * isn't cached then {@link com.ekuater.httpfileloader.listener.FileLoadingListener#onLoadingFailed(String, FailReason)} callback will be fired
     * with {@link FailReason.FailType#NETWORK_DENIED}
     *
     * @param denyNetworkDownloads pass <b>true</b> - to deny engine to download images from the network; <b>false</b> -
     *                             to allow engine to download images from network.
     */
    void denyNetworkDownloads(boolean denyNetworkDownloads) {
        networkDenied.set(denyNetworkDownloads);
    }

    /**
     * Sets option whether ImageLoader will use {@link FlushedInputStream} for network downloads to handle <a
     * href="http://code.google.com/p/android/issues/detail?id=6066">this known problem</a> or not.
     *
     * @param handleSlowNetwork pass <b>true</b> - to use {@link FlushedInputStream} for network downloads; <b>false</b>
     *                          - otherwise.
     */
    void handleSlowNetwork(boolean handleSlowNetwork) {
        slowNetwork.set(handleSlowNetwork);
    }

    /**
     * Pauses engine. All new "load&display" tasks won't be executed until ImageLoader is {@link #resume() resumed}.<br
     * /> Already running tasks are not paused.
     */
    void pause() {
        paused.set(true);
    }

    /**
     * Resumes engine work. Paused "load&display" tasks will continue its work.
     */
    void resume() {
        paused.set(false);
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    /**
     * Stops engine, cancels all running and scheduled display image tasks. Clears internal data.
     * <br />
     * <b>NOTE:</b> This method doesn't shutdown
     * {@linkplain FileLoaderConfiguration.Builder#taskExecutor(java.util.concurrent.Executor)
     * custom task executors} if you set them.
     */
    void stop() {
        if (!configuration.customExecutor) {
            ((ExecutorService) taskExecutor).shutdownNow();
        }
        if (!configuration.customExecutorForCachedImages) {
            ((ExecutorService) taskExecutorForCachedFiles).shutdownNow();
        }

        uriLocks.clear();
    }

    void fireCallback(Runnable r) {
        taskDistributor.execute(r);
    }

    ReentrantLock getLockForUri(String uri) {
        ReentrantLock lock = uriLocks.get(uri);
        if (lock == null) {
            lock = new ReentrantLock();
            uriLocks.put(uri, lock);
        }
        return lock;
    }

    AtomicBoolean getPause() {
        return paused;
    }

    Object getPauseLock() {
        return pauseLock;
    }

    boolean isNetworkDenied() {
        return networkDenied.get();
    }

    boolean isSlowNetwork() {
        return slowNetwork.get();
    }
}
