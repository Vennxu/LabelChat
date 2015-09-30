package com.ekuater.labelchat.delegate;

import android.content.Context;

import com.ekuater.labelchat.datastruct.Stranger;

/**
 * Created by Leo on 2015/1/31.
 *
 * @author LinYong
 */
public class StrangerManager extends BaseManager {

    private static StrangerManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new StrangerManager(context.getApplicationContext());
        }
    }

    public static StrangerManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private StrangerManager(Context context) {
        super(context);
    }

    public void addStranger(Stranger stranger) {
        mCoreService.addStranger(stranger);
    }

    public Stranger getStranger(String userId) {
        return mCoreService.getStranger(userId);
    }

    public void deleteStranger(String userId) {
        mCoreService.deleteStranger(userId);
    }
}
