package com.ekuater.labelchat.ui.fragment.labelstory;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.ConfideComment;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.datastruct.SystemPushType;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.delegate.FollowingManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.ShareContent;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Label on 2015/1/27.
 */
public class LabelStoryUtils {
    public static final int COMMENT_REQUEST_CODE = 1101;
    public static final int PRAISE_REQUEST_CODE = 1102;
    public static final int PRAISE_REQUEST_EXIT_CODE = 1103;
    public static final int COMMENT_PRAISE_REQUEST_CODE = 1104;
    public static final int FOLLOWING_REQUEST_CODE = 1105;
    public static final int LETTER_REQUEST_CODE = 1106;
    public static final int RESULT_LABEL_STORY_CODE = 1107;
    public static final int LABEL_STORY_MY_DELETE = 1108;
    public static final int CONFIDE_COMMENT_CODE = 1109;
    public static final int REFRESH_DATA = 0;
    public static final int LOADING_DADA = 1;
    public static final int ALL = 2201;
    public static final int FOLLOW = 2202;
    public static final int MY = 2203;
    public static final int ONE = 2204;
    public static final int MIX = 2205;
    public static final int STRANGERINFO = 2205;
    public static final String SHOW_PHOTO_URL = "show_photo_url";
    public static final String CATEGORY = "category";
    public static final String LABEL_STORY_USER_ID = "user_id";
    public static final String LABEL_STORY_SHOW = "label_story_show";
    public static final String LABEL_STORY_TITLE_SHOW = "label_story_title_show";
    public static final String LABEL_ISSHOW_BUNDING = "label_isshow_bunding";
    public static final String CATEGORY_NAME = "category_name";
    public static final String STRANGER = "stranger";
    public static final String TAG = "tag";
    public static final String CONTENT = "content";
    public static final String IS_GROUP = "is_group";
    public static final String IS_PRAISE = "is_praise";
    public static final String IS_COMMENT = "is_comment";
    public static final String IS_KEYBROAD = "is_keybroad";
    private ContentSharer mContentSharer;
    private LabelStoryManager mLabelStoryManager;
    private FollowingManager mFollowingManager;
    private ConfideManager mConfideManager;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private Context mContext;
    private Fragment fragment;

    public LabelStoryUtils(Fragment fragment, ContentSharer contentSharer, Handler handler) {
        this.fragment = fragment;
        mContext = fragment.getActivity();
        mContentSharer = contentSharer;
        mLabelStoryManager = LabelStoryManager.getInstance(mContext);
        mFollowingManager = FollowingManager.getInstance(mContext);
        mConfideManager = ConfideManager.getInstance(mContext);
        mHandler = handler;
    }

    public static boolean isMyLabel(String labelId, UserLabelManager userLabelManager) {
        UserLabel[] userLabels = userLabelManager.getAllLabels();
        if (userLabels != null && userLabels.length > 0) {
            for (UserLabel userLabel : userLabels) {
                if (userLabel.getId().equals(labelId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void insertSystemPush(PushMessageManager pushMessageManager, Stranger stranger, String message) {
        SystemPush systemPush = new SystemPush();
        systemPush.setType(SystemPushType.TYPE_PRIVATE_LETTER);
        systemPush.setTime(System.currentTimeMillis());
        systemPush.setState(SystemPush.STATE_PROCESSED);
        systemPush.setFlag(stranger.getUserId());
        systemPush.setContent(strangerToJson(stranger, message));
        pushMessageManager.insertPushMessage(systemPush);
    }

    public static String strangerToJson(Stranger stranger, String message) {
        String jsonresult = "";
        JSONObject object = new JSONObject();
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put(CommandFields.User.USER_ID, stranger.getUserId());
            jsonObj.put(CommandFields.User.NICKNAME, stranger.getNickname());
            jsonObj.put(CommandFields.User.LABEL_CODE, stranger.getLabelCode());
            jsonObj.put(CommandFields.User.AVATAR, stranger.getAvatar());
            jsonObj.put(CommandFields.User.AVATAR_THUMB, stranger.getAvatarThumb());

            object.put(CommandFields.User.USER, jsonObj);
            object.put(CommandFields.StoryLabel.MESSAGE, message);
            object.put(CommandFields.StoryLabel.TAG, 1);
            jsonresult = object.toString();
        } catch (JSONException e) {
            return null;
        }
        return jsonresult;
    }

    public void shareContent(ShareContent content) {
        mContentSharer.setShareContent(content);
        mContentSharer.openSharePanel();
    }

    public void comment(LabelStoryComments comment) {
        LabelStoryManager.LabelStoryCommentQueryObserver observer = new LabelStoryManager.LabelStoryCommentQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStoryComments labelStoryComments, boolean remaining) {
                Message message = Message.obtain(mHandler, COMMENT_REQUEST_CODE, result, 0, labelStoryComments);
                mHandler.sendMessage(message);
            }
        };
        mLabelStoryManager.commentLabelStory(comment, observer);
    }

    public void praise(String labelStoryId, final int position, final Object extra) {
        LabelStoryManager.LabelStoryQueryObserver observer = new LabelStoryManager.LabelStoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStory[] labelStories, boolean remaining, int frendsCount) {

            }

            @Override
            public void onPraiseQueryResult(int result, boolean remaining) {
                Message msg = Message.obtain(mHandler, PRAISE_REQUEST_CODE, result, position, extra);
                mHandler.sendMessage(msg);
            }
        };
        //TODO
        mLabelStoryManager.praiseLabelStory(labelStoryId, null, observer);
    }

