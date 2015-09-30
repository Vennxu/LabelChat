package com.ekuater.labelchat.ui.fragment.main;

import android.content.Context;

import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.FollowingManager;

/**
 * Created by Leo on 2015/4/9.
 *
 * @author LinYong
 */
public class FollowerLoadTask extends FollowLoadTask {

    private ContactsListItem.FollowerItemListener mItemListener;
    private FollowingManager mFollowingManager;
    private AvatarManager mAvatarManager;

    public FollowerLoadTask(Context context, FollowLoadListener listener,
                            ContactsListItem.FollowerItemListener itemListener) {
        super(listener);
        mItemListener = itemListener;
        mFollowingManager = FollowingManager.getInstance(context);
        mAvatarManager = AvatarManager.getInstance(context);
    }

    @Override
    protected FollowUser[] getAllFollowUser() {
        return mFollowingManager.getAllFollowerUser();
    }

    @Override
    protected ContactsListItem.Item newFollowItem(FollowUser followUser, String sortLetter) {
        String userId = followUser.getUserId();
        boolean followEachOther = mFollowingManager.getFollowingUser(userId) != null;
        return new ContactsListItem.FollowerItem(followUser, sortLetter,
                followEachOther, mAvatarManager, mItemListener);
    }
}