
package com.ekuater.labelchat.delegate;

/**
 * command execute response interface
 * 
 * @author LinYong
 */
public interface ICommandResponseHandler {

    /**
     * on command execute response
     * 
     * @param result execute result, success or error code
     * @param response response String data
     */
    public void onResponse(int result, String response);
}
