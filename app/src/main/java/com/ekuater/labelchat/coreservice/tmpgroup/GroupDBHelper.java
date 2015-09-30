package com.ekuater.labelchat.coreservice.tmpgroup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.util.L;

/**
 * @author LinYong
 */
/*package*/ class GroupDBHelper extends SQLiteOpenHelper {

    private static final String TAG = GroupDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "tmp_group_chat.db";
    private static final int DATABASE_VERSION = 1;

    private static GroupDBHelper sSingleton = null;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new GroupDBHelper(context, DATABASE_NAME, DATABASE_VERSION);
        }
    }

    public static GroupDBHelper getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private static class GroupTable implements BaseColumns {

        public static final String TABLE_NAME = "groups";

        public static final String GROUP_ID = "group_id";
        public static final String GROUP_NAME = "group_name";
        public static final String GROUP_LABEL = "group_label";
        public static final String CREATE_USER_ID = "create_user_id";
        public static final String CREATE_TIME = "create_time";
        public static final String EXPIRE_TIME = "expire_time";
        public static final String SYSTEM_TIME = "system_time";
        public static final String LOCAL_CREATE_TIME = "local_create_time";
        public static final String DISMISS_REMIND_TIME = "dismiss_remind_time";
        public static final String GROUP_AVATAR = "group_avatar";
        public static final String STATE = "state";

        public static void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME
                    + " ("
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + GROUP_ID + " TEXT NOT NULL UNIQUE,"
                    + GROUP_NAME + " TEXT NOT NULL,"
                    + GROUP_LABEL + " TEXT NOT NULL,"
                    + CREATE_USER_ID + " TEXT NOT NULL,"
                    + CREATE_TIME + " INTEGER,"
                    + EXPIRE_TIME + " INTEGER,"
                    + SYSTEM_TIME + " INTEGER,"
                    + LOCAL_CREATE_TIME + " INTEGER,"
                    + DISMISS_REMIND_TIME + " INTEGER,"
                    + GROUP_AVATAR + " TEXT,"
                    + STATE + " INTEGER"
                    + ");");
        }
    }

    private static class MemberTable implements BaseColumns {

        public static final String TABLE_NAME = "members";

        public static final String USER_ID = "user_id";
        public static final String LABEL_CODE = "label_code";
        public static final String NICKNAME = "nick_name";
        public static final String MOBILE = "mobile";
        public static final String GENDER = "sex";
        public static final String BIRTHDAY = "birthday";
        public static final String AGE = "age";
        public static final String CONSTELLATION = "constellation";
        public static final String PROVINCE = "province";
        public static final String CITY = "city";
        public static final String SCHOOL = "school";
        public static final String SIGNATURE = "signature";
        public static final String AVATAR = "avatar";
        public static final String AVATAR_THUMB = "avatar_thumb";
        public static final String LABELS = "labels";
        public static final String APPEARANCE_FACE = "face";
        public static final String LOCATION = "location";

        public static void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME
                    + " ("
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + USER_ID + " TEXT NOT NULL UNIQUE,"
                    + LABEL_CODE + " TEXT NOT NULL UNIQUE,"
                    + NICKNAME + " TEXT,"
                    + MOBILE + " TEXT,"
                    + GENDER + " INTEGER,"
                    + BIRTHDAY + " INTEGER,"
                    + AGE + " INTEGER,"
                    + CONSTELLATION + " INTEGER,"
                    + PROVINCE + " TEXT,"
                    + CITY + " TEXT,"
                    + SCHOOL + " TEXT,"
                    + SIGNATURE + " TEXT,"
                    + AVATAR + " TEXT,"
                    + AVATAR_THUMB + " TEXT,"
                    + LABELS + " TEXT,"
                    + APPEARANCE_FACE + " TEXT,"
                    + LOCATION + " TEXT"
                    + ");");
        }
    }

    private static class RelationTable implements BaseColumns {

        public static final String TABLE_NAME = "relations";

        public static final String GROUP_ID = GroupTable.GROUP_ID;
        public static final String USER_ID = MemberTable.USER_ID;

        public static void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME
                    + " ("
                    + _ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + GROUP_ID + " TEXT NOT NULL CONSTRAINT " + GROUP_ID
                    + " REFERENCES " + GroupTable.TABLE_NAME + "(" + GroupTable.GROUP_ID + ")"
                    + " ON DELETE CASCADE ON UPDATE CASCADE,"
                    + USER_ID + " TEXT NOT NULL,"
                    + "UNIQUE(" + GROUP_ID + ", " + USER_ID + ") ON CONFLICT REPLACE"
                    + ");");
        }
    }

    protected GroupDBHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        GroupTable.createTable(db);
        MemberTable.createTable(db);
        RelationTable.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do nothing when upgrade
    }

    /**
     * Add or update group information to database
     *
     * @param group group information
     */
    public void updateGroup(TmpGroup group) {
        if (group == null) {
            throw new NullPointerException("Empty TmpGroup");
        }

        final SQLiteDatabase db = getWritableDatabase();
        final String groupId = group.getGroupId();

        // Now update group information in group table.
        Cursor cursor = db.query(GroupTable.TABLE_NAME, null,
                GroupTable.GROUP_ID + "=?",
                new String[]{groupId},
                null, null, null);
        boolean exist = cursor.getCount() > 0;
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(GroupTable.GROUP_ID, groupId);
        values.put(GroupTable.GROUP_NAME, group.getGroupName());
        values.put(GroupTable.GROUP_LABEL, group.getGroupLabelString());
        values.put(GroupTable.CREATE_USER_ID, group.getCreateUserId());
        values.put(GroupTable.CREATE_TIME, group.getCreateTime());
        values.put(GroupTable.EXPIRE_TIME, group.getExpireTime());
        values.put(GroupTable.SYSTEM_TIME, group.getSystemTime());
        values.put(GroupTable.LOCAL_CREATE_TIME, group.getLocalCreateTime());
        values.put(GroupTable.DISMISS_REMIND_TIME, group.getDismissRemindTime());
        values.put(GroupTable.GROUP_AVATAR, group.getGroupAvatar());
        values.put(GroupTable.STATE, group.getState());

        if (exist) {
            db.update(GroupTable.TABLE_NAME, values,
                    GroupTable.GROUP_ID + "=?",
                    new String[]{groupId});
        } else {
            db.insert(GroupTable.TABLE_NAME, null, values);
        }

        // update member information
        final Stranger[] members = group.getMembers();
        if (members != null) {
            updateMembersInternal(db, members);
        }

        // update group member relationship
        deleteGroupMemberRelationship(db, groupId);
        insertGroupMemberRelationship(db, groupId, members);
    }

    private void deleteGroupMemberRelationship(SQLiteDatabase db, String groupId) {
        db.delete(RelationTable.TABLE_NAME, RelationTable.GROUP_ID + "=?", new String[]{groupId});
    }

    private void insertGroupMemberRelationship(SQLiteDatabase db, String groupId,
                                               Stranger[] members) {
        if (members != null && members.length > 0) {
            for (Stranger member : members) {
                if (member != null) {
                    ContentValues values = new ContentValues();
                    values.put(RelationTable.GROUP_ID, groupId);
                    values.put(RelationTable.USER_ID, member.getUserId());
                    db.insert(RelationTable.TABLE_NAME, null, values);
                }
            }
        }
    }

    /**
     * Delete all group information from database.
     *
     * @param groupId group id
     */
    public void deleteGroup(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }

        final SQLiteDatabase db = getWritableDatabase();
        // delete group, relationship, member, chat history
        db.delete(GroupTable.TABLE_NAME, GroupTable.GROUP_ID + "=?", new String[]{groupId});
        deleteGroupMemberRelationship(db, groupId);
        deleteUselessMembersInternal(db);
    }

    /**
     * Query all group ids in database
     *
     * @return all group ids in database
     */
    @Nullable
    public String[] queryAllGroupId() {
        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(GroupTable.TABLE_NAME,
                new String[]{GroupTable.GROUP_ID},
                null, null, null, null, null);

        String[] groupIds = null;

        if (cursor.getCount() > 0) {
            int idx = 0;
            int groupIdIdx = cursor.getColumnIndex(GroupTable.GROUP_ID);
            groupIds = new String[cursor.getCount()];
            cursor.moveToFirst();
            do {
                groupIds[idx++] = cursor.getString(groupIdIdx);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return groupIds;
    }

    @Nullable
    public TmpGroup queryGroup(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }

        final SQLiteDatabase db = getReadableDatabase();
        final Cursor cursor = db.query(GroupTable.TABLE_NAME, null,
                GroupTable.GROUP_ID + "=?", new String[]{groupId},
                null, null, null);

        TmpGroup group = null;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            String groupName = cursor.getString(cursor.getColumnIndex(GroupTable.GROUP_NAME));
            String groupLabel = cursor.getString(cursor.getColumnIndex(GroupTable.GROUP_LABEL));
            String createUserId = cursor.getString(cursor.getColumnIndex(GroupTable.CREATE_USER_ID));
            long createTime = cursor.getLong(cursor.getColumnIndex(GroupTable.CREATE_TIME));
            long expireTime = cursor.getLong(cursor.getColumnIndex(GroupTable.EXPIRE_TIME));
            long systemTime = cursor.getLong(cursor.getColumnIndex(GroupTable.SYSTEM_TIME));
            long localCreateTime = cursor.getLong(cursor.getColumnIndex(GroupTable.LOCAL_CREATE_TIME));
            long dismissRemindTime = cursor.getLong(cursor.getColumnIndex(GroupTable.DISMISS_REMIND_TIME));
            String groupAvatar = cursor.getString(cursor.getColumnIndex(GroupTable.GROUP_AVATAR));
            int state = cursor.getInt(cursor.getColumnIndex(GroupTable.STATE));

            group = new TmpGroup();
            group.setGroupId(groupId);
            group.setGroupName(groupName);
            group.setGroupLabelByString(groupLabel);
            group.setCreateUserId(createUserId);
            group.setCreateTime(createTime);
            group.setExpireTime(expireTime);
            group.setSystemTime(systemTime);
            group.setLocalCreateTime(localCreateTime);
            group.setDismissRemindTime(dismissRemindTime);
            group.setGroupAvatar(groupAvatar);
            group.setState(state);
            group.setMembers(queryGroupMembers(groupId));
        }

        cursor.close();

        return group;
    }

    private void updateMembersInternal(SQLiteDatabase db, Stranger[] members) {
        for (Stranger member : members) {
            if (member != null) {
                updateMember(db, member);
            }
        }
    }

    private void updateMember(SQLiteDatabase db, Stranger member) {
        // Query this member in database or not.
        Cursor cursor = db.query(MemberTable.TABLE_NAME, null,
                MemberTable.USER_ID + "=?",
                new String[]{member.getUserId()},
                null, null, null);
        boolean exist = cursor.getCount() > 0;
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(MemberTable.USER_ID, member.getUserId());
        values.put(MemberTable.LABEL_CODE, member.getLabelCode());
        values.put(MemberTable.NICKNAME, member.getNickname());
        values.put(MemberTable.MOBILE, member.getMobile());
        values.put(MemberTable.GENDER, member.getSex());
        values.put(MemberTable.BIRTHDAY, member.getBirthday());
        values.put(MemberTable.AGE, member.getAge());
        values.put(MemberTable.CONSTELLATION, member.getConstellation());
        values.put(MemberTable.PROVINCE, member.getProvince());
        values.put(MemberTable.CITY, member.getCity());
        values.put(MemberTable.SCHOOL, member.getSchool());
        values.put(MemberTable.SIGNATURE, member.getSignature());
        values.put(MemberTable.AVATAR, member.getAvatar());
        values.put(MemberTable.AVATAR_THUMB, member.getAvatarThumb());
        values.put(MemberTable.LABELS, member.getLabelsString());
        values.put(MemberTable.LOCATION, member.getLocation() != null
                ? member.getLocation().toString() : "");

        if (exist) {
            db.update(MemberTable.TABLE_NAME, values,
                    MemberTable.USER_ID + "=?",
                    new String[]{member.getUserId()});
        } else {
            db.insert(MemberTable.TABLE_NAME, null, values);
        }
    }

    private void deleteUselessMembersInternal(SQLiteDatabase db) {
        db.delete(MemberTable.TABLE_NAME, MemberTable.TABLE_NAME
                + ".[" + MemberTable.USER_ID + "]"
                + " NOT IN (SELECT DISTINCT " + RelationTable.TABLE_NAME
                + ".[" + RelationTable.USER_ID + "] FROM "
                + RelationTable.TABLE_NAME + ")", null);
    }

    /**
     * Query all group member information from database
     *
     * @param groupId group id
     * @return member array
     */
    @Nullable
    public Stranger[] queryGroupMembers(String groupId) {
        final SQLiteDatabase db = getReadableDatabase();
        final String sql = "SELECT " + MemberTable.TABLE_NAME + ".*"
                + " FROM " + MemberTable.TABLE_NAME
                + " JOIN " + RelationTable.TABLE_NAME + " ON ("
                + RelationTable.TABLE_NAME + ".[" + RelationTable.GROUP_ID + "]=? AND "
                + RelationTable.TABLE_NAME + ".[" + RelationTable.USER_ID
                + "]=" + MemberTable.TABLE_NAME + ".[" + MemberTable.USER_ID + "])";
        final String[] selectionArgs = {
                groupId,
        };

        Cursor cursor = null;
        Stranger[] strangers = null;

        try {
            cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.getCount() > 0) {
                final Stranger[] tmpStrangers = new Stranger[cursor.getCount()];
                final StrangerColumnsMap columnsMap = new StrangerColumnsMap(cursor);
                int idx = 0;

                cursor.moveToFirst();
                do {
                    tmpStrangers[idx++] = buildStranger(cursor, columnsMap);
                } while (cursor.moveToNext());
                strangers = tmpStrangers;
            }
        } catch (Exception e) {
            L.w(TAG, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return strangers;
    }

    public void removeMember(String groupId, String userId) {
        final SQLiteDatabase db = getReadableDatabase();
        db.delete(RelationTable.TABLE_NAME, RelationTable.GROUP_ID + "=? AND "
                + RelationTable.USER_ID + "=?", new String[]{groupId, userId});
    }

    private Stranger buildStranger(Cursor cursor, StrangerColumnsMap columnsMap) {
        final String userId = cursor.getString(columnsMap.mUserId);
        final String labelCode = cursor.getString(columnsMap.mLabelCode);
        final String nickname = cursor.getString(columnsMap.mNickname);
        final String mobile = cursor.getString(columnsMap.mMobile);
        final int sex = cursor.getInt(columnsMap.mSex);
        final long birthday = cursor.getLong(columnsMap.mBirthday);
        final int age = cursor.getInt(columnsMap.mAge);
        final int constellation = cursor.getInt(columnsMap.mConstellation);
        final String province = cursor.getString(columnsMap.mProvince);
        final String city = cursor.getString(columnsMap.mCity);
        final String school = cursor.getString(columnsMap.mSchool);
        final String signature = cursor.getString(columnsMap.mSignature);
        final String avatar = cursor.getString(columnsMap.mAvatar);
        final String avatarThumb = cursor.getString(columnsMap.mAvatarThumb);
        final String labels = cursor.getString(columnsMap.mLabels);
        final String location = cursor.getString(columnsMap.mLocation);
        final Stranger stranger = new Stranger();

        stranger.setUserId(userId);
        stranger.setLabelCode(labelCode);
        stranger.setNickname(nickname);
        stranger.setMobile(mobile);
        stranger.setSex(sex);
        stranger.setBirthday(birthday);
        stranger.setAge(age);
        stranger.setConstellation(constellation);
        stranger.setProvince(province);
        stranger.setCity(city);
        stranger.setSchool(school);
        stranger.setSignature(signature);
        stranger.setAvatar(avatar);
        stranger.setAvatarThumb(avatarThumb);
        stranger.setLabelsByString(labels);
        stranger.setLocationByString(location);

        return stranger;
    }

    private static class StrangerColumnsMap {

        public final int mId;
        public final int mUserId;
        public final int mLabelCode;
        public final int mNickname;
        public final int mMobile;
        public final int mSex;
        public final int mBirthday;
        public final int mAge;
        public final int mConstellation;
        public final int mProvince;
        public final int mCity;
        public final int mSchool;
        public final int mSignature;
        public final int mAvatar;
        public final int mAvatarThumb;
        public final int mLabels;
        public final int mAppearanceFace;
        public final int mLocation;

        private static int getColumnIndex(Cursor cursor, String columnName) {
            return cursor.getColumnIndex(columnName);
        }

        public StrangerColumnsMap(Cursor cursor) {
            mId = getColumnIndex(cursor, MemberTable._ID);
            mUserId = getColumnIndex(cursor, MemberTable.USER_ID);
            mLabelCode = getColumnIndex(cursor, MemberTable.LABEL_CODE);
            mNickname = getColumnIndex(cursor, MemberTable.NICKNAME);
            mMobile = getColumnIndex(cursor, MemberTable.MOBILE);
            mSex = getColumnIndex(cursor, MemberTable.GENDER);
            mBirthday = getColumnIndex(cursor, MemberTable.BIRTHDAY);
            mAge = getColumnIndex(cursor, MemberTable.AGE);
            mConstellation = getColumnIndex(cursor, MemberTable.CONSTELLATION);
            mProvince = getColumnIndex(cursor, MemberTable.PROVINCE);
            mCity = getColumnIndex(cursor, MemberTable.CITY);
            mSchool = getColumnIndex(cursor, MemberTable.SCHOOL);
            mSignature = getColumnIndex(cursor, MemberTable.SIGNATURE);
            mAvatar = getColumnIndex(cursor, MemberTable.AVATAR);
            mAvatarThumb = getColumnIndex(cursor, MemberTable.AVATAR_THUMB);
            mLabels = getColumnIndex(cursor, MemberTable.LABELS);
            mAppearanceFace = getColumnIndex(cursor, MemberTable.APPEARANCE_FACE);
            mLocation = getColumnIndex(cursor, MemberTable.LOCATION);
        }
    }
}
