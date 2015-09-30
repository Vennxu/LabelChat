package com.ekuater.labelchat.delegate;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.confide.CommentCommand;
import com.ekuater.labelchat.command.confide.ConfideBatchPraiseCommand;
import com.ekuater.labelchat.command.confide.ConfideCommentCommand;
import com.ekuater.labelchat.command.confide.ConfideListCommand;
import com.ekuater.labelchat.command.confide.DeleteCommand;
import com.ekuater.labelchat.command.confide.MyConfideCommand;
import com.ekuater.labelchat.command.confide.PraiseCommand;
import com.ekuater.labelchat.command.confide.PublishCommand;
import com.ekuater.labelchat.command.confide.RoleListCommand;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConfideComment;
import com.ekuater.labelchat.datastruct.ConfideRole;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.confide.PublishContent;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Leo on 2015/4/7.
 *
 * @author LinYong
 */
public class ConfideManager extends BaseManager {

    private static final String TAG = ConfideManager.class.getSimpleName();


    public interface RoleObserver {
        public void onQueryResult(int result, ConfideRole[] roles);
    }

    public interface ConfideObserver {
        public void onQueryResult(int result, Confide[] confides);
    }

    public interface ConfideCommentObserver {
        public void onQueryResult(int result, ConfideComment comment);
    }

    public interface ConfideCommentListObserver {
        public void onQueryResult(int result, Confide confides);
    }

    public interface PublishObserver {
        public void onPublishResult(int result, Confide confide);
    }

    private static ConfideManager sSingleton;
    private SettingHelper mSettingHelper;
    private Map<String, Integer> confideBs;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new ConfideManager(context.getApplicationContext());
        }
    }

    public static ConfideManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private ConfideManager(Context context) {
        super(context);
        mSettingHelper = SettingHelper.getInstance(context);
        initConfideBg(context);
    }

    public void queryRoles(RoleObserver observer) {
        if (observer == null) {
            return;
        }

        RoleListCommand command = new RoleListCommand();
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                RoleObserver observer = (RoleObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    RoleListCommand.CommandResponse cmdResp
                            = new RoleListCommand.CommandResponse(response);
                    ConfideRole[] roles = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        roles = cmdResp.getRoleArray();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, roles);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryConfide(String requestTime, ConfideObserver observer) {
        if (observer == null) {
            return;
        }

        ConfideListCommand command = new ConfideListCommand(getSession(), getUserId());
        command.putParamRequstTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                ConfideObserver observer = (ConfideObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    ConfideListCommand.CommandResponse cmdResp
                            = new ConfideListCommand.CommandResponse(response);
                    Confide[] confides = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        confides = cmdResp.getConfide();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, confides);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryMyConfides(String requestTime, ConfideObserver observer) {
        if (observer == null) {
            return;
        }

        MyConfideCommand command = new MyConfideCommand(getSession(), getUserId());
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                ConfideObserver observer = (ConfideObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    MyConfideCommand.CommandResponse cmdResp
                            = new MyConfideCommand.CommandResponse(response);
                    Confide[] confides = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        confides = cmdResp.getConfides();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, confides);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryConfideComment(String confideId, String requestTime, ConfideCommentListObserver observer) {
        if (observer == null) {
            return;
        }

        ConfideCommentCommand command = new ConfideCommentCommand(getSession(), getUserId());
        command.putParamRequstTime(requestTime);
        command.putParamConfideId(confideId);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                ConfideCommentListObserver observer = (ConfideCommentListObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    ConfideCommentCommand.CommandResponse cmdResp
                            = new ConfideCommentCommand.CommandResponse(response);
                    Confide confides = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        confides = cmdResp.getConfide();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, confides);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void addConfideComment(ConfideComment comment, ConfideCommentObserver observer) {
        if (observer == null) {
            return;
        }

        CommentCommand command = new CommentCommand(getSession(), getUserId());
        command.putParamConfideId(comment.getConfideId());
        command.putParamComment(comment.getComment());
        command.putParamPosition(comment.getPosition());
        command.putParamParentCommentId(comment.getConfideCommentId());
        command.putParamReplyFloor(comment.getReplyFloor());
        command.putParamReplyComment(comment.getReplayComment());
        command.putParamArrayUserId(comment.getUserIds());
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                ConfideCommentObserver observer = (ConfideCommentObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    CommentCommand.CommandResponse cmdResp
                            = new CommentCommand.CommandResponse(response);
                    ConfideComment comment = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        comment = cmdResp.getConfideComent();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, comment);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void publishConfide(PublishContent content, List<String> userIds, PublishObserver observer) {
        PublishCommand command = new PublishCommand(getSession(), getUserId());
        command.putParamPublishContent(content);
        command.putParamArrayUserId(userIds);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                PublishObserver observer = (PublishObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onPublishResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    PublishCommand.CommandResponse cmdResp
                            = new PublishCommand.CommandResponse(response);
                    Confide confide = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        confide = cmdResp.getConfide();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onPublishResult(_ret, confide);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onPublishResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void praiseConfide(String confideId, FunctionCallListener listener) {
        PraiseCommand command = new PraiseCommand(getSession(), getUserId());
        command.putParamConfideId(confideId);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public void batchPraiseConfide(String batchPraise) {
        ConfideBatchPraiseCommand command = new ConfideBatchPraiseCommand(getSession(), getUserId());
        ICommandResponseHandler handler = new CommonResponseHandler(null);
        command.putParamConfideIdArray(batchPraise);
        executeCommand(command, handler);
    }

    public void deleteConfide(String confideId, FunctionCallListener listener) {
        DeleteCommand command = new DeleteCommand(getSession(), getUserId());
        command.putParamConfideId(confideId);
        ICommandResponseHandler handler = new CommonResponseHandler(listener);
        executeCommand(command, handler);
    }

    public List<String> getUserIds(List<ConfideComment> comments) {
        List<String> userIds = new ArrayList<>();
        for (ConfideComment comment : comments) {
            userIds.add(comment.getConfideUserId());
        }
        userIds.remove(mSettingHelper.getAccountUserId());
        HashSet hashSet = new HashSet(userIds);
        userIds.clear();
        userIds.addAll(hashSet);
        return userIds;
    }

    public Map<String, Integer> getConfideBs(){
        return confideBs;
    }

    private void initConfideBg(Context context){
        confideBs = new HashMap<>();
        Resources res = context.getResources();
        final TypedArray key = res.obtainTypedArray(R.array.confide_image_key);
        final TypedArray value = res.obtainTypedArray(R.array.confide_image_value);
        for (int i = 0; i < key.length(); ++i) {
            confideBs.put(key.getString(i), value.getResourceId(i, 0));
        }
        value.recycle();
    }

}
