
package com.ekuater.labelchat.coreservice.systempush;

import com.ekuater.labelchat.datastruct.SystemPush;

/**
 * @author LinYong
 */
public interface ISystemPushListener {

    /**
     * Notify there is a new SystemPush message has been received.
     *
     * @param systemPush new SystemPush message
     */
    public void onNewSystemPushReceived(SystemPush systemPush);

    public void onNewAccountPushReceived(SystemPush systemPush);

    public void onNewContactPushReceived(SystemPush systemPush);

    public void onNewTmpGroupPushReceived(SystemPush systemPush);
}
