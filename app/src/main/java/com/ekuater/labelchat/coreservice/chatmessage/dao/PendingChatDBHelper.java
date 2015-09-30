package com.ekuater.labelchat.coreservice.chatmessage.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ekuater.labelchat.datastruct.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by Leo on 2015/1/30.
 * PendingChat message database helper
 *
 * @author LinYong
 */
public class PendingChatDBHelper {

    private static final String DATABASE_NAME = "pending_chat.db";

    public static PendingChat toPendingChat(ChatMessage chatMessage) {
        PendingChat pendingChat = new PendingChat();

        pendingChat.setType(chatMessage.getType());
        pendingChat.setConversationType(chatMessage.getConversationType());
        pendingChat.setState(chatMessage.getState());
        pendingChat.setContent(chatMessage.getContent());
        pendingChat.setPreview(chatMessage.getPreview());
        pendingChat.setTime(chatMessage.getTime());
        pendingChat.setTargetId(chatMessage.getTargetId());
        pendingChat.setSenderId(chatMessage.getSenderId());
        pendingChat.setDirection(chatMessage.getDirection());

        return pendingChat;
    }

    public static ChatMessage toChatMessage(PendingChat pendingChat) {
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setType(pendingChat.getType());
        chatMessage.setConversationType(pendingChat.getConversationType());
        chatMessage.setState(pendingChat.getState());
        chatMessage.setContent(pendingChat.getContent());
        chatMessage.setPreview(pendingChat.getPreview());
        chatMessage.setTime(pendingChat.getTime());
        chatMessage.setTargetId(pendingChat.getTargetId());
        chatMessage.setSenderId(pendingChat.getSenderId());
        chatMessage.setDirection(pendingChat.getDirection());

        return chatMessage;
    }

    private final PendingChatDao pendingChatDao;

    public PendingChatDBHelper(Context context) {
        PendingChatMaster.DevOpenHelper helper = new PendingChatMaster.DevOpenHelper(
                context, DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        PendingChatMaster daoMaster = new PendingChatMaster(database);
        PendingChatSession daoSession = daoMaster.newSession();
        pendingChatDao = daoSession.getPendingChatDao();
    }

    public void addPendingChat(PendingChat pendingChat) {
        pendingChatDao.insert(pendingChat);
    }

    public List<PendingChat> getTargetPendingChats(String targetId) {
        return pendingChatDao.queryBuilder().where(
                PendingChatDao.Properties.TargetId.eq(targetId))
                .list();
    }

    public List<PendingChat> getSenderPendingChats(String senderId, int[] conversationTypes) {
        QueryBuilder<PendingChat> qb = pendingChatDao.queryBuilder();

        if (conversationTypes != null && conversationTypes.length > 0) {
            List<Integer> inList = new ArrayList<Integer>();
            for (int type : conversationTypes) {
                inList.add(type);
            }
            qb.where(PendingChatDao.Properties.SenderId.eq(senderId),
                    PendingChatDao.Properties.ConversationType.in(inList));
        } else {
            qb.where(PendingChatDao.Properties.SenderId.eq(senderId));
        }

        return qb.list();
    }

    public List<PendingChat> getEveryTargetLastPendingChat() {
        return pendingChatDao.queryRaw(" GROUP BY "
                + PendingChatDao.Properties.TargetId.columnName);
    }

    public void deletePendingChats(List<PendingChat> pendingChats) {
        pendingChatDao.deleteInTx(pendingChats);
    }

    public void deleteAll() {
        pendingChatDao.deleteAll();
    }

    public void deleteTarget(String targetId) {
        deletePendingChats(getTargetPendingChats(targetId));
    }
}
