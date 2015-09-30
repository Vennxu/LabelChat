package com.ekuater.labelchat.delegate;

import android.content.Context;

import com.ekuater.labelchat.command.BaseCommand;

/**
 * @author LinYong
 */
/*package*/abstract class BaseManager {

    public interface IListener {
        public void onCoreServiceConnected();

        public void onCoreServiceDied();
    }

    protected final CoreServiceDelegate mCoreService;
    protected final GuestMode mGuestMode;

    public BaseManager(Context context) {
        mCoreService = CoreServiceDelegate.getInstance(context);
        mGuestMode = GuestMode.getInstance(context);
    }

    /**
     * Now CoreService available or not
     *
     * @return CoreService now available or not
     */
    public boolean available() {
        return mCoreService.available();
    }

    public boolean isInGuestMode() {
        return mGuestMode.isInGuestMode();
    }

    public void executeCommand(BaseCommand command, ICommandResponseHandler handler) {
        mCoreService.executeCommand(command, handler);
    }

    public String getSession() {
        return mCoreService.accountGetSession();
    }

    public String getUserId() {
        return mCoreService.accountGetUserId();
    }

    public String getLabelCode() {
        return mCoreService.accountGetLabelCode();
    }
}
