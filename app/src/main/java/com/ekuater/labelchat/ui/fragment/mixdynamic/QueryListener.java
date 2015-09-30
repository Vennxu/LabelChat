package com.ekuater.labelchat.ui.fragment.mixdynamic;

import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;

/**
 * Created by Leo on 2015/4/21.
 *
 * @author LinYong
 */
interface QueryListener {

    public void onQueryResult(int result, DynamicWrapper[] wrappers);
}
