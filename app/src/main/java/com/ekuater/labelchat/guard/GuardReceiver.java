package com.ekuater.labelchat.guard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ekuater.labelchat.settings.SettingHelper;

/**
 * Created by Leo on 2015/2/11.
 *
 * @author LinYong
 */
public class GuardReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(GuardConst.ACTION_SERVICE_DEAD)) {
            if (!SettingHelper.getInstance(context).isManualExitApp()) {
                onServiceDead(context, intent);
            }
        }
    }

    private void onServiceDead(Context context, Intent intent) {
        final String deadService = intent.getStringExtra(GuardConst.EXTRA_SERVICE);

        if (GuardConst.SERVICE_CORE.equals(deadService)) {
            onCoreServiceDead(context);
        } else if (GuardConst.SERVICE_NOTIFICATION.equals(deadService)) {
            onNotificationServiceDead(context);
        }
    }

    private void onCoreServiceDead(Context context) {
        // TODO
    }

    private void onNotificationServiceDead(Context context) {
        // TODO
    }
}
