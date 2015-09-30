package com.ekuater.labelchat.coreservice.following.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Leo on 2015/3/27.
 *
 * @author LinYong
 */
public class FollowUserDBHelper {

    private static final String DATABASE_NAME = "follow_users.db";

    private final DBFollowingUserDao dBFollowingUserDao;
    private final DBFollowerUserDao dBFollowerUserDao;

    public FollowUserDBHelper(Context context) {
        DBFollowUserMaster.DevOpenHelper helper = new DBFollowUserMaster.DevOpenHelper(
                context, DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DBFollowUserMaster daoMaster = new DBFollowUserMaster(database);
        DBFollowUserSession daoSession = daoMaster.newSession();
        dBFollowingUserDao = daoSession.getDBFollowingUserDao();
        dBFollowerUserDao = daoSession.getDBFollowerUserDao();
    }

    public void addFollowingUser(DBFollowingUser followingUser) {
        DBFollowingUser oldFollowingUser = getFollowingUser(followingUser.getUserId());
        if (oldFollowingUser != null) {
            followingUser.setId(oldFollowingUser.getId());
            dBFollowingUserDao.update(followingUser);
        } else {
            dBFollowingUserDao.insert(followingUser);
        }
    }

    public void updateFollowingUser(DBFollowingUser followingUser) {
        DBFollowingUser oldFollowingUser = getFollowingUser(followingUser.getUserId());
        if (oldFollowingUser != null) {
            followingUser.setId(oldFollowingUser.getId());
            dBFollowingUserDao.update(followingUser);
        }
    }

    public DBFollowingUser getFollowingUser(String userId) {
        return dBFollowingUserDao.queryBuilder().where(
                DBFollowingUserDao.Properties.UserId.eq(userId)).unique();
    }

    public List<DBFollowingUser> getAllFollowingUser() {
        return dBFollowingUserDao.loadAll();
    }

    public void deleteFollowingUser(String userId) {
        DBFollowingUser followUser = getFollowingUser(userId);
        if (followUser != null) {
            dBFollowingUserDao.delete(followUser);
        }
    }

    public void deleteAllFollowingUser() {
        dBFollowingUserDao.deleteAll();
    }

    public void addFollowerUser(DBFollowerUser followerUser) {
        DBFollowerUser oldFollowerUser = getFollowerUser(followerUser.getUserId());
        if (oldFollowerUser != null) {
            followerUser.setId(oldFollowerUser.getId());
            dBFollowerUserDao.update(followerUser);
        } else {
            dBFollowerUserDao.insert(followerUser);
        }
    }

    public void updateFollowerUser(DBFollowerUser followerUser) {
        DBFollowerUser oldFollowerUser = getFollowerUser(followerUser.getUserId());
        if (oldFollowerUser != null) {
            followerUser.setId(oldFollowerUser.getId());
            dBFollowerUserDao.update(followerUser);
        }
    }

    public DBFollowerUser getFollowerUser(String userId) {
        return dBFollowerUserDao.queryBuilder().where(
                DBFollowerUserDao.Properties.UserId.eq(userId)).unique();
    }

    public List<DBFollowerUser> batchQueryFollowerUser(String[] userIds) {
        return dBFollowerUserDao.queryBuilder().where(
                DBFollowerUserDao.Properties.UserId.in(Arrays.asList(userIds))).list();
    }

    public List<DBFollowerUser> getAllFollowerUser() {
        return dBFollowerUserDao.loadAll();
    }

    public void deleteFollowerUser(String userId) {
        DBFollowerUser followUser = getFollowerUser(userId);
        if (followUser != null) {
            dBFollowerUserDao.delete(followUser);
        }
    }

    public void deleteAllFollowerUser() {
        dBFollowerUserDao.deleteAll();
    }
}
