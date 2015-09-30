package com.ekuater.labelchat.notificationcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author LinYong
 */
public class NotificationIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationIntent.processReceiveIntent(context, intent);
    }
}
