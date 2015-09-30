package com.ekuater.daogen;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Leo on 2015/1/30.
 *
 * @author LinYong
 */
public class StrangerDBSchema implements IDBSchema {

    private static final String JAVA_PACKAGE = "com.ekuater.labelchat.coreservice.strangers.dao";

    @Override
    public Schema getSchema() {
        Schema schema = new Schema(1, "DBStranger", JAVA_PACKAGE);
        addStranger(schema);
        return schema;
    }

    private void addStranger(Schema schema) {
        Entity entity = schema.addEntity("DBStranger");
        entity.addIdProperty();
        entity.addStringProperty("userId").notNull().unique();
        entity.addStringProperty("labelCode").notNull().unique();
        entity.addStringProperty("nickname");
        entity.addStringProperty("mobile");
        entity.addIntProperty("gender");
        entity.addLongProperty("birthday");
        entity.addIntProperty("age");
        entity.addIntProperty("constellation");
        entity.addStringProperty("province");
        entity.addStringProperty("city");
        entity.addStringProperty("school");
        entity.addStringProperty("signature");
        entity.addStringProperty("avatar");
        entity.addStringProperty("avatarThumb");
        entity.addStringProperty("labels");
        entity.addStringProperty("location");
    }
}
