package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.content.Context;
import android.os.Bundle;

import com.ekuater.labelchat.delegate.MixDynamicManager;

/**
 * Created by Leo on 2015/4/21.
 *
 * @author LinYong
 */
@SuppressWarnings("UnusedParameters")
abstract class DynamicConfig {

    protected final MixDynamicManager dynamicManager;

    public DynamicConfig(Context context, Bundle args) {
        this.dynamicManager = MixDynamicManager.getInstance(context);
    }

    public boolean monitorSentDynamic() {
        return false;
    }

    public boolean loadComments() {
        return false;
    }

    public boolean needDeleteDynamic() {
        return false;
    }

    public boolean needShowTitle() {
        return false;
    }


    public String getTitle() {
        return null;
    }

    public String getNoDataTip() {
        return null;
    }

    public abstract void queryMixDynamic(int requestTime, QueryListener listener);
}
