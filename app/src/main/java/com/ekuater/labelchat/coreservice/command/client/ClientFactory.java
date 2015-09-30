
package com.ekuater.labelchat.coreservice.command.client;

/**
 * @author LinYong
 */
public final class ClientFactory {

    public static ICommandClient getDefaultClient() {
        return HttpClient.getInstance();
    }
}
