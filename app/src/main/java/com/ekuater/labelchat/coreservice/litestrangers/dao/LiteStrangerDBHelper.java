package com.ekuater.labelchat.coreservice.litestrangers.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ekuater.labelchat.datastruct.LiteStranger;

/**
 * Created by Leo on 2015/3/3.
 *
 * @author LinYong
 */
public class LiteStrangerDBHelper {

    private static final String DATABASE_NAME = "litestrangers.db";

    public static DBLiteStranger toDBLiteStranger(LiteStranger stranger) {
        DBLiteStranger dbStranger = new DBLiteStranger();
        dbStranger.setUserId(stranger.getUserId());
        dbStranger.setLabelCode(stranger.getLabelCode());
        dbStranger.setNickname(stranger.getNickname());
        dbStranger.setAvatarThumb(stranger.getAvatarThumb());
        dbStranger.setCacheTime(System.currentTimeMillis());
        return dbStranger;
    }

    public static LiteStranger toLiteStranger(DBLiteStranger dbStranger) {
        LiteStranger stranger = new LiteStranger();
        stranger.setUserId(dbStranger.getUserId());
        stranger.setLabelCode(dbStranger.getLabelCode());
        stranger.setNickname(dbStranger.getNickname());
        stranger.setAvatarThumb(dbStranger.getAvatarThumb());
        return stranger;
    }

    private final DBLiteStrangerDao dbLiteStrangerDao;

    public LiteStrangerDBHelper(Context context) {
        DBLiteStrangerMaster.DevOpenHelper helper = new DBLiteStrangerMaster.DevOpenHelper(
                context, DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DBLiteStrangerMaster daoMaster = new DBLiteStrangerMaster(database);
        DBLiteStrangerSession daoSession = daoMaster.newSession();
        dbLiteStrangerDao = daoSession.getDBLiteStrangerDao();
    }

    public void add(DBLiteStranger stranger) {
        DBLiteStranger oldStranger = get(stranger.getUserId());
        if (oldStranger == null) {
            dbLiteStrangerDao.insert(stranger);
        } else {
            stranger.setId(oldStranger.getId());
            dbLiteStrangerDao.update(stranger);
        }
    }

    public DBLiteStranger get(String userId) {
        return dbLiteStrangerDao.queryBuilder().where(
                DBLiteStrangerDao.Properties.UserId.eq(userId)).unique();
    }

    public void delete(String userId) {
        DBLiteStranger stranger = get(userId);
        if (stranger != null) {
            dbLiteStrangerDao.delete(stranger);
        }
    }

    public void delelteAll() {
        dbLiteStrangerDao.deleteAll();
    }
}
