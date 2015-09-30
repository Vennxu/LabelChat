package com.ekuater.labelchat.ui.fragment.tags;

import com.ekuater.labelchat.datastruct.UserTag;

/**
 * Created by Leo on 2015/3/17.
 *
 * @author LinYong
 */
public interface TagSelectListener {

    public void onTagSelectChanged(UserTag tag, boolean selected);
}
