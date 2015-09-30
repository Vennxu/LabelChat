package com.ekuater.daogen;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Leo on 2015/1/29.
 *
 * @author LinYong
 */
public class PendingChatDBSchema implements IDBSchema {

    private static final String JAVA_PACKAGE = "com.ekuater.labelchat.coreservice.chatmessage.dao";

    @Override
    public Schema getSchema() {
        Schema schema = new Schema(1, "PendingChat", JAVA_PACKAGE);
        addPendingChat(schema);
        return schema;
    }

    private void addPendingChat(Schema schema) {
        Entity entity = schema.addEntity("PendingChat");
        entity.addIdProperty();
        entity.addIntProperty("type").notNull();
        entity.addIntProperty("conversationType").notNull();
        entity.addIntProperty("state").notNull();
        entity.addStringProperty("content");
        entity.addStringProperty("preview");
        entity.addLongProperty("time").notNull();
        entity.addStringProperty("targetId").notNull();
        entity.addStringProperty("senderId");
        entity.addStringProperty("messageId");
        entity.addIntProperty("direction").notNull();
    }
}
