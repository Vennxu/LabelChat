package com.ekuater.httpfileloader;

import com.ekuater.httpfileloader.listener.FileLoadingListener;
import com.ekuater.httpfileloader.listener.FileLoadingProgressListener;

import java.util.concurrent.locks.ReentrantLock;

final class FileLoadingInfo {

    final String uri;
    final FileLoadOptions options;
    final FileLoadingListener listener;
    final FileLoadingProgressListener progressListener;
    final ReentrantLock loadFromUriLock;

    public FileLoadingInfo(String uri, FileLoadOptions options, FileLoadingListener listener,
                           FileLoadingProgressListener progressListener,
                           ReentrantLock loadFromUriLock) {
        this.uri = uri;
        this.options = options;
        this.listener = listener;
        this.progressListener = progressListener;
        this.loadFromUriLock = loadFromUriLock;
    }
}
