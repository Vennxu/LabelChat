package com.ekuater.httpfileloader;

import android.os.Handler;

import com.ekuater.httpfileloader.assist.FailReason;
import com.ekuater.httpfileloader.assist.FailReason.FailType;
import com.ekuater.httpfileloader.download.FileDownloader;
import com.ekuater.httpfileloader.listener.FileLoadingListener;
import com.ekuater.httpfileloader.listener.FileLoadingProgressListener;
import com.ekuater.httpfileloader.utils.IoUtils;
import com.ekuater.httpfileloader.utils.L;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

final class LoadFileTask implements Runnable, IoUtils.CopyListener {

    private final FileLoaderEngine engine;
    private final FileLoadingInfo fileLoadingInfo;
    private final Handler handler;

    // Helper references
    private final FileLoaderConfiguration configuration;
    private final FileDownloader downloader;
    private final FileDownloader networkDeniedDownloader;
    private final FileDownloader slowNetworkDownloader;
    final FileLoadingProgressListener progressListener;
    private final boolean syncLoading;

    final String uri;
    final FileLoadOptions options;
    final FileLoadingListener listener;

    public LoadFileTask(FileLoaderEngine engine, FileLoadingInfo fileLoadingInfo, Handler handler) {
        this.engine = engine;
        this.fileLoadingInfo = fileLoadingInfo;
        this.handler = handler;

        configuration = engine.configuration;
        downloader = configuration.downloader;
        networkDeniedDownloader = configuration.networkDeniedDownloader;
        slowNetworkDownloader = configuration.slowNetworkDownloader;
        uri = fileLoadingInfo.uri;
        options = fileLoadingInfo.options;
        listener = fileLoadingInfo.listener;
        progressListener = fileLoadingInfo.progressListener;
        syncLoading = options.isSyncLoading();
    }

    @Override
    public void run() {
        if (waitIfPaused()) return;

        ReentrantLock loadFromUriLock = fileLoadingInfo.loadFromUriLock;

        loadFromUriLock.lock();
        File file;
        try {
            file = tryLoadFile();
            if (file == null) {
                return; // listener callback already was fired
            }

            checkTaskInterrupted();
        } catch (TaskCancelledException e) {
            fireCancelEvent();
            return;
        } finally {
            loadFromUriLock.unlock();
        }

        fireCompleteEvent(file);
    }

    /**
     * @return <b>true</b> - if task should be interrupted; <b>false</b> - otherwise
     */
    private boolean waitIfPaused() {
        AtomicBoolean pause = engine.getPause();
        if (pause.get()) {
            synchronized (engine.getPauseLock()) {
                if (pause.get()) {
                    try {
                        engine.getPauseLock().wait();
                    } catch (InterruptedException e) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private File tryLoadFile() throws TaskCancelledException {
        File file = null;
        try {
            file = configuration.diskCache.get(uri);

            if (file == null || !file.exists() || file.length() <= 0) {
                if (tryCacheFileOnDisk()) {
                    file = configuration.diskCache.get(uri);
                }
                if (file == null || !file.exists() || file.length() <= 0) {
                    fireFailEvent(FailType.DECODING_ERROR, null);
                }
            }
        } catch (IllegalStateException e) {
            fireFailEvent(FailType.NETWORK_DENIED, null);
        } catch (TaskCancelledException e) {
            throw e;
        } catch (OutOfMemoryError e) {
            L.e(e);
            fireFailEvent(FailType.OUT_OF_MEMORY, e);
        } catch (Throwable e) {
            L.e(e);
            fireFailEvent(FailType.UNKNOWN, e);
        }
        return file;
    }

    /**
     * @return <b>true</b> - if image was downloaded successfully; <b>false</b> - otherwise
     */
    private boolean tryCacheFileOnDisk() throws TaskCancelledException {
        boolean loaded;
        try {
            loaded = downloadFile();
        } catch (IOException e) {
            L.e(e);
            loaded = false;
        }
        return loaded;
    }

    private boolean downloadFile() throws IOException {
        InputStream is = getDownloader().getStream(uri, options.getExtraForDownloader());
        if (is == null) {
            return false;
        } else {
            try {
                return configuration.diskCache.save(uri, is, this);
            } finally {
                IoUtils.closeSilently(is);
            }
        }
    }

    @Override
    public boolean onBytesCopied(int current, int total) {
        return syncLoading || fireProgressEvent(current, total);
    }

    /**
     * @return <b>true</b> - if loading should be continued; <b>false</b> - if loading should be interrupted
     */
    private boolean fireProgressEvent(final int current, final int total) {
        if (isTaskInterrupted()) return false;
        if (progressListener != null) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    progressListener.onProgressUpdate(uri, current, total);
                }
            };
            runTask(r, false, handler, engine);
        }
        return true;
    }

    private void fireFailEvent(final FailType failType, final Throwable failCause) {
        if (syncLoading || isTaskInterrupted()) return;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.onLoadingFailed(uri, new FailReason(failType, failCause));
            }
        };
        runTask(r, false, handler, engine);
    }

    private void fireCancelEvent() {
        if (syncLoading || isTaskInterrupted()) return;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.onLoadingCancelled(uri);
            }
        };
        runTask(r, false, handler, engine);
    }

    private void fireCompleteEvent(final File file) {
        if (syncLoading || isTaskInterrupted()) return;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                listener.onLoadingComplete(uri, file);
            }
        };
        runTask(r, false, handler, engine);
    }

    private FileDownloader getDownloader() {
        FileDownloader d;
        if (engine.isNetworkDenied()) {
            d = networkDeniedDownloader;
        } else if (engine.isSlowNetwork()) {
            d = slowNetworkDownloader;
        } else {
            d = downloader;
        }
        return d;
    }

    /**
     * @throws TaskCancelledException if current task was interrupted
     */
    private void checkTaskInterrupted() throws TaskCancelledException {
        if (isTaskInterrupted()) {
            throw new TaskCancelledException();
        }
    }

    /**
     * @return <b>true</b> - if current task was interrupted; <b>false</b> - otherwise
     */
    private boolean isTaskInterrupted() {
        return Thread.interrupted();
    }

    String getLoadingUri() {
        return uri;
    }

    static void runTask(Runnable r, boolean sync, Handler handler, FileLoaderEngine engine) {
        if (sync) {
            r.run();
        } else if (handler == null) {
            engine.fireCallback(r);
        } else {
            handler.post(r);
        }
    }

    /**
     * Exceptions for case when task is cancelled (thread is interrupted, image view is reused for another task, view is
     * collected by GC).
     */
    class TaskCancelledException extends Exception {
    }
}
