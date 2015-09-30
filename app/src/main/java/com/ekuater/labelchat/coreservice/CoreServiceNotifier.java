package com.ekuater.labelchat.coreservice;

import android.os.RemoteException;

/**
 * Created by Leo on 2015/3/30.
 *
 * @author LinYong
 */
public interface CoreServiceNotifier {

    public void notify(ICoreServiceListener listener) throws RemoteException;
}
