package com.ekuater.httpfileloader;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.ekuater.httpfileloader.assist.FailReason;
import com.ekuater.httpfileloader.assist.FlushedInputStream;
import com.ekuater.httpfileloader.listener.FileLoadingListener;
import com.ekuater.httpfileloader.listener.FileLoadingProgressListener;
import com.ekuater.httpfileloader.listener.SimpleFileLoadingListener;
import com.ekuater.httpfileloader.disc.DiskCache;
import com.ekuater.httpfileloader.utils.L;

import java.io.File;

@SuppressWarnings("UnusedDeclaration")
public class FileLoader {

    public static final String TAG = FileLoader.class.getSimpleName();

    static final String LOG_INIT_CONFIG = "Initialize FileLoader with configuration";
    static final String LOG_DESTROY = "Destroy FileLoader";

    private static final String WARNING_RE_INIT_CONFIG = "Try to initialize FileLoader which had already been initialized before. "
            + "To re-init FileLoader with new configuration call FileLoader.destroy() at first.";
    private static final String ERROR_NOT_INIT = "FileLoader must be init with configuration before using";
    private static final String ERROR_INIT_CONFIG_WITH_NULL = "FileLoader configuration can not be initialized with null";

    private FileLoaderConfiguration configuration;
    private FileLoaderEngine engine;

    private FileLoadingListener defaultListener = new SimpleFileLoadingListener();

    private volatile static FileLoader instance;

    /**
     * Returns singleton class instance
     */
    public static FileLoader getInstance() {
        if (instance == null) {
            synchronized (FileLoader.class) {
                if (instance == null) {
                    instance = new FileLoader();
                }
            }
        }
        return instance;
    }

    protected FileLoader() {
    }

    /**
     * Initializes instance with configuration.
     */
    public synchronized void init(FileLoaderConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
        }
        if (this.configuration == null) {
            L.d(LOG_INIT_CONFIG);
            engine = new FileLoaderEngine(configuration);
            this.configuration = configuration;
        } else {
            L.w(WARNING_RE_INIT_CONFIG);
        }
    }

    public boolean isInited() {
        return configuration != null;
    }

    public void loadFile(String uri, FileLoadingListener listener,
                         FileLoadingProgressListener progressListener) {
        loadFile(uri, null, listener, progressListener);
    }

    public void loadFile(String uri, FileLoadingListener listener) {
        loadFile(uri, null, listener, null);
    }

    public void loadFile(String uri, FileLoadOptions options, FileLoadingListener listener,
                         FileLoadingProgressListener progressListener) {
        checkConfiguration();
        if (listener == null) {
            listener = defaultListener;
        }
        if (options == null) {
            options = configuration.defaultFileLoadOptions;
        }

        if (TextUtils.isEmpty(uri)) {
            listener.onLoadingStarted(uri);
            listener.onLoadingComplete(uri, null);
            return;
        }

        listener.onLoadingStarted(uri);

        FileLoadingInfo fileLoadingInfo = new FileLoadingInfo(uri, options, listener,
                progressListener, engine.getLockForUri(uri));
        LoadFileTask displayTask = new LoadFileTask(engine, fileLoadingInfo,
                defineHandler(options));
        if (options.isSyncLoading()) {
            displayTask.run();
        } else {
            engine.submit(displayTask);
        }
    }

    public File loadFileSync(String uri) {
        return loadFileSync(uri, null);
    }

    public File loadFileSync(String uri, FileLoadOptions options) {
        if (options == null) {
            options = configuration.defaultFileLoadOptions;
        }
        options = new FileLoadOptions.Builder().cloneFrom(options).syncLoading(true).build();
        SyncFileLoadingListener listener = new SyncFileLoadingListener();
        loadFile(uri, options, listener, null);
        return listener.getLoadedFile();
    }

    /**
     * Checks if loader's configuration was initialized
     *
     * @throws IllegalStateException if configuration wasn't initialized
     */
    private void checkConfiguration() {
        if (configuration == null) {
            throw new IllegalStateException(ERROR_NOT_INIT);
        }
    }

    /**
     * Sets a default loading listener for all display and loading tasks.
     */
    public void setDefaultLoadingListener(FileLoadingListener listener) {
        defaultListener = listener == null ? new SimpleFileLoadingListener() : listener;
    }

    /**
     * Returns disk cache
     *
     * @throws IllegalStateException if {@link #init(FileLoaderConfiguration)} method wasn't called before
     */
    public DiskCache getDiskCache() {
        checkConfiguration();
        return configuration.diskCache;
    }

    /**
     * Clears disk cache.
     *
     * @throws IllegalStateException if {@link #init(FileLoaderConfiguration)} method wasn't called before
     */
    public void clearDiskCache() {
        checkConfiguration();
        configuration.diskCache.clear();
    }

    /**
     * Denies or allows load to download from the network.<br />
     * <br />
     * If downloads are denied and if image isn't cached then
     * {@link com.ekuater.httpfileloader.listener.FileLoadingListener#onLoadingFailed(String, FailReason)} callback will be fired with
     * {@link FailReason.FailType#NETWORK_DENIED}
     *
     * @param denyNetworkDownloads pass <b>true</b> - to deny engine to download images from the network; <b>false</b> -
     *                             to allow engine to download images from network.
     */
    public void denyNetworkDownloads(boolean denyNetworkDownloads) {
        engine.denyNetworkDownloads(denyNetworkDownloads);
    }

    /**
     * Sets option whether loader will use {@link FlushedInputStream} for network downloads to handle <a
     * href="http://code.google.com/p/android/issues/detail?id=6066">this known problem</a> or not.
     *
     * @param handleSlowNetwork pass <b>true</b> - to use {@link FlushedInputStream} for network downloads; <b>false</b>
     *                          - otherwise.
     */
    public void handleSlowNetwork(boolean handleSlowNetwork) {
        engine.handleSlowNetwork(handleSlowNetwork);
    }

    public void pause() {
        engine.pause();
    }

    public void resume() {
        engine.resume();
    }

    public void stop() {
        engine.stop();
    }

    public void destroy() {
        if (configuration != null) L.d(LOG_DESTROY);
        stop();
        configuration.diskCache.close();
        engine = null;
        configuration = null;
    }

    private static Handler defineHandler(FileLoadOptions options) {
        Handler handler = options.getHandler();
        if (options.isSyncLoading()) {
            handler = null;
        } else if (handler == null && Looper.myLooper() == Looper.getMainLooper()) {
            handler = new Handler();
        }
        return handler;
    }

    /**
     * Listener which is designed for synchronous loading.
     */
    private static class SyncFileLoadingListener extends SimpleFileLoadingListener {

        private File loadedFile;

        @Override
        public void onLoadingComplete(String fileUri, File file) {
            this.loadedFile = file;
        }

        public File getLoadedFile() {
            return loadedFile;
        }
    }
}
