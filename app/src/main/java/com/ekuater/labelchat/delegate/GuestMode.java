package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;

import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserLabel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LinYong
 */
/*package*/ final class GuestMode {

    private static GuestMode sInstance;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GuestMode(context.getApplicationContext());
        }
    }

    public static GuestMode getInstance(Context context) {
        if (sInstance == null) {
            initInstance(context);
        }

        return sInstance;
    }

    private final CoreServiceDelegate mCoreService;
    private final ICoreServiceNotifier mNotifier = new AbstractCoreServiceNotifier() {
        @Override
        public void onCoreServiceConnected() {
            setInGuestMode(!mCoreService.accountIsLogin());
        }

        @Override
        public void onCoreServiceDied() {
        }

        @Override
        public void onAccountLogin(int result) {
            setInGuestMode(result != ConstantCode.ACCOUNT_OPERATION_SUCCESS);
        }

        @Override
        public void onAccountLogout(int result) {
            setInGuestMode(true);
        }
    };

    private boolean mInGuestMode;
    private Session mSession;

    private GuestMode(Context context) {
        mCoreService = CoreServiceDelegate.getInstance(context);
        mInGuestMode = false/*!mCoreService.accountIsLogin()*/;
        mSession = /* mInGuestMode ? new Session() :*/ null;
        mCoreService.registerNotifier(mNotifier);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mCoreService.unregisterNotifier(mNotifier);
    }

    private synchronized void setInGuestMode(boolean inGuestMode) {
        /*if (mInGuestMode != inGuestMode) {
            mInGuestMode = inGuestMode;
            mSession = mInGuestMode ? new Session() : null;
        }*/
    }

    public boolean isInGuestMode() {
        return mInGuestMode;
    }

    public String getSession() {
        return null;
    }

    public String getUserId() {
        return null;
    }

    public String getLabelCode() {
        return null;
    }

    public void addUserLabels(BaseLabel[] labels) {
        if (mSession != null) {
            mSession.addUserLabels(labels);
        }
    }

    public void deleteUserLabels(UserLabel[] labels) {
        if (mSession != null) {
            mSession.deleteUserLabels(labels);
        }
    }

    public UserLabel[] getUserLabels() {
        return (mSession != null) ? mSession.getUserLabels() : null;
    }

    private static class Session {

        private final Map<String, UserLabel> mUserLabelMap;

        public Session() {
            mUserLabelMap = new HashMap<>();
        }

        public synchronized void addUserLabels(BaseLabel[] labels) {
            if (labels != null && labels.length > 0) {
                for (BaseLabel label : labels) {
                    String name = label.getName();
                    if (!TextUtils.isEmpty(name) && !mUserLabelMap.containsKey(name)) {
                        long time = System.currentTimeMillis();
                        UserLabel userLabel = new UserLabel(name,
                                String.valueOf(time), time, 1);
                        mUserLabelMap.put(name, userLabel);
                    }
                }
            }
        }

        public synchronized void deleteUserLabels(UserLabel[] labels) {
            if (labels != null && labels.length > 0) {
                for (UserLabel label : labels) {
                    mUserLabelMap.remove(label.getName());
                }
            }
        }

        public synchronized UserLabel[] getUserLabels() {
            final int count = mUserLabelMap.size();
            return (count > 0) ? mUserLabelMap.values().toArray(new UserLabel[count]) : null;
        }
    }
}
