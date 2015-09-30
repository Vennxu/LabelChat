package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.content.Context;
import android.os.Bundle;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.delegate.MixDynamicManager;

/**
 * Created by Leo on 2015/4/21.
 *
 * @author LinYong
 */
class MyOwnConfig extends DynamicConfig {

    private final Context context;

    public MyOwnConfig(Context context, Bundle args) {
        super(context, args);
        this.context = context;
    }

    @Override
    public boolean needDeleteDynamic() {
        return true;
    }

    @Override
    public boolean needShowTitle() {
        return true;
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.my_story);
    }

    @Override
    public void queryMixDynamic(int requestTime, final QueryListener listener) {
        dynamicManager.queryMyOwnDynamic(
                String.valueOf(requestTime),
                new MixDynamicManager.DynamicObserver() {
                    @Override
                    public void onQueryResult(int result, DynamicWrapper[] wrappers) {
                        listener.onQueryResult(result, wrappers);
                    }
                });
    }
}
