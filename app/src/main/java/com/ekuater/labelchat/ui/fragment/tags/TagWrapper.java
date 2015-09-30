package com.ekuater.labelchat.ui.fragment.tags;

import com.ekuater.labelchat.datastruct.UserTag;

/**
 * Created by Leo on 2015/3/17.
 *
 * @author LinYong
 */
public class TagWrapper {

    private UserTag userTag;
    private boolean isSelected;

    public TagWrapper(UserTag userTag) {
        this(userTag, false);
    }

    public TagWrapper(UserTag userTag, boolean isSelected) {
        this.userTag = userTag;
        this.isSelected = isSelected;
    }

    public UserTag getUserTag() {
        return userTag;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
