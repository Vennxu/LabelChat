package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ekuater.httpfileloader.FileLoader;
import com.ekuater.httpfileloader.listener.FileLoadingListener;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandErrorCode;
import com.ekuater.labelchat.command.CommandUrl;
import com.ekuater.labelchat.command.labelstory.LabelStoryAllCommand;
import com.ekuater.labelchat.command.labelstory.LabelStoryCategoryCommand;
import com.ekuater.labelchat.command.labelstory.LabelStoryCommand;
import com.ekuater.labelchat.command.labelstory.LabelStoryCommentCommand;
import com.ekuater.labelchat.command.labelstory.LabelStoryCommentListCommand;
import com.ekuater.labelchat.command.labelstory.LabelStoryDeleteCommand;
import com.ekuater.labelchat.command.labelstory.LabelStoryReComment;
import com.ekuater.labelchat.command.labelstory.MusicCommand;
import com.ekuater.labelchat.command.labelstory.OneStoryDynamicCommand;
import com.ekuater.labelchat.command.labelstory.PostMediaStoryCommand;
import com.ekuater.labelchat.command.labelstory.PraiseLabelStoryCommand;
import com.ekuater.labelchat.command.labelstory.SendLabelStoryCommand;
import com.ekuater.labelchat.command.labelstory.SendLabelStoryImageCommand;
import com.ekuater.labelchat.command.labelstory.SendLetterCommand;
import com.ekuater.labelchat.command.labelstory.ShareStatisticsCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.FollowUser;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.datastruct.LabelStoryChildComment;
import com.ekuater.labelchat.datastruct.LabelStoryComments;
import com.ekuater.labelchat.datastruct.Music;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryCategoryFragment;
import com.ekuater.labelchat.ui.util.FileUtils;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Label on 2015/1/4.
 *
 * @author XuWenXiang
 */
public class LabelStoryManager extends BaseManager {

    private static final String TAG = LabelStoryManager.class.getSimpleName();

    public static final int QUERY_RESULT_SUCCESS = 0;
    public static final int QUERY_RESULT_ILLEGAL_ARGUMENTS = 1;
    public static final int QUERY_RESULT_QUERY_FAILURE = 2;
    public static final int QUERY_RESULT_RESPONSE_DATA_ERROR = 3;
    public static final int QUERY_RESULT_EXIT_PRAISE = 4;

    private static LabelStoryManager sInstance;
    private SettingHelper mSettingHelper;
    private ContactsManager mContactsManager;
    private FollowingManager mFollowManager;

    private static synchronized void initInstance(Context context) {
        if (sInstance == null) {

            sInstance = new LabelStoryManager(context.getApplicationContext());
        }
    }

    public static LabelStoryManager getInstance(Context context) {
        if (sInstance == null) {

            initInstance(context);
        }
        return sInstance;
    }

    public interface LabelStoryQueryObserver {
         void onQueryResult(int result, LabelStory[] labelStories, boolean remaining, int frendsCount);

         void onPraiseQueryResult(int result, boolean remaining);
    }

    public interface LabelStoryCommentQueryObserver {
         void onQueryResult(int result, LabelStoryComments labelStoryComments, boolean remaining);
    }

    public interface LabelStoryCommentListQueryObserver {
         void onQueryResult(int result, LabelStory labelStory, boolean remaining);
    }

    public interface LabelStoryPostImageQueryObserver {
         void onQueryResult(int result, boolean remaining);
    }

    public interface LabelStoryGradeQueryObserver {
         void onQueryResult(int result, boolean remaining);
    }

    public interface LabelStoryReplyCommentQueryObserver {
         void onQueryResult(int result, LabelStoryChildComment childComment, boolean remaining);
    }

    public interface LabelStoryDeleteQueryObserver {
         void onQueryResult(int result, boolean remaining);
    }

    public interface LabelStoryCategoryQueryObserver {
         void onQueryResult(int result, LabelStoryCategory[] categories, boolean remaining);
    }

