
package com.ekuater.labelchat.coreservice.command.client;

/**
 * @author LinYong
 */
public interface ICommandExecuteListener {

    public void onSuccess(int statusCode, String response);

    public void onFailure(int statusCode, String response, Throwable throwable);
}
