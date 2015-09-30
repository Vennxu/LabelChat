package com.ekuater.labelchat.coreservice.following;

import com.ekuater.labelchat.coreservice.following.dao.DBFollowerUser;
import com.ekuater.labelchat.coreservice.following.dao.DBFollowingUser;
import com.ekuater.labelchat.datastruct.FollowUser;

/**
 * Created by Leo on 2015/3/28.
 *
 * @author LinYong
 */
public class Utils {

    public static FollowUser toFollowUser(DBFollowingUser followingUser) {
        FollowUser followUser = new FollowUser();

        followUser.setUserId(followingUser.getUserId());
        followUser.setLabelCode(followingUser.getLabelCode());
        followUser.setNickname(followingUser.getNickname());
        followUser.setAvatarThumb(followingUser.getAvatarThumb());
        followUser.setAvatar(followingUser.getAvatar());
        followUser.setGender(followingUser.getGender());
        return followUser;
    }

    public static DBFollowingUser toFollowingUser(FollowUser followUser) {
        DBFollowingUser followingUser = new DBFollowingUser();

        followingUser.setUserId(followUser.getUserId());
        followingUser.setLabelCode(followUser.getLabelCode());
        followingUser.setNickname(followUser.getNickname());
        followingUser.setAvatarThumb(followUser.getAvatarThumb());
        followingUser.setAvatar(followUser.getAvatar());
        followingUser.setGender(followUser.getGender());
        return followingUser;
    }

    public static FollowUser toFollowUser(DBFollowerUser followerUser) {
        FollowUser followUser = new FollowUser();

        followUser.setUserId(followerUser.getUserId());
        followUser.setLabelCode(followerUser.getLabelCode());
        followUser.setNickname(followerUser.getNickname());
        followUser.setAvatarThumb(followerUser.getAvatarThumb());
        followUser.setAvatar(followerUser.getAvatar());
        followUser.setGender(followerUser.getGender());
        return followUser;
    }

    public static DBFollowerUser toFollowerUser(FollowUser followUser) {
        DBFollowerUser followerUser = new DBFollowerUser();

        followerUser.setUserId(followUser.getUserId());
        followerUser.setLabelCode(followUser.getLabelCode());
        followerUser.setNickname(followUser.getNickname());
        followerUser.setAvatarThumb(followUser.getAvatarThumb());
        followerUser.setAvatar(followUser.getAvatar());
        followerUser.setGender(followUser.getGender());
        return followerUser;
    }
}
