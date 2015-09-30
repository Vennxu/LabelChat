package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.content.Context;
import android.os.Bundle;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.datastruct.mixdynamic.WrapperUtils;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.QueryResult;
import com.ekuater.labelchat.util.L;

import java.util.ArrayList;

/**
 * Created by Leo on 2015/4/23.
 *
 * @author LinYong
 */
public class UserOwnConfig extends DynamicConfig {

    private static final String TAG = UserOwnConfig.class.getSimpleName();

    private final Context context;
    private final String userId;
    private final Stranger userStranger;
    private final LabelStoryManager storyManager;

    public UserOwnConfig(Context context, Bundle args) {
        super(context, args);
        this.context = context;
        this.userId = args.getString(MixDynamicArgs.ARGS_USER_ID);
        this.userStranger = args.getParcelable(MixDynamicArgs.ARGS_USER_STRANGER);
        this.storyManager = LabelStoryManager.getInstance(context);
    }

    @Override
    public boolean needShowTitle() {
        return true;
    }

    @Override
    public String getTitle() {
        int resId = userStranger.getSex() == ConstantCode.USER_SEX_FEMALE
                ? R.string.her_story : R.string.he_story;
        return context.getString(resId);
    }

    @Override
    public String getNoDataTip() {
        int resId = userStranger.getSex() == ConstantCode.USER_SEX_FEMALE
                ? R.string.her_story_null : R.string.he_story_null;
        return context.getString(resId);
    }

    @Override
    public void queryMixDynamic(int requestTime, QueryListener listener) {
        storyManager.accessMyLabelStoryAllInfo(userId, String.valueOf(requestTime),
                new StoryQueryObserver(userStranger, listener));
    }

    private static DynamicWrapper[] toWrappers(LabelStory[] stories, Stranger stranger) {
        if (stories == null) {
            return null;
        }

        ArrayList<DynamicWrapper> list = new ArrayList<>();
        for (LabelStory story : stories) {
            try {
                if (story.getStranger() == null) {
                    story.setStranger(stranger);
                }
                DynamicWrapper wrapper = WrapperUtils.fromStory(story);
                if (wrapper != null) {
                    list.add(wrapper);
                }
            } catch (Exception e) {
                L.w(TAG, e);
            }
        }

        final int size = list.size();
        return (size > 0) ? list.toArray(new DynamicWrapper[size]) : null;
    }

    private static class StoryQueryObserver implements LabelStoryManager.LabelStoryQueryObserver {

        private final Stranger stranger;
        private final QueryListener listener;

        public StoryQueryObserver(Stranger stranger, QueryListener listener) {
            this.stranger = stranger;
            this.listener = listener;
        }

        @Override
        public void onQueryResult(int result, LabelStory[] labelStories,
                                  boolean remaining, int frendsCount) {
            if (result == LabelStoryManager.QUERY_RESULT_SUCCESS) {
                listener.onQueryResult(QueryResult.RESULT_SUCCESS,
                        toWrappers(labelStories, stranger));
            } else {
                listener.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
            }
        }

        @Override
        public void onPraiseQueryResult(int result, boolean remaining) {
        }
    }
}
