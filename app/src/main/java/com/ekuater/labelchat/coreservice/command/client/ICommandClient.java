
package com.ekuater.labelchat.coreservice.command.client;

/**
 * @author LinYong
 */
public interface ICommandClient {

    public ICommandRequest get(String url, String param, ICommandResponse response);

    public ICommandRequest post(String url, String param, ICommandResponse response);

    public ICommandRequest put(String url, String param, ICommandResponse response);

    public ICommandRequest delete(String url, String param, ICommandResponse response);
}
