
package com.ekuater.labelchat.coreservice.contacts;

import com.ekuater.labelchat.datastruct.UserContact;

/**
 * @author LinYong
 */
public interface IContactsListener {

    public void onNewContactAdded(UserContact contact);

    public void onContactUpdated(UserContact contact);

    public void onModifyFriendRemarkResult(int result, String friendUserId,
                                           String friendRemark);

    public void onDeleteFriendResult(int result, String friendUserId,
                                     String friendLabelCode);

    public void onContactDefriendedMe(String friendUserId);
}
