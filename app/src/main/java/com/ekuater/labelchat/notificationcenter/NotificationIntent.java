package com.ekuater.labelchat.notificationcenter;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * @author LinYong
 */
/*package*/ final class NotificationIntent {

    public static final int INTENT_TYPE_ACTIVITY = 1;

    private static final String EXTRA_INTENT = "extra_intent";
    private static final String EXTRA_INTENT_TYPE = "extra_intent_type";

    public static PendingIntent getNotificationIntent(
            Context context, Intent intent, int intentType, int flags) {
        Intent broadcastIntent = new Intent(context, NotificationIntentReceiver.class);
        broadcastIntent.putExtra(EXTRA_INTENT, intent);
        broadcastIntent.putExtra(EXTRA_INTENT_TYPE, intentType);
        return PendingIntent.getBroadcast(context, 0, broadcastIntent, flags);
    }

    public static PendingIntent getNotificationIntent(
            Context context, Intent intent, int intentType) {
        return getNotificationIntent(context, intent, intentType,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getActivityNotificationIntent(
            Context context, Intent intent) {
        return getNotificationIntent(context, intent, INTENT_TYPE_ACTIVITY);
    }

    public static void processReceiveIntent(Context context, Intent broadcastIntent) {
        Intent intent = broadcastIntent.getParcelableExtra(EXTRA_INTENT);
        int intentType = broadcastIntent.getIntExtra(EXTRA_INTENT_TYPE, 0);

        if (intent != null) {
            Intent serviceIntent = new Intent(context, NotificationIntentService.class);
            serviceIntent.putExtra(EXTRA_INTENT, intent);
            serviceIntent.putExtra(EXTRA_INTENT_TYPE, intentType);
            context.startService(serviceIntent);
        }
    }

    public static void processServiceIntent(Context context, Intent serviceIntent) {
        Intent intent = serviceIntent.getParcelableExtra(EXTRA_INTENT);
        int intentType = serviceIntent.getIntExtra(EXTRA_INTENT_TYPE, 0);

        switch (intentType) {
            case INTENT_TYPE_ACTIVITY:
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                break;
            default:
                break;
        }
    }
}
