package com.ekuater.labelchat.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.UserContact;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Leo on 2015/1/22.
 *
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
public class SystemAccount {

    private static SystemAccount sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new SystemAccount(context.getApplicationContext());
        }
    }

    public static SystemAccount getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final int mFieldCount;
    private final String mAppTeamUserId;
    private final Map<String, UserContact> mAccountMap;

    private SystemAccount(Context context) {
        Resources res = context.getResources();
        mFieldCount = res.getInteger(R.integer.system_account_field);
        mAppTeamUserId = res.getString(R.string.app_team_user_id);
        mAccountMap = new LinkedHashMap<>();
        parseAccount(res);
    }

    private void parseAccount(Resources res) {
        final TypedArray ar = res.obtainTypedArray(R.array.system_accounts);

        for (int i = 0; i < ar.length(); ++i) {
            final String[] values = res.getStringArray(ar.getResourceId(i, 0));
            addNewAccount(values);
        }

        ar.recycle();
    }

    private void addNewAccount(String[] values) {
        if (values == null || values.length != mFieldCount) {
            throw new IllegalArgumentException();
        }

        UserContact contact = new UserContact();
        contact.setLabelCode(values[0]);
        contact.setUserId(values[1]);
        contact.setNickname(values[2]);
        contact.setAvatarThumb(values[3]);
        contact.setAvatar(values[4]);

        mAccountMap.put(contact.getUserId(), contact);
    }

    public UserContact getAccount(String userId) {
        return mAccountMap.get(userId);
    }

    public String getAppTeamUserId() {
        return mAppTeamUserId;
    }
}
