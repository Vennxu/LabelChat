package com.ekuater.labelchat.ui.fragment.labelstory;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.ShowToast;

/**
 * Created by Administrator on 2015/5/9.
 *
 * @author FanChong
 */
public class DynamicDetailsHelper {
    private static final int MSG_LOAD_DYNAMIC_DETAILS = 101;
    private static final String QUERY_COUNT = "1";
    private Context context;
    private LabelStoryManager labelStoryManager;
    private DynamicArguments dynamicArguments;
    private SimpleProgressHelper simpleProgressHelper;
    private Handler handler;

    public DynamicDetailsHelper(Context context, SimpleProgressHelper simpleProgressHelper, DynamicArguments arguments) {
        this.context = context;
        this.simpleProgressHelper = simpleProgressHelper;
        this.dynamicArguments = arguments;
        this.labelStoryManager = LabelStoryManager.getInstance(context);
        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handlerQueryDynamicDetailsResult(msg.arg1, (LabelStory) msg.obj);
            }
        };

    }

    public void loadDynamicDetails() {
        simpleProgressHelper.show();
        labelStoryManager.commentListLabelStory(dynamicArguments.getLabelStory().getLabelStoryId(),
                QUERY_COUNT, new LabelStoryManager.LabelStoryCommentListQueryObserver() {
                    @Override
                    public void onQueryResult(int result, LabelStory labelStory, boolean remaining) {
                        Message message = handler.obtainMessage(MSG_LOAD_DYNAMIC_DETAILS, result, 0, labelStory);
                        handler.sendMessage(message);
                    }
                });
    }

    public void handlerQueryDynamicDetailsResult(int result, LabelStory labelStory) {
        switch (result) {
            case LabelStoryManager.QUERY_RESULT_SUCCESS:
                if (labelStory != null) {
                    simpleProgressHelper.dismiss();
                    dynamicArguments.setLabelStory(labelStory);
                    UILauncher.launchFragmentLabelStoryDetaileUI(context, dynamicArguments);
                }
                break;
            case LabelStoryManager.QUERY_RESULT_QUERY_FAILURE:
                simpleProgressHelper.dismiss();
                ShowToast.makeText(context, R.drawable.emoji_cry, context.getResources().getString(R.string.dynamic_data_not_exist)).show();
                break;
            default:
                simpleProgressHelper.dismiss();
                ShowToast.makeText(context, R.drawable.emoji_cry, context.getResources().getString(R.string.request_fail)).show();
                break;
        }

    }

}
