package com.ekuater.labelchat.ui.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.delegate.MiscManager;

/**
 * @author LinYong
 */
public final class MiscUtils {

    public static void showAvatarThumb(AvatarManager avatarManager, String url,
                                       ImageView avatarImage) {
        showAvatarThumb(avatarManager, url, avatarImage, R.drawable.contact_single);
    }

    public static void showAvatarThumb(AvatarManager avatarManager, String url,
                                       ImageView avatarImage, int defaultResId) {
        avatarManager.displayAvatarThumb(url, avatarImage, defaultResId);
    }

    public static void showGroupAvatarThumb(AvatarManager avatarManager, String url,
                                            ImageView avatarImage, int defaultResId) {
        avatarManager.displayTmpGroupAvatar(url, avatarImage, defaultResId);
    }

    public static void showChatRoomAvatarThumb(AvatarManager avatarManager, String url,
                                               ImageView avatarImage, int defaultResId) {
        avatarManager.displayChatRoomAvatar(url, avatarImage, defaultResId);
    }

    public static void showCategoryAvatarThumb(AvatarManager avatarManager, String url,
                                               ImageView avatarImage, int defaultResId) {
        avatarManager.displayCategoryAvatar(url, avatarImage, defaultResId);
    }

    public static void showConfideAvatarThumb(AvatarManager avatarManager, String url,
                                              ImageView avatarImage, int defaultResId) {
        avatarManager.displayConfideAvatar(url, avatarImage, defaultResId);
    }

    public static void showLabelStoryImageThumb(AvatarManager avatarManager, String url,
                                                ImageView imageView, int defaultResId) {
        StoryImageThumbLoadListener listener = new StoryImageThumbLoadListener(url, imageView);
        Bitmap bitmap = avatarManager.getLabelStoryImageThumb(url, listener);
        if (bitmap != null) {
            listener.onLoadComplete(url, bitmap);
        } else {
            imageView.setImageResource(defaultResId);
        }
    }

    public static void showLabelStoryImage(AvatarManager avatarManager, String url,
                                           ImageView imageView, int defaultResId) {
        StoryImageLoadListener listener = new StoryImageLoadListener(url, imageView);
        Bitmap bitmap = avatarManager.getLabelStoryImage(url, listener);
        if (bitmap != null) {
            listener.onLoadComplete(url, bitmap);
        } else {
            imageView.setImageResource(defaultResId);
        }
    }

    public static void showLabelStoryCommentAvatarThumb(AvatarManager avatarManager, String url,
                                                        ImageView imageView, int defaultResId) {
        avatarManager.displayStoryImageThumb(url, imageView, defaultResId);
    }

    public static String getDistanceString(Context context, double distance) {
        String distanceString;
        if (distance < 1000) {
            distanceString = context.getString(R.string.distance_in_meter, (int) distance);
        } else {
            distanceString = context.getString(R.string.distance_in_kilometer,
                    (long) (distance / 1000));
        }
        return distanceString;
    }

    public static String getAgeString(Resources resources, int age) {
        if (age >= 0) {
            return resources.getString(R.string.age_format, age);
        } else {
            return null;
        }
    }

    public static String getHeightString(Resources resources, int height) {
        if (height > 0) {
            return resources.getString(R.string.height_format, height);
        } else {
            return null;
        }
    }

    public static String getConstellationString(Resources resources, int constellation) {
        final String[] constellations = resources.getStringArray(
                R.array.constellation_array);
        final int[] constellationValues = resources.getIntArray(
                R.array.constellation_value_array);

        for (int i = 0; i < constellationValues.length; ++i) {
            if (constellation == constellationValues[i]) {
                return constellations[i];
            }
        }

        return null;
    }

    public static String getGenderString(Resources resources, int gender) {
        String value;

        switch (gender) {
            case ConstantCode.USER_SEX_MALE:
                value = resources.getString(R.string.male);
                break;
            case ConstantCode.USER_SEX_FEMALE:
                value = resources.getString(R.string.female);
                break;
            default:
                value = null;
                break;
        }

        return value;
    }

    /**
     * 获取字符串长度，一个汉字长度为2，字母为1
     *
     * @param chsString 需要获取长度的字符串
     * @return 计算后的字符串长度
     */
    public static int getChsStringLength(String chsString) {
        if (!TextUtils.isEmpty(chsString)) {
            char[] chars = chsString.toCharArray();
            int length = 0;

            for (char ch : chars) {
                length += isChineseChar(ch) ? 2 : 1;
            }

            return length;
        } else {
            return 0;
        }
    }

    public static boolean isChineseChar(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
    }

    public static int dp2px(Context context, float dp) {
        return dp2px(context.getResources(), dp);
    }

    public static int dp2px(Resources res, float dp) {
        return dp2px(res.getDisplayMetrics(), dp);
    }

    public static int dp2px(DisplayMetrics metrics, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                metrics);
    }

    public static void complainDynamic(final Context context, String dynamicId) {
        MiscManager miscManager = MiscManager.getInstance(context);
        miscManager.complainDynamic(dynamicId, new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                onComplainResult(context, result);
            }
        });
    }

    public static void complainUser(final Context context, String complainUserId) {
        MiscManager miscManager = MiscManager.getInstance(context);
        miscManager.complainUser(complainUserId, new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                onComplainResult(context, result);
            }
        });
    }

    public static void complainConfide(final Context context, String confideId) {
        MiscManager miscManager = MiscManager.getInstance(context);
        miscManager.complainConfide(confideId, new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                onComplainResult(context, result);
            }
        });
    }

    private static void onComplainResult(final Context context, int result) {
        final int resId = result == FunctionCallListener.RESULT_CALL_SUCCESS
                ? R.string.complain_success : R.string.complain_failed;
        final Handler handler = new Handler(context.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                ShowToast.makeText(context, R.drawable.emoji_smile,
                        context.getString(resId)).show();
            }
        });
    }

    public static String getUserRemarkName(Context context, String userId) {
        ContactsManager contactsManager = ContactsManager.getInstance(context);
        if (contactsManager.getUserContactByUserId(userId) != null) {
            UserContact[] userContacts = contactsManager.getAllUserContact();
            if (userContacts != null && userContacts.length > 0) {
                for (UserContact contact : contactsManager.getAllUserContact()) {
                    if (contact.getUserId().equals(userId)) {
                        return contact.getRemarkName() != null ? contact.getRemarkName() : contact.getRemarkName();
                    }
                }
            }
        }
        return "";

    }
}
