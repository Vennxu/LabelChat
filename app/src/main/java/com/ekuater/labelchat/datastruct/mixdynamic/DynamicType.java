package com.ekuater.labelchat.datastruct.mixdynamic;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public enum DynamicType {

    TXT,
    AUDIO,
    CONFIDE,
    BANKNOTE,
    ONLINEAUDIO;

    public static DynamicType toType(int type) {
        return values()[type];
    }

    public int getType() {
        return ordinal();
    }
}
