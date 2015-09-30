package com.ekuater.labelchat.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.InterestTypeProperty;
import com.ekuater.labelchat.ui.util.CompatUtils;

/**
 * Created by Administrator on 2015/5/18.
 *
 * @author LinYong
 */
public class InterestUtils {

    private static final SparseArrayCompat<InterestTypeProperty> TYPE_PROPERTY_MAP
            = new SparseArrayCompat<>();

    static {
        addTypeProperty(InterestType.MOVIE, R.drawable.label_movie,
                R.color.movie_bg, R.color.movie_name);
        addTypeProperty(InterestType.MUSIC, R.drawable.label_music,
                R.color.music_bg, R.color.music_name);
        addTypeProperty(InterestType.BOOK, R.drawable.label_book,
                R.color.book_bg, R.color.book_name);
        addTypeProperty(InterestType.SPORT, R.drawable.label_sport,
                R.color.sport_bg, R.color.sport_name);
        addTypeProperty(InterestType.FOOD, R.drawable.label_food,
                R.color.food_bg, R.color.food_name);
    }

    private static void addTypeProperty(int typeId, InterestTypeProperty property) {
        TYPE_PROPERTY_MAP.put(typeId, property);
    }

    private static void addTypeProperty(int typeId, int typeIconResId,
                                        int itemColorResId, int itemBgResId) {
        addTypeProperty(typeId, new InterestTypeProperty(typeId, typeIconResId,
                itemColorResId, itemBgResId));
    }

    @Nullable
    public static InterestTypeProperty getTypePropertyNonDefault(int typeId) {
        return TYPE_PROPERTY_MAP.get(typeId);
    }

    public static InterestTypeProperty getTypeProperty(int typeId) {
        InterestTypeProperty property = getTypePropertyNonDefault(typeId);
        if (property == null) {
            property = TYPE_PROPERTY_MAP.get(InterestType.MOVIE);
        }
        return property;
    }

    public static void setInterestColor(Context context, TextView textView, int typeId) {
        setInterestColor(context, textView, getTypeProperty(typeId));
    }

    public static void setInterestColor(Context context, TextView textView,
                                        InterestTypeProperty property) {
        Resources res = context.getResources();
        GradientDrawable interestDrawable = (GradientDrawable)
                res.getDrawable(R.drawable.interest_bg);

        if (interestDrawable != null) {
            interestDrawable.setColor(res.getColor(property.getItemBgResId()));
            CompatUtils.setBackground(textView, interestDrawable);
        } else {
            textView.setBackgroundColor(property.getItemBgResId());
        }
        textView.setTextColor(res.getColor(property.getItemColorResId()));
    }

    public static int getInterestTypeIcon(int typeId) {
        return getTypeProperty(typeId).getTypeIconResId();
    }

    public static boolean isInterestTypeSupported(int typeId) {
        return getTypePropertyNonDefault(typeId) != null;
    }
}
