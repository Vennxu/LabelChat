package com.ekuater.labelchat.coreservice.command;

/**
 * Created by Leo on 2015/1/19.
 *
 * @author LinYong
 */
public interface ICommandCheckListener {

    public void onSessionInvalid(String cmdSession, String response);
}
