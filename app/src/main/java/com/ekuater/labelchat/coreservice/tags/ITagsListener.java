package com.ekuater.labelchat.coreservice.tags;

/**
 * Created by Leo on 2015/3/16.
 *
 * @author LinYong
 */
public interface ITagsListener {

    public void onTagUpdated();

    public void onSetTagResult(int result);
}
