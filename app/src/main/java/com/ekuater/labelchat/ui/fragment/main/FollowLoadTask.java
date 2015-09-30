package com.ekuater.labelchat.ui.fragment.main;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.ui.util.CharacterParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Leo on 2015/4/9.
 *
 * @author LinYong
 */
public abstract class FollowLoadTask extends AsyncTask<Void, Void, List<ContactsListItem.Item>> {

    private final FollowLoadListener mListener;
    private final CharacterParser mCharacterParser;
    private final PinyinComparator mPinyinComparator;

    public FollowLoadTask(FollowLoadListener listener) {
        mListener = listener;
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();
    }

    public FollowLoadTask executeInThreadPool() {
        executeOnExecutor(THREAD_POOL_EXECUTOR, (Void) null);
        return this;
    }

    @Override
    protected List<ContactsListItem.Item> doInBackground(Void... params) {
        final List<ContactsListItem.Item> itemList = new ArrayList<>();
        final FollowUser[] followUsers = getAllFollowUser();

        if (followUsers != null) {
            for (FollowUser followUser : followUsers) {
                final String name = followUser.getNickname();
                final String sortLetter;

                if (!TextUtils.isEmpty(name)) {
                    final String pinyin = mCharacterParser.getSelling(name);
                    final String firstLetter = pinyin.substring(0, 1).toUpperCase();
                    sortLetter = firstLetter.matches("[A-Z]") ? firstLetter.toUpperCase() : "#";
                } else {
                    sortLetter = "#";
                }

                ContactsListItem.Item item = newFollowItem(followUser, sortLetter);
                if (item != null) {
                    itemList.add(item);
                }
            }
        }

        Collections.sort(itemList, mPinyinComparator);
        return itemList;
    }

    @Override
    protected void onPostExecute(List<ContactsListItem.Item> items) {
        if (mListener != null) {
            mListener.onLoadDone(items);
        }
    }

    protected abstract FollowUser[] getAllFollowUser();

    protected abstract ContactsListItem.Item newFollowItem(
            FollowUser followUser, String sortLetter);
}
