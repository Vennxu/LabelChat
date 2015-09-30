package com.ekuater.daogen;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Leo on 2015/3/3.
 *
 * @author LinYong
 */
public class LiteStrangerDBSchema implements IDBSchema {

    private static final String JAVA_PACKAGE = "com.ekuater.labelchat.coreservice.litestrangers.dao";

    @Override
    public Schema getSchema() {
        Schema schema = new Schema(1, "DBLiteStranger", JAVA_PACKAGE);
        addStranger(schema);
        return schema;
    }

    private void addStranger(Schema schema) {
        Entity entity = schema.addEntity("DBLiteStranger");
        entity.addIdProperty();
        entity.addStringProperty("userId").notNull().unique();
        entity.addStringProperty("labelCode").notNull().unique();
        entity.addStringProperty("nickname");
        entity.addStringProperty("avatarThumb");
        entity.addLongProperty("cacheTime");
    }
}
