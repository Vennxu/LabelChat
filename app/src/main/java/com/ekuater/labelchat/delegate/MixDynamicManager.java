package com.ekuater.labelchat.delegate;

import android.content.Context;

import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.command.mixdynamic.FilterBuilder;
import com.ekuater.labelchat.command.mixdynamic.GlobalListCommand;
import com.ekuater.labelchat.command.mixdynamic.MyOwnListCommand;
import com.ekuater.labelchat.command.mixdynamic.RelatedListCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.util.L;

import org.json.JSONException;

/**
 * Created by Leo on 2015/4/17.
 *
 * @author LinYong
 */
public class MixDynamicManager extends BaseManager {

    private static final String TAG = MixDynamicManager.class.getSimpleName();

    public interface DynamicObserver {
        public void onQueryResult(int result, DynamicWrapper[] wrappers);
    }

    private static MixDynamicManager sSingleton;

    private static synchronized void initInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new MixDynamicManager(context.getApplicationContext());
        }
    }

    public static MixDynamicManager getInstance(Context context) {
        if (sSingleton == null) {
            initInstance(context);
        }
        return sSingleton;
    }

    private final FilterBuilder mSupportedFilterBuilder;

    private MixDynamicManager(Context context) {
        super(context);
        mSupportedFilterBuilder = newSupportedFilter();
    }

    private FilterBuilder newSupportedFilter() {
        FilterBuilder filterBuilder = new FilterBuilder();
        filterBuilder.addType(CommandFields.Dynamic.CONTENT_TYPE_STORY,
                new String[]{
                        LabelStory.TYPE_TXT_IMG,
                        LabelStory.TYPE_AUDIO,
                        LabelStory.TYPE_BANKNOTE,
                        LabelStory.TYPE_ONLINEAUDIO
                });
        filterBuilder.addType(CommandFields.Dynamic.CONTENT_TYPE_CONFIDE);
        return filterBuilder;
    }

    public void queryMixDynamic(String requestTime, DynamicObserver observer) {
        if (observer == null) {
            return;
        }

        GlobalListCommand command = new GlobalListCommand(getSession(), getUserId());
        command.putParamFilter(mSupportedFilterBuilder);
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                DynamicObserver observer = (DynamicObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    GlobalListCommand.CommandResponse cmdResp
                            = new GlobalListCommand.CommandResponse(response);
                    DynamicWrapper[] wrappers = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        wrappers = cmdResp.getWrappers();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, wrappers);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryRelatedDynamic(String requestTime, DynamicObserver observer) {
        if (observer == null) {
            return;
        }

        RelatedListCommand command = new RelatedListCommand(getSession(), getUserId());
        command.putParamFilter(mSupportedFilterBuilder);
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                DynamicObserver observer = (DynamicObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    RelatedListCommand.CommandResponse cmdResp
                            = new RelatedListCommand.CommandResponse(response);
                    DynamicWrapper[] wrappers = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        wrappers = cmdResp.getWrappers();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, wrappers);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }

    public void queryMyOwnDynamic(String requestTime, DynamicObserver observer) {
        if (observer == null) {
            return;
        }

        MyOwnListCommand command = new MyOwnListCommand(getSession(), getUserId());
        command.putParamFilter(mSupportedFilterBuilder);
        command.putParamRequestTime(requestTime);
        ICommandResponseHandler handler = new WithObjCmdRespHandler(observer) {
            @Override
            public void onResponse(int result, String response) {
                DynamicObserver observer = (DynamicObserver) mObj;

                if (result != ConstantCode.EXECUTE_RESULT_SUCCESS) {
                    observer.onQueryResult(QueryResult.RESULT_QUERY_FAILURE, null);
                    return;
                }

                try {
                    MyOwnListCommand.CommandResponse cmdResp
                            = new MyOwnListCommand.CommandResponse(response);
                    DynamicWrapper[] wrappers = null;
                    int _ret = QueryResult.RESULT_QUERY_FAILURE;

                    if (cmdResp.requestSuccess()) {
                        wrappers = cmdResp.getWrappers();
                        _ret = QueryResult.RESULT_SUCCESS;
                    }

                    observer.onQueryResult(_ret, wrappers);
                    return;
                } catch (JSONException e) {
                    L.w(TAG, e);
                }
                observer.onQueryResult(QueryResult.RESULT_RESPONSE_DATA_ERROR, null);
            }
        };
        executeCommand(command, handler);
    }
}
