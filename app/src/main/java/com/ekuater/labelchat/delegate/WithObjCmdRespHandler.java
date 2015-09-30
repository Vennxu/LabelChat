package com.ekuater.labelchat.delegate;

/**
 * @author LinYong
 */
/*package*/ abstract class WithObjCmdRespHandler implements ICommandResponseHandler {

    protected final Object mObj;

    public WithObjCmdRespHandler(Object obj) {
        mObj = obj;
    }
}
