package com.ekuater.labelchat.coreservice.strangers.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ekuater.labelchat.datastruct.Stranger;

/**
 * Created by Leo on 2015/1/30.
 *
 * @author LinYong
 */
public class StrangerDBHelper {

    private static final String DATABASE_NAME = "strangers.db";

    public static DBStranger toDBStranger(Stranger stranger) {
        DBStranger dbStranger = new DBStranger();

        dbStranger.setUserId(stranger.getUserId());
        dbStranger.setLabelCode(stranger.getLabelCode());
        dbStranger.setNickname(stranger.getNickname());
        dbStranger.setMobile(stranger.getMobile());
        dbStranger.setGender(stranger.getSex());
        dbStranger.setBirthday(stranger.getBirthday());
        dbStranger.setAge(stranger.getAge());
        dbStranger.setConstellation(stranger.getConstellation());
        dbStranger.setProvince(stranger.getProvince());
        dbStranger.setCity(stranger.getCity());
        dbStranger.setSchool(stranger.getSchool());
        dbStranger.setSignature(stranger.getSignature());
        dbStranger.setAvatar(stranger.getAvatar());
        dbStranger.setAvatarThumb(stranger.getAvatarThumb());
        dbStranger.setLabels(stranger.getLabelsString());
        dbStranger.setLocation(stranger.getLocationString());

        return dbStranger;
    }

    public static Stranger toStranger(DBStranger dbStranger) {
        Stranger stranger = new Stranger();

        stranger.setUserId(dbStranger.getUserId());
        stranger.setLabelCode(dbStranger.getLabelCode());
        stranger.setNickname(dbStranger.getNickname());
        stranger.setMobile(dbStranger.getMobile());
        stranger.setSex(dbStranger.getGender());
        stranger.setBirthday(dbStranger.getBirthday());
        stranger.setAge(dbStranger.getAge());
        stranger.setConstellation(dbStranger.getConstellation());
        stranger.setProvince(dbStranger.getProvince());
        stranger.setCity(dbStranger.getCity());
        stranger.setSchool(dbStranger.getSchool());
        stranger.setSignature(dbStranger.getSignature());
        stranger.setAvatar(dbStranger.getAvatar());
        stranger.setAvatarThumb(dbStranger.getAvatarThumb());
        stranger.setLabelsByString(dbStranger.getLabels());
        stranger.setLocationByString(dbStranger.getLocation());

        return stranger;
    }

    private final DBStrangerDao dbStrangerDao;

    public StrangerDBHelper(Context context) {
        DBStrangerMaster.DevOpenHelper helper = new DBStrangerMaster.DevOpenHelper(
                context, DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DBStrangerMaster daoMaster = new DBStrangerMaster(database);
        DBStrangerSession daoSession = daoMaster.newSession();
        dbStrangerDao = daoSession.getDBStrangerDao();
    }

    public void addDBStranger(DBStranger dbStranger) {
        DBStranger oldStranger = getDBStranger(dbStranger.getUserId());
        if (oldStranger == null) {
            dbStrangerDao.insert(dbStranger);
        } else {
            dbStranger.setId(oldStranger.getId());
            dbStrangerDao.update(dbStranger);
        }
    }

    public DBStranger getDBStranger(String userId) {
        return dbStrangerDao.queryBuilder().where(DBStrangerDao
                .Properties.UserId.eq(userId)).unique();
    }

    public void deleteDBStranger(String userId) {
        DBStranger dbStranger = getDBStranger(userId);
        if (dbStranger != null) {
            dbStrangerDao.delete(dbStranger);
        }
    }
    public void deleteAll(){
        dbStrangerDao.deleteAll();
    }
}