    public void praise(String labelStoryId, final int position) {
        praise(labelStoryId, position, null);
    }

    public void sendLetter(String labelStoryId, String strangerUserId, String message, final int position) {

        LabelStoryManager.LabelStoryLetterQueryObserver observer = new LabelStoryManager.LabelStoryLetterQueryObserver() {
            @Override
            public void onQueryResult(int result, boolean remaining) {
                Message message = Message.obtain(mHandler, LETTER_REQUEST_CODE, result, position);
                mHandler.sendMessage(message);
            }
        };
        mLabelStoryManager.letterLabelStory(labelStoryId, strangerUserId, message, observer);
    }

    public static class FollowUserResult {

        public final int result;
        public final int position;
        public final int followCount;
        public final Object extra;

        public FollowUserResult(int result, int position, int followCount, Object extra) {
            this.result = result;
            this.position = position;
            this.followCount = followCount;
            this.extra = extra;
        }
    }

    public void following(String followUserId, final int position, final Object extra) {
        FollowingManager.FollowingCountQueryObserver observer = new FollowingManager.FollowingCountQueryObserver() {
            @Override
            public void onQueryResult(int result, int followCount, boolean remaining) {
                Message message = Message.obtain(mHandler, FOLLOWING_REQUEST_CODE, 0, 0, new FollowUserResult(result, position, followCount, extra));
                mHandler.sendMessage(message);
            }
        };
        mFollowingManager.followingUserCountInfo(followUserId, observer);
    }

    public void following(String followUserId, int position) {
        following(followUserId, position, null);
    }

    public List<String> getUserIds(LabelStory stories, LabelStoryComments[] comments) {
        ArrayList<LabelStoryComments> arrayList = null;
        if (comments != null) {
            arrayList = new ArrayList<>();
            List<LabelStoryComments> list = Arrays.asList(comments);
            arrayList.addAll(list);
        }
        return mLabelStoryManager.getAllUserIds(stories, arrayList);
    }

    public void addConfideComment(ConfideComment confideComment) {
        mConfideManager.addConfideComment(confideComment, new ConfideManager.ConfideCommentObserver() {
            @Override
            public void onQueryResult(int result, ConfideComment comment) {
                mHandler.obtainMessage(CONFIDE_COMMENT_CODE, result, 0, comment).sendToTarget();
            }
        });
    }

    private SimpleProgressDialog mProgressDialog;


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(fragment.getFragmentManager(), "SimpleProgressDialog");
        }
    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    public static int getCommentNull(Context context) {

        final TypedArray ar = context.getResources().obtainTypedArray(
                R.array.comment_image);
        final int length = ar.length();
        final int[] array = new int[length];

        for (int i = 0; i < length; ++i) {
            array[i] = ar.getResourceId(i, 0);
        }

        int random = new Random().nextInt(length);
        Log.d("random", random + "");
        ar.recycle();
        Log.d("random", array.length + "");
        return array[random];
    }
}
