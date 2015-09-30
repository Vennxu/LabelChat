package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Leo on 2015/4/21.
 *
 * @author LinYong
 */
public enum DynamicScenario {

    GLOBAL {
        public DynamicConfig getConfig(Context context, Bundle args) {
            return new GlobalConfig(context, args);
        }
    },
    RELATED {
        @Override
        public DynamicConfig getConfig(Context context, Bundle args) {
            return new RelatedConfig(context, args);
        }
    },
    MY_OWN {
        @Override
        public DynamicConfig getConfig(Context context, Bundle args) {
            return new MyOwnConfig(context, args);
        }
    },
    USER_OWN {
        @Override
        public DynamicConfig getConfig(Context context, Bundle args) {
            return new UserOwnConfig(context, args);
        }
    };

    public static DynamicScenario fromInt(int type) {
        final DynamicScenario[] values = values();
        final int count = values.length;

        if (0 <= type && type < count) {
            return values[type];
        } else {
            return null;
        }
    }

    public int toInt() {
        return ordinal();
    }

    public abstract DynamicConfig getConfig(Context context, Bundle args);
}
