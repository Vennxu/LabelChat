
package com.ekuater.labelchat.coreservice.command;

import com.ekuater.labelchat.datastruct.RequestCommand;

/**
 * @author LinYong
 */
public interface ICommandResponseHandler {

    /**
     * on command execute response
     * 
     * @param command command to be executed
     * @param result execute result, success or error code
     * @param response response String data
     */
    public void onResponse(RequestCommand command, int result, String response);
}
