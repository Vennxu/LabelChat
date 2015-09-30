package com.ekuater.labelchat.ui.fragment.usershowpage;

/**
 * Created by Leo on 2015/2/6.
 *
 * @author LinYong
 */
public interface UserAdapterListener {

    public void onBaseUserInfoUpdate(BaseUserInfo baseUserInfo);

    public void onPageLoading();

    public void onPageLoadDone();
}
