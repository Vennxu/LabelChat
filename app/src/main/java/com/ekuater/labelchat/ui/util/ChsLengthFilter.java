package com.ekuater.labelchat.ui.util;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * 汉字长度为1，字母为.05长度限制 filter
 *
 * @author LinYong
 */
public class ChsLengthFilter implements InputFilter {

    private final int mMax;

    public ChsLengthFilter(int max) {
        mMax = max * 2;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        if (source instanceof String) {
            int destLength = getChsStringLength(dest.toString());
            CharSequence destSub = dest.subSequence(dstart, dend);
            int destSubLength = getChsStringLength(destSub.toString());
            int keep = mMax - destLength + destSubLength;
            int sourceLength = getChsStringLength(source.subSequence(start, end).toString());

            if (keep <= 0) {
                return ""; // skip source input
            } else if (keep >= sourceLength) {
                return null; // keep original
            } else {
                return getMatchSubSequence(source, start, end, keep);
            }
        } else {
            return "";
        }
    }

    private CharSequence getMatchSubSequence(CharSequence source, int start,
                                             int end, int keep) {
        CharSequence subSequence = source.subSequence(start, start + 1);
        int length = getChsStringLength(subSequence.toString());

        if (length < keep) {
            int left = start;
            int right = end - 1;
            int subEnd = start + 1;

            while (left <= right) {
                int middle = (left + right) / 2;
                int tmpSubEnd = middle + 1;

                length = getChsStringLength(source.subSequence(
                        start, tmpSubEnd).toString());

                if (length < keep) {
                    left = middle + 1;
                } else if (length == keep) {
                    subEnd = tmpSubEnd;
                    break;
                } else {
                    right = middle - 1;
                }
            }

            return source.subSequence(start, subEnd);
        } else if (length == keep) {
            return subSequence;
        } else {
            return "";
        }
    }

    private int getChsStringLength(String chsString) {
        return MiscUtils.getChsStringLength(chsString);
    }
}