    public interface LabelStoryLetterQueryObserver {
         void onQueryResult(int result, boolean remaining);
    }

    public interface MusicQueryObserver {
        void onQueryResult(int result, Music[] music, boolean remaining);
    }

    private Context mContext;
    private FileLoader mFileLoader;
    private String mBaseAudioUrl;

    private LabelStoryManager(Context context) {
        super(context);
        mContext = context;
        mFileLoader = FileLoaderHelper.getFileLoader(context);
        mBaseAudioUrl = context.getString(R.string.config_audio_url);
        mSettingHelper = SettingHelper.getInstance(context);
        mContactsManager = ContactsManager.getInstance(context);
        mFollowManager = FollowingManager.getInstance(context);
    }

    public void accessLabelStoryInfo(String labelId, String requestTime, String queryUserId,
                                     LabelStoryQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        LabelStoryCommand command = new LabelStoryCommand(getSession(), getUserId());
        command.putParamLabelId(labelId);
        command.putParamRequestTime(requestTime);
        command.putParamQueryUserId(queryUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryQueryObserver observer = (LabelStoryQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false, 0);
                    return;
                }

                try {
                    LabelStoryCommand.CommandResponse cmdResp
                            = new LabelStoryCommand.CommandResponse(response);
                    LabelStory[] labelStories = null;
                    int frendsCount = 0;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labelStories = cmdResp.getLabelStory();
                        frendsCount = cmdResp.getFrendsCount();
                        L.v(TAG, "accessLabelStoryInfo()" + frendsCount);
                        L.v(TAG, "accessLabelStoryInfo()");
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelStories, false, frendsCount);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false, 0);
            }
        };
        executeCommand(command, handler);
    }

    public void accessLabelStoryAllInfo(String requestTime, boolean isFollowFragment,
                                        LabelStoryQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }
        String url;
        if (isFollowFragment) {
            url = CommandUrl.LABEL_STORY_FOLLOW_LIST;
        } else {
            url = CommandUrl.LABEL_STORY_LATESTSTORY;
        }
        LabelStoryAllCommand command = new LabelStoryAllCommand(getSession(), getUserId(), url);
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryQueryObserver observer = (LabelStoryQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false, 0);
                    return;
                }

                try {
                    LabelStoryAllCommand.CommandResponse cmdResp
                            = new LabelStoryAllCommand.CommandResponse(response);
                    LabelStory[] labelStories = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labelStories = cmdResp.getLabelStory();
                        L.v(TAG, "accessLabelStoryInfo()");
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelStories, false, 0);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false, 0);
            }
        };
        executeCommand(command, handler);
    }

    public void accessMyLabelStoryAllInfo(String queryUserId, String requestTime,
                                          LabelStoryQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        LabelStoryCommand command = new LabelStoryCommand(getSession(), getUserId(), "flags");
        command.putParamRequestTime(requestTime);
        command.putQueryUserId(queryUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryQueryObserver observer = (LabelStoryQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false, 0);
                    return;
                }

                try {
                    LabelStoryCommand.CommandResponse cmdResp
                            = new LabelStoryCommand.CommandResponse(response);
                    LabelStory[] labelStories = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labelStories = cmdResp.getLabelStory();
                        L.v(TAG, "accessLabelStoryInfo()");
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelStories, false, 0);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false, 0);
            }
        };
        executeCommand(command, handler);
    }

    public void visitLatelyOneStoryDynamic(String queryUserId, LabelStoryQueryObserver observer) {
        if (observer == null) {
            return;
        }
        OneStoryDynamicCommand command = new OneStoryDynamicCommand(getSession(), getUserId());
        command.putQueryUserId(queryUserId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryQueryObserver)) {
                    return;
                }

                LabelStoryQueryObserver observer = (LabelStoryQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false, 0);
                    return;
                }

                try {
                    OneStoryDynamicCommand.CommandImageResponse cmdResp
                            = new OneStoryDynamicCommand.CommandImageResponse(response);
                    LabelStory[] labelStories = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labelStories = cmdResp.getLabelStory();
                        L.v(TAG, "accessLabelStoryInfo()");
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelStories, false, 0);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false, 0);
            }
        };
        executeCommand(command, handler);
    }

    public void sendLabelStory(String categoryId, String content, LabelStoryQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        SendLabelStoryCommand command = new SendLabelStoryCommand(getSession(), getUserId());
        command.putParamLabelId(categoryId);
        command.putParamLabelStoryContent(content);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryQueryObserver observer = (LabelStoryQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false, 0);
                    return;
                }

                try {
                    SendLabelStoryCommand.CommandResponse cmdResp
                            = new SendLabelStoryCommand.CommandResponse(response);
                    LabelStory[] labelStories = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labelStories = cmdResp.getLabelStory();
                        L.v(TAG, "sendLabelStory count=%1$d", labelStories != null
                                ? labelStories.length : 0);
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelStories, false, 0);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false, 0);
            }
        };
        executeCommand(command, handler);
    }

    public void praiseLabelStory(String labelStoryId, List<String> userIds, LabelStoryQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        PraiseLabelStoryCommand command = new PraiseLabelStoryCommand(getSession(), getUserId());
        command.putParamLabelStoryId(labelStoryId);
        command.putParamArrayUserId(userIds);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryQueryObserver observer = (LabelStoryQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false, 0);
                    return;
                }

                try {
                    PraiseLabelStoryCommand.CommandResponse cmdResp
                            = new PraiseLabelStoryCommand.CommandResponse(response);

                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        L.v(TAG, "praiseLabelStory request success");
                        _ret = QUERY_RESULT_SUCCESS;
                    } else if (cmdResp.requestExit()) {
                        _ret = QUERY_RESULT_EXIT_PRAISE;
                    }

                    observer.onPraiseQueryResult(_ret, false);
                    L.v(TAG, "praiseLabelStory _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onPraiseQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, false);
            }
        };
        executeCommand(command, handler);
    }

    public void deleteLabelStory(String labelStoryId, LabelStoryDeleteQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        LabelStoryDeleteCommand command = new LabelStoryDeleteCommand(getSession(), getUserId());
        command.putParamLabelStoryId(labelStoryId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryDeleteQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryDeleteQueryObserver observer = (LabelStoryDeleteQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, false);
                    return;
                }

                try {
                    LabelStoryDeleteCommand.CommandResponse cmdResp
                            = new LabelStoryDeleteCommand.CommandResponse(response);

                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        L.v(TAG, "praiseLabelStory request success");
                        _ret = QUERY_RESULT_SUCCESS;
                    }
                    observer.onQueryResult(_ret, false);
                    L.v(TAG, "praiseLabelStory _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, false);
            }
        };
        executeCommand(command, handler);
    }

    public void praiseLabelStoryComments(String labelStoryCommentId, LabelStoryQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        PraiseLabelStoryCommand command = new PraiseLabelStoryCommand(getSession(), getUserId(), PraiseLabelStoryCommand.COMENTS_URL);
        command.putParamLabelStoryCommentId(labelStoryCommentId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryQueryObserver observer = (LabelStoryQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false, 0);
                    return;
                }

                try {
                    PraiseLabelStoryCommand.CommandResponse cmdResp
                            = new PraiseLabelStoryCommand.CommandResponse(response);

                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        L.v(TAG, "praiseLabelStory request success");
                        _ret = QUERY_RESULT_SUCCESS;
                    } else if (cmdResp.requestExit()) {
                        _ret = QUERY_RESULT_EXIT_PRAISE;
                    }

                    observer.onPraiseQueryResult(_ret, false);
                    L.v(TAG, "praiseLabelStory _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onPraiseQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, false);
            }
        };
        executeCommand(command, handler);
    }

    public void commentLabelStory(LabelStoryComments comment, LabelStoryCommentQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }

        LabelStoryCommentCommand command = new LabelStoryCommentCommand(getSession(), getUserId());
        command.putParamLabelStoryId(comment.getmLabelStoryId());
        command.putParamCommentContent(comment.getmStoryComment());
        command.putParamParentCommentId(comment.getmParentCommentId());
        command.putParamReplyNickName(comment.getmReplyNickName());
        command.putParamReplyUserId(comment.getmReplyUserId());
        command.putParamArrayUserId(comment.getmArrayUserId());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryCommentQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryCommentQueryObserver observer = (LabelStoryCommentQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    LabelStoryCommentCommand.CommandResponse cmdResp
                            = new LabelStoryCommentCommand.CommandResponse(response);
                    LabelStoryComments labelStoryComments = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        L.v(TAG, "praiseLabelStory request success");
                        labelStoryComments = cmdResp.getLabelStoryComment();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelStoryComments, false);
                    L.v(TAG, "praiseLabelStory _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    public void replyCommentLabelStory(String labelStoryId, LabelStoryComments comments, LabelStoryReplyCommentQueryObserver observer) {
        if (observer == null) {
            // no observer, so just miss it, do not care about it.
            return;
        }
        LabelStoryReComment command = new LabelStoryReComment(getSession(), getUserId());
        command.putParamLabelStoryId(labelStoryId);
        command.putParamParentCommentId(comments.getmParentCommentId());
        command.putParamStoryComment(comments.getmStoryComment());
        command.putParamReplyNickName(comments.getmReplyNickName());
        command.putParamReplyUserId(comments.getmReplyUserId());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {

            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryReplyCommentQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }

                LabelStoryReplyCommentQueryObserver observer = (LabelStoryReplyCommentQueryObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                    return;
                }

                try {
                    LabelStoryReComment.CommandResponse cmdResp
                            = new LabelStoryReComment.CommandResponse(response);
                    LabelStoryChildComment childComment = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        L.v(TAG, "praiseLabelStory request success");
                        childComment = cmdResp.toChildComment();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, childComment, false);
                    L.v(TAG, "praiseLabelStory _ret=%1$d", _ret);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    public void commentListLabelStory(final String labelStoryId, String requestTime, final LabelStoryCommentListQueryObserver observer) {
        if (observer == null) {
            return;
        }
        LabelStoryCommentListCommand labelStoryCommentListCommand = new LabelStoryCommentListCommand(getSession(), getUserId());
        labelStoryCommentListCommand.putParamLabelStoryId(labelStoryId);
        labelStoryCommentListCommand.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryCommentListQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }
                LabelStoryCommentListQueryObserver observer = (LabelStoryCommentListQueryObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                }
                try {
                    LabelStoryCommentListCommand.CommandResponse cmdResp
                            = new LabelStoryCommentListCommand.CommandResponse(response);
                    LabelStory labelStory = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        labelStory = cmdResp.getLabelStory();
                        Log.d("labelStorys", labelStory.toString());
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, labelStory, false);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);

            }
        };
        executeCommand(labelStoryCommentListCommand, handler);
    }

    public void sendImageLabelStory(String userIds, File[] photos, String content, String labelId,
                                    PostStoryListener listener)
            throws FileNotFoundException {
        if (photos == null || photos.length <= 0) {
            throw new NullPointerException("image story no photos");
        }

        SendLabelStoryImageCommand command = new SendLabelStoryImageCommand(
                getSession(), getUserId());
        if (!TextUtils.isEmpty(content)) {
            command.putParamContent(content);
        }
        if (!TextUtils.isEmpty(labelId)) {
            command.putParamLabelId(labelId);
        }
        command.putParamUserId(userIds);
        for (File photo : photos) {
            command.addPhoto(photo);
        }
        command.putParamType(LabelStory.TYPE_TXT_IMG);
        IUploadResponseHandler handler = new UploadMediaStoryHandler(listener);
        mCoreService.doUpload(command, handler);
    }

    public void sendBanknoteStory(File banknoteFile, String content, PostStoryListener listener)
            throws FileNotFoundException {
        SendLabelStoryImageCommand command = new SendLabelStoryImageCommand(
                getSession(), getUserId());
        if (!TextUtils.isEmpty(content)) {
            command.putParamContent(content);
        }
        command.addPhoto(banknoteFile);
        command.putParamType(LabelStory.TYPE_BANKNOTE);
        IUploadResponseHandler handler = new UploadMediaStoryHandler(listener);
        mCoreService.doUpload(command, handler);
    }

    public void categoryListLabelStory(LabelStoryCategoryQueryObserver observer) {
        if (observer == null) {
            return;
        }
        LabelStoryCategoryCommand categoryCommand = new LabelStoryCategoryCommand();
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryCategoryQueryObserver)) {
                    // no available observer, so do not need to care about the
                    // response result.
                    return;
                }
                LabelStoryCategoryQueryObserver observer = (LabelStoryCategoryQueryObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                }
                try {
                    LabelStoryCategoryCommand.CommandResponse cmdResp
                            = new LabelStoryCategoryCommand.CommandResponse(response);
                    LabelStoryCategory[] catetories = null;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        keepCategory(response, LabelStoryCategoryFragment.CATEGORY_TXT);
                        catetories = cmdResp.getCatetory();
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, catetories, false);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);

            }
        };
        executeCommand(categoryCommand, handler);
    }

    public void letterLabelStory(String labelStoryId, String strangerUserId, String message, LabelStoryLetterQueryObserver observer) {
        if (observer == null) {
            return;
        }
        SendLetterCommand letterCommand = new SendLetterCommand(getSession(), getUserId());
        letterCommand.putParamStrangerUserId(strangerUserId);
        letterCommand.putParamMessage(message);
        letterCommand.putParamLabelStoryId(labelStoryId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof LabelStoryLetterQueryObserver)) {
                    return;
                }
                LabelStoryLetterQueryObserver observer = (LabelStoryLetterQueryObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QUERY_RESULT_QUERY_FAILURE, false);
                }
                try {
                    SendLetterCommand.CommandResponse cmdResp
                            = new SendLetterCommand.CommandResponse(response);
                    int _ret = QUERY_RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        _ret = QUERY_RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, false);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, false);

            }
        };
        executeCommand(letterCommand, handler);
    }

    public void keepCategory(String fileString, String fileName) {
        String category = FileUtils.readFileData(fileName, mContext);
        if (!TextUtils.isEmpty(category)) {
            FileUtils.deletFileData(fileName);
        }
        FileUtils.writeFileData(fileName, fileString, mContext);
    }

    public void doShareStatistics(String labelStoryId, String sharePlatform) {
        ShareStatisticsCommand command = new ShareStatisticsCommand(getSession(), getUserId());
        command.putParamLabelStoryId(labelStoryId);
        command.putParamSharePlatform(sharePlatform);
        ICommandResponseHandler handler = new ICommandResponseHandler() {
            @Override
            public void onResponse(int result, String response) {
            }
        };
        executeCommand(command, handler);
    }

    public void postAudioStory(String userIds, String content, String categoryId, File audioFile,
                               long duration, PostStoryListener listener)
            throws FileNotFoundException {
        postMediaStory(userIds, content, categoryId, LabelStory.TYPE_AUDIO, audioFile, null,duration, listener);
    }

    public void postOnlineAudioStory(String userIds, String content, String imageUrl, File audioFile, String url,
                               long duration, PostStoryListener listener)
            throws FileNotFoundException {
        postMediaStory(userIds, content, imageUrl, LabelStory.TYPE_ONLINEAUDIO, audioFile, url,duration, listener);
    }

    public void postVideoStory(String userIds, String content, String categoryId, File videoFile,
                               long duration, PostStoryListener listener)
            throws FileNotFoundException {
        postMediaStory(userIds, content, categoryId, LabelStory.TYPE_VIDEO, videoFile, null, duration, listener);
    }

    public void postMediaStory(String userIds, String content, String imageUrl, String type, File mediaFile, String url,
                               long duration, PostStoryListener listener)
            throws FileNotFoundException {
        if (TextUtils.isEmpty(type)) {
            throw new IllegalArgumentException("postMediaStory, empty type");
        }
        PostMediaStoryCommand command = new PostMediaStoryCommand(getSession(), getUserId());
        if (!LabelStory.TYPE_ONLINEAUDIO.equals(type)) {
             if (mediaFile == null) {
                 throw new IllegalArgumentException("postMediaStory, null media file");
             }
            command.setMediaFile(mediaFile);
        }
        IUploadResponseHandler handler = new UploadMediaStoryHandler(listener);
        if (!TextUtils.isEmpty(content)) {
            command.putParamContent(content);
        }
        command.putParamType(type);
        command.putParamImageUrl(imageUrl);
        command.putParamMediaUrl(url);
        command.putParamDuration(duration);
        mCoreService.doUpload(command, handler);
    }

    public void loadAudioFile(String url, FileLoadingListener loadingListener) {
        loadMediaFile(getAudioUrl(url), loadingListener);
    }

    public void loadOnlineAudioFile(String url, FileLoadingListener loadingListener) {
        loadMediaFile(url, loadingListener);
    }

    private void loadMediaFile(String fileUrl, FileLoadingListener loadingListener) {
        mFileLoader.loadFile(fileUrl, loadingListener);
    }

    private String getAudioUrl(String url) {
        return mBaseAudioUrl + parseUrl(url);
    }

    private String parseUrl(String url) {
        final int idx = url.lastIndexOf("/");
        return (idx >= 0 && idx < (url.length() - 1)) ? url.substring(idx + 1) : url;
    }

    private static class UploadMediaStoryHandler implements IUploadResponseHandler {

        private final PostStoryListener listener;
        private boolean uploadFinish = false;

        public UploadMediaStoryHandler(PostStoryListener listener) {
            this.listener = listener;
        }

        @Override
        public void onProgress(long bytesWritten, long totalSize) {
            uploadFinish = bytesWritten >= totalSize;
        }

        @Override
        public void onResponse(int result, String response) {
            int callResult = QueryResult.RESULT_UNKNOWN_ERROR;
            int errorCode = CommandErrorCode.EXECUTE_FAILED;
            String errorDesc = null;
            LabelStory[] stories = null;

            L.v(TAG, "onResponse(), response=" + response);

            if (this.listener == null) {
                return;
            }

            if (result == ConstantCode.EXECUTE_RESULT_SUCCESS) {
                try {
                    PostMediaStoryCommand.CommandResponse cmdResp
                            = new PostMediaStoryCommand.CommandResponse(response);

                    errorCode = cmdResp.getErrorCode();
                    errorDesc = cmdResp.getErrorDesc();
                    callResult = QueryResult.RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        stories = cmdResp.getStories();
                        callResult = QueryResult.RESULT_SUCCESS;
                    }
                } catch (JSONException e) {
                    L.w(TAG, e);
                    callResult = QueryResult.RESULT_RESPONSE_DATA_ERROR;
                }
            } else if (result == ConstantCode.EXECUTE_RESULT_NETWORK_ERROR && uploadFinish) {
                callResult = QueryResult.RESULT_SUCCESS;
                errorCode = CommandErrorCode.REQUEST_SUCCESS;
            }
            this.listener.onPostResult(callResult, errorCode, errorDesc, stories);
        }
    }

    public void musicListQuery(String musicName, int requestTime, MusicQueryObserver observer) {
        if (observer == null) {
            return;
        }
        MusicCommand command = new MusicCommand();
        command.putParamMusicName(musicName);
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                if (mObj == null || !(mObj instanceof MusicQueryObserver)) {
                    return;
                }
                MusicQueryObserver queryObserver = (MusicQueryObserver) mObj;
                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    queryObserver.onQueryResult(QUERY_RESULT_QUERY_FAILURE, null, false);
                }
                try {
                    MusicCommand.CommandResponse cmdResp = new MusicCommand.CommandResponse(response);
                    Music[] musics = null;
                    boolean remaining = false;
                    int _ret = QUERY_RESULT_QUERY_FAILURE;
                    if (cmdResp.requestSuccess()) {
                        musics = cmdResp.getMusic();
                        remaining = (musics != null) && (musics.length >= 6);
                        _ret = QUERY_RESULT_SUCCESS;
                    }
                    queryObserver.onQueryResult(_ret, musics, remaining);
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                queryObserver.onQueryResult(QUERY_RESULT_RESPONSE_DATA_ERROR, null, false);
            }
        };
        executeCommand(command, handler);
    }

    public List<String> getAllUserIds(LabelStory labelStory, ArrayList<LabelStoryComments> labelStoryCommentses) {
        List<String> allUserId = new ArrayList<>();
        UserContact[] userContacts = mContactsManager.getAllUserContact();
        FollowUser[] followUsers = mFollowManager.getAllFollowerUser();
        List<String> userContactId = new ArrayList<>();
        if (userContacts != null && userContacts.length > 0) {
            for (UserContact userContact : userContacts) {
                userContactId.add(userContact.getUserId());
            }
        }

        List<String> followingUserId = new ArrayList<>();
        if (followUsers != null && followUsers.length > 0) {
            for (FollowUser followingUser : followUsers) {
                followingUserId.add(followingUser.getUserId());
            }
        }

        List<String> commentUserId = new ArrayList<>();
        if (labelStoryCommentses != null && labelStoryCommentses.size() > 0) {
            for (LabelStoryComments commentUser : labelStoryCommentses) {
                commentUserId.add(commentUser.getmStranger().getUserId());
            }
        }

        UserPraise[] praiseUsers = labelStory.getUserPraise();
        List<String> praiseUserId = new ArrayList<>();
        if (praiseUsers != null && praiseUsers.length > 0) {
            for (UserPraise praiseUser : praiseUsers) {
                praiseUserId.add(praiseUser.getmPraiseUserId());
            }
        }

        praiseUserId.removeAll(commentUserId);
        praiseUserId.addAll(commentUserId);
        HashSet hs = new HashSet(praiseUserId);
        praiseUserId.clear();
        praiseUserId.addAll(hs);

        List<String> removeId = new ArrayList<>();
        removeId.add(mSettingHelper.getAccountUserId());
        removeId.add(labelStory.getAuthorUserId());
        praiseUserId.removeAll(removeId);
        String[] interactionUserId = new String[praiseUserId.size()];
        for (int i = 0; i < praiseUserId.size(); i++) {
            interactionUserId[i] = praiseUserId.get(i);
        }
        UserContact[] friendUserId = mContactsManager.batchQueryUserContact(interactionUserId);
        FollowUser[] followerUserId = mFollowManager.batchQueryFollowerUser(interactionUserId);
        List<String> friendId = new ArrayList<>();
        List<String> fansId = new ArrayList<>();
        if (friendUserId != null && friendUserId.length > 0) {
            for (UserContact userId : friendUserId) {
                friendId.add(userId.getUserId());
            }
        }
        if (followerUserId != null && followerUserId.length > 0) {
            for (FollowUser userId : followerUserId) {
                fansId.add(userId.getUserId());
            }
        }
        allUserId.addAll(friendId);
        allUserId.addAll(fansId);
        HashSet hashSet = new HashSet(allUserId);
        praiseUserId.clear();
        praiseUserId.addAll(hashSet);
        return allUserId;
    }

}
