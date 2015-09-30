package com.ekuater.labelchat.ui.fragment.main;

import java.util.Comparator;

/**
 * Created by Leo on 2015/3/9.
 *
 * @author LinYong
 */
public class PinyinComparator implements Comparator<ContactsListItem.Item> {

    @Override
    public int compare(ContactsListItem.Item o1, ContactsListItem.Item o2) {
        if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
