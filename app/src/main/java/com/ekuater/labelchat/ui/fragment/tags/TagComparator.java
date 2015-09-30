package com.ekuater.labelchat.ui.fragment.tags;

import com.ekuater.labelchat.datastruct.UserTag;

import java.util.Comparator;

/**
 * Created by Leo on 2015/4/1.
 *
 * @author LinYong
 */
public class TagComparator implements Comparator<UserTag> {

    @Override
    public int compare(UserTag lhs, UserTag rhs) {
        final int lTypeId = lhs.getTypeId();
        final int rTypeId = rhs.getTypeId();
        final int lId = lhs.getTagId();
        final int rId = rhs.getTagId();
        return (lTypeId != rTypeId) ? (lTypeId - rTypeId) : (lId - rId);
    }
}
