package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.FollowingManager;
import com.ekuater.labelchat.ui.util.CharacterParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Leo on 2015/3/28.
 *
 * @author LinYong
 */
public class FollowingLoadTask extends FollowLoadTask {

    private ContactsListItem.FollowingItemListener mItemListener;
    private FollowingManager mFollowingManager;
    private AvatarManager mAvatarManager;

    public FollowingLoadTask(Context context, FollowLoadListener listener,
                             ContactsListItem.FollowingItemListener itemListener) {
        super(listener);
        mItemListener = itemListener;
        mFollowingManager = FollowingManager.getInstance(context);
        mAvatarManager = AvatarManager.getInstance(context);
    }

    @Override
    protected FollowUser[] getAllFollowUser() {
        return mFollowingManager.getAllFollowingUser();
    }

    @Override
    protected ContactsListItem.Item newFollowItem(FollowUser followUser, String sortLetter) {
        String userId = followUser.getUserId();
        boolean followEachOther = mFollowingManager.getFollowerUser(userId) != null;
        return new ContactsListItem.FollowingItem(followUser, sortLetter,
                followEachOther, mAvatarManager, mItemListener);
    }
}
