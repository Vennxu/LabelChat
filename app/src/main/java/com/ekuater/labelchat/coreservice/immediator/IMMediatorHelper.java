package com.ekuater.labelchat.coreservice.immediator;

import android.content.Context;

import com.ekuater.labelchat.coreservice.ICoreServiceCallback;

/**
 * @author Linyong
 */
public final class IMMediatorHelper {

    public static BaseIMMediator newIMMediator(Context context, ICoreServiceCallback callback) {
        return new RongIMMediator(context, callback);
    }
}
