
package com.ekuater.labelchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ekuater.labelchat.settings.SettingHelper;

/**
 * @author LinYong
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (!SettingHelper.getInstance(context).isManualExitApp()) {
                context.startService(new Intent(context, BootCompleteService.class));
            }
        }
    }
}
