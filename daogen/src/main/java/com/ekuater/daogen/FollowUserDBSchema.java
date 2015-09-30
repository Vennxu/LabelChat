package com.ekuater.daogen;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Leo on 2015/3/27.
 *
 * @author LinYong
 */
public class FollowUserDBSchema implements IDBSchema {

    private static final String JAVA_PACKAGE = "com.ekuater.labelchat.coreservice.following.dao";

    @Override
    public Schema getSchema() {
        Schema schema = new Schema(2, "DBFollowUser", JAVA_PACKAGE);
        addFollowingUser(schema);
        addFollowerUser(schema);
        return schema;
    }

    private void addFollowingUser(Schema schema) {
        addFollowUser(schema, "DBFollowingUser");
    }

    private void addFollowerUser(Schema schema) {
        addFollowUser(schema, "DBFollowerUser");
    }

    private void addFollowUser(Schema schema, String className) {
        Entity entity = schema.addEntity(className);
        entity.addIdProperty();
        entity.addStringProperty("userId").notNull().unique();
        entity.addStringProperty("labelCode").notNull().unique();
        entity.addStringProperty("nickname");
        entity.addStringProperty("avatar");
        entity.addStringProperty("avatarThumb");
        entity.addIntProperty("gender");
    }
}
