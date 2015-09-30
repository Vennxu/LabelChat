package com.ekuater.labelchat.ui.fragment.confide;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.Stranger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/4/8.
 */
public class ConfideUtils {

    public static final int REFRESH = 0;
    public static final int LOADING = 1;

    public static final int CONFIDE_SHOW_CODE = 10;

    public static final String CONFIDE = "confide";
    public static final String CONFIDE_INDEX = "confide_index";
    public static final String IS_MY_CONFIDE = "is_my_confide";
    public static final String IS_SHOW_SOFT = "is_show_soft";


    public static void startAnimation(ImageView imageLoading) {
        imageLoading.setVisibility(View.VISIBLE);
        Drawable drawable = imageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    public static void stopAnimation(ImageView imageLoading) {
        imageLoading.setVisibility(View.GONE);
        Drawable drawable = imageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
    }

    public static void startAnimation(LinearLayout linearLoading, ImageView loading) {
        linearLoading.setVisibility(View.VISIBLE);
        Drawable drawable = loading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    public static void stopAnimation(LinearLayout linearLoading, ImageView loading) {
        linearLoading.setVisibility(View.GONE);
        Drawable drawable = loading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
    }

    public static String batchPraiseToJson(ArrayList<String> list) {
        JSONArray object = new JSONArray();
        for (int i = 0; i < list.size(); ++i) {
            object.put(list.get(i));
        }
        return object.toString();
    }
}
