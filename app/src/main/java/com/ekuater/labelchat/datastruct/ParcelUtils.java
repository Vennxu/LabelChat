package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Leo on 2015/4/15.
 *
 * @author LinYong
 */
public final class ParcelUtils {

    public static <T> T createParcelType(Parcel source, Parcelable.Creator<T> c) {
        if (source.readInt() != 0) {
            return c.createFromParcel(source);
        } else {
            return null;
        }
    }

    public static <T extends Parcelable> void writeParcelType(
            Parcel dest, T val, int parcelableFlags) {
        if (val != null) {
            dest.writeInt(1);
            val.writeToParcel(dest, parcelableFlags);
        } else {
            dest.writeInt(0);
        }
    }
}
