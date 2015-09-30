package com.ekuater.labelchat.ui.fragment.mood;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import com.ekuater.labelchat.R;

import java.util.Random;

/**
 * Created by Administrator on 2015/5/9.
 */
public class MoodUtils {

    public static final int MOOD_USER_LIST_CODE = 101;
    public static final int MOOD_SEND_RESULT = 102;
    public static final String USERIDS = "userids";
    public static final String USER_GROUP = "user_group";

    public static int[] getEmojiArray(Context context, int arryId){

        final TypedArray ar = context.getResources().obtainTypedArray(
                arryId);
        final int length = ar.length();
        final int[] array = new int[length];
        for (int i = 0; i < length; ++i) {
            array[i] = ar.getResourceId(i, 0);
        }
        return array;
    }

}
