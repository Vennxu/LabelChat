package com.ekuater.labelchat.banknote;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.util.ColorUtils;

import java.util.Random;

/**
 * Created by Leo on 2015/5/11.
 *
 * @author LinYong
 */
public final class BanknoteParser {

    private static final int FIELD_COUNT = 4;

    public static Banknote randomBanknote(Resources res) {
        TypedArray ar = res.obtainTypedArray(R.array.banknotes);
        String[] values = res.getStringArray(ar.getResourceId(
                new Random().nextInt(ar.length()), 0));
        ar.recycle();
        return parseBanknote(values);
    }

    public static Banknote[] getBanknotes(Resources res) {
        TypedArray ar = res.obtainTypedArray(R.array.banknotes);
        int length = ar.length();
        Banknote[] banknotes = new Banknote[length];

        for (int i = 0; i < length; ++i) {
            banknotes[i] = parseBanknote(res.getStringArray(ar.getResourceId(i, 0)));
        }
        ar.recycle();
        return banknotes;
    }

    private static Banknote parseBanknote(String[] values) {
        if (values == null || values.length != FIELD_COUNT) {
            throw new IllegalArgumentException();
        }

        Banknote banknote = new Banknote();
        banknote.setImage(values[0]);
        banknote.setFacePosition(parsePoint(values[1]));
        banknote.setFaceSize(parsePoint(values[2]));
        banknote.setBaseColor(ColorUtils.parseColor(values[3]));
        return banknote;
    }

    private static Point parsePoint(String value) {
        String[] numbers = value.split(",");
        if (numbers.length != 2) {
            throw new IllegalArgumentException();
        }
        return new Point(Integer.parseInt(numbers[0].trim()),
                Integer.parseInt(numbers[1].trim()));
    }
}
