package com.ekuater.labelchat.coreservice.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LinYong
 */
public final class TaskExecutor {

    private static TaskExecutor sInstance;

    public static TaskExecutor getInstance() {
        if (sInstance == null) {
            initInstance();
        }
        return sInstance;
    }

    private static synchronized void initInstance() {
        if (sInstance == null) {
            sInstance = new TaskExecutor();
        }
    }

    private final ExecutorService mExecutorService;

    private TaskExecutor() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public void execute(Runnable task) {
        mExecutorService.execute(task);
    }
}
