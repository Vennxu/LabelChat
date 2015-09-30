package com.ekuater.labelchat;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.ekuater.labelchat.util.L;

/**
 * Created by Leo on 2015/2/11.
 *
 * @author LinYong
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = CrashHandler.class.getSimpleName();

    private static CrashHandler ourInstance = new CrashHandler();

    public static CrashHandler getInstance() {
        return ourInstance;
    }

    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        L.e(TAG, ex);

        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // sleep a while
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                L.e(TAG, "Error : ", e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            L.w(TAG, "handleException(), null Throwable");
            return true;
        }

        String msg = ex.getLocalizedMessage();
        if (msg == null) {
            return false;
        }

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, R.string.crash_exit,
                        Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();

        return true;
    }
}
