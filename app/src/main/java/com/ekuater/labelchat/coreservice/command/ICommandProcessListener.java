
package com.ekuater.labelchat.coreservice.command;

/**
 * @author LinYong
 */
public interface ICommandProcessListener extends ICommandCheckListener {

    public void onSuccess(String cmdSession, String response);

    public void onFailure(String cmdSession, String response, int errorCode);
}
