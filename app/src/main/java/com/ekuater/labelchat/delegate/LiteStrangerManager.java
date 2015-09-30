package com.ekuater.labelchat.delegate;

import android.content.Context;

import com.ekuater.labelchat.datastruct.LiteStranger;

/**
 * Created by Leo on 2015/3/3.
 *
 * @author LinYong
 */
public class LiteStrangerManager extends BaseManager {

    private static LiteStrangerManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new LiteStrangerManager(context.getApplicationContext());
        }
    }

    public static LiteStrangerManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    public LiteStrangerManager(Context context) {
        super(context);
    }

    public void addLiteStranger(LiteStranger stranger) {
        mCoreService.addLiteStranger(stranger);
    }

    public LiteStranger getLiteStranger(String userId) {
        return mCoreService.getLiteStranger(userId);
    }

    public void deleteLiteStranger(String userId) {
        mCoreService.deleteLiteStranger(userId);
    }
}
