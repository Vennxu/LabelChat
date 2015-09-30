package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Leo on 2015/4/21.
 *
 * @author LinYong
 */
public class MixDynamicUIHelper {

    public static Fragment newGlobalFragment() {
        return newMixDynamicFragment(DynamicScenario.GLOBAL);
    }

    public static Fragment newRelatedFragment() {
        return newMixDynamicFragment(DynamicScenario.RELATED);
    }

    public static Fragment newMixDynamicFragment(DynamicScenario scenario) {
        Bundle args = new Bundle();
        MixDynamicAllFragment fragment = new MixDynamicAllFragment();

        args.putInt(MixDynamicArgs.ARGS_SCENARIO_TYPE, scenario.toInt());
        fragment.setArguments(args);
        return fragment;
    }
}
