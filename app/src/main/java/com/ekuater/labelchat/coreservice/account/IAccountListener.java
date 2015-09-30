
package com.ekuater.labelchat.coreservice.account;

/**
 * @author LinYong
 */
public interface IAccountListener {

    public void onLoginResult(int result, boolean accountChanged, boolean infoUpdated);

    public void onLogoutResult(int result);

    public void onRegisterResult(int result);

    public void onPersonalInfoUpdatedResult(int result);

    public void onLoginInOtherClient();

    public void onOAuthBindAccountResult(int result);
}
