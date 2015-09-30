package com.ekuater.labelchat.coreservice.pushmediator;

import android.content.Context;

import com.ekuater.labelchat.coreservice.ICoreServiceCallback;

/**
 * @author LinYong
 */
public final class PushMediatorHelper {

    public static BasePushMediator newPushMediator(Context context,
                                                   ICoreServiceCallback callback) {
        return new RongIMPushMediator(context, callback);
    }
}
