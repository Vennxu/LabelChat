
package com.ekuater.labelchat.coreservice.command.client;

/**
 * @author LinYong
 */
public class AbstractClient implements ICommandClient {

    @Override
    public ICommandRequest get(String url, String param, ICommandResponse response) {
        return null;
    }

    @Override
    public ICommandRequest post(String url, String param, ICommandResponse response) {
        return null;
    }

    @Override
    public ICommandRequest put(String url, String param, ICommandResponse response) {
        return null;
    }

    @Override
    public ICommandRequest delete(String url, String param, ICommandResponse response) {
        return null;
    }
}
