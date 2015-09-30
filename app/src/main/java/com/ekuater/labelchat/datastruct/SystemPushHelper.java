package com.ekuater.labelchat.datastruct;

/**
 * Created by Leo on 2015/3/25.
 *
 * @author LinYong
 */
public final class SystemPushHelper {

    public static String getSystemPushFlag(SystemPush systemPush) {
        String flag = "";

        switch (systemPush.getType()) {
            case SystemPushType.TYPE_PRIVATE_LETTER: {
                LetterMessage message = LetterMessage.build(systemPush);
                flag = message.getStranger().getUserId();
                break;
            }
            default:
                break;
        }
        return flag;
    }
}
