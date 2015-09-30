package com.ekuater.labelchat.delegate;

/**
 * Created by Leo on 2015/4/12.
 *
 * @author LinYong
 */
public interface IUploadResponseHandler extends ICommandResponseHandler {

    /**
     * Fired when the request progress, override to handle in your own code
     *
     * @param bytesWritten offset from start of file
     * @param totalSize    total size of file
     */
    void onProgress(long bytesWritten, long totalSize);
}
