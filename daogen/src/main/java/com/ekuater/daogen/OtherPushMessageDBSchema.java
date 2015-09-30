package com.ekuater.daogen;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Administrator on 2015/4/22.
 *
 * @author FanChong
 */
public class OtherPushMessageDBSchema implements IDBSchema {

    private static final String JAVA_PACKAGE = "com.ekuater.labelchat.coreservice.systempush.dao";

    @Override
    public Schema getSchema() {
        Schema schema = new Schema(1, "DBSystemPush", JAVA_PACKAGE);
        addSystemPush(schema);
        return schema;
    }

    private void addSystemPush(Schema schema) {
        Entity entity = schema.addEntity("DBSystemPush");
        entity.addIdProperty().primaryKeyAsc();
        entity.addStringProperty("content");
        entity.addLongProperty("time");
        entity.addIntProperty("state");
        entity.addIntProperty("type");
        entity.addStringProperty("flag");
    }
}
