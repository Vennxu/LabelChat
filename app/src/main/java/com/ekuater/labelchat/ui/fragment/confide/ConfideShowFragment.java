package com.ekuater.labelchat.ui.fragment.confide;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.confide.ConfidePublishEvent;
import com.ekuater.labelchat.delegate.ConfideManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.fragment.labelstory.CustomListView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/4/7.
 */
public class ConfideShowFragment extends Fragment implements Handler.Callback, View.OnClickListener {

    private static final int QUERY_CONFIDE_RESULT = 101;
    private static final int DELETE_CONFIDE_RESULT = 102;

    private CustomListView mConfideShowList;
    private ImageView loading;

    private EventBus mUIEventBus;
    private Activity activity;
    private ConfideShowAdapter adapter;
    private ConfideManager mConfideManager;
    private int mRequstTime = 0;
    private Handler handler;
    private boolean isMyConfide = false;
    private String confideId;
    private int index;

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;
        switch (msg.what) {
            case QUERY_CONFIDE_RESULT:
                handlerQueryConfide(msg);
                break;

            case DELETE_CONFIDE_RESULT:
                handlerDeleteConfide(msg.arg1);
                break;

            default:
                handled = false;
                break;
        }
        return handled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        argmentParam();
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        handler = new Handler(this);
        mConfideManager = ConfideManager.getInstance(activity);
        adapter = new ConfideShowAdapter(activity, this);
        mUIEventBus.register(this);
    }

    private void argmentParam() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            isMyConfide = bundle.getBoolean(ConfideUtils.IS_MY_CONFIDE);
        }
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(ConfidePublishEvent event) {
        Confide confide = event.getConfide();
        if (confide != null) {
            adapter.addSendConfide(confide);
        }
    }

    private CustomListView.OnRefreshListener onRefreshListener = new CustomListView.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mRequstTime = 0;
            queryConfideShow(ConfideUtils.REFRESH);
        }
    };

    private CustomListView.OnLoadMoreListener onLoadMoreListener = new CustomListView.OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            queryConfideShow(ConfideUtils.LOADING);
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Confide confide = adapter.getItem(position - 1);
            if (confide != null) {
                UILauncher.launchConfideDetaileUI(ConfideShowFragment.this, confide, ConfideUtils.CONFIDE_SHOW_CODE, position - 1);
            }
        }
    };

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (isMyConfide) {
                index = position - 1;
                confideId = adapter.getItem(index).getConfideId();
                showConfirmDialog();
            }
            return true;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confide_show, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView right_title = (TextView) view.findViewById(R.id.right_title);
        right_title.setBackgroundResource(R.drawable.write_confide);
        right_title.setVisibility(View.VISIBLE);
        right_title.setOnClickListener(this);
        icon.setOnClickListener(this);
        title.setText(getString(isMyConfide ? R.string.my_confide : R.string.confide));
        right_title.setText("");

        mConfideShowList = (CustomListView) view.findViewById(R.id.confide_show_list);
        loading = (ImageView) view.findViewById(R.id.confide_loading);
        mConfideShowList.setOnItemClickListener(onItemClickListener);
        mConfideShowList.setOnItemLongClickListener(onItemLongClickListener);
        mConfideShowList.setOnRefreshListener(onRefreshListener);
        mConfideShowList.setOnLoadListener(onLoadMoreListener);
        mConfideShowList.setAdapter(adapter);
        queryConfideShow(ConfideUtils.REFRESH);
        ConfideUtils.startAnimation(loading);
        return view;
    }

    private void queryConfideShow(int flgs) {
        if (isMyConfide) {
            queryMyConfide(flgs);
        } else {
            queryConfide(flgs);
        }
    }

    private void handlerQueryConfide(Message msg) {
        ConfideUtils.stopAnimation(loading);
        if (msg.arg1 == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            Confide[] confides = (Confide[]) msg.obj;
            if (confides != null && confides.length > 0) {
                adapter.notifyAdapterList(confides, mConfideShowList, msg.arg2);
            } else {
                if (msg.arg2 == ConfideUtils.LOADING) {
                    mConfideShowList.onLoadMoreComplete();
                }
            }
        }
    }

    private void handlerDeleteConfide(int result) {
        dismissProgressDialog();
        if (result == ConstantCode.EXECUTE_RESULT_SUCCESS) {
            adapter.removeConfide(index);
        }
    }

    public void queryConfide(final int flags) {
        mRequstTime++;
        mConfideManager.queryConfide(String.valueOf(mRequstTime), new ConfideManager.ConfideObserver() {
            @Override
            public void onQueryResult(int result, Confide[] confides) {
                handler.obtainMessage(QUERY_CONFIDE_RESULT, result, flags, confides).sendToTarget();
            }
        });
    }

    public void queryMyConfide(final int flags) {
        mRequstTime++;
        mConfideManager.queryMyConfides(String.valueOf(mRequstTime), new ConfideManager.ConfideObserver() {
            @Override
            public void onQueryResult(int result, Confide[] confides) {
                handler.obtainMessage(QUERY_CONFIDE_RESULT, result, flags, confides).sendToTarget();
            }
        });
    }

    public void deleteConfide() {
        mConfideManager.deleteConfide(confideId, new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                handler.obtainMessage(DELETE_CONFIDE_RESULT, result).sendToTarget();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case ConfideUtils.CONFIDE_SHOW_CODE:
                resultConfideDetaile(resultCode, data);
                break;

            default:
                break;
        }

    }

    private void resultConfideDetaile(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Confide confide = data.getParcelableExtra(ConfideUtils.CONFIDE);
            int index = data.getIntExtra(ConfideUtils.CONFIDE_INDEX, 0);
            if (confide != null) {
                adapter.addAdapterList(confide, index);
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.operation_bar_praise:
                int position = Integer.parseInt(v.getTag().toString());
                Confide confide = adapter.getItem(position);
                if (!TextUtils.isEmpty(confide.getConfideId())) {
                    praiseOnlick(confide);
                }
                adapter.notifyDataSetChanged();
                break;

            case R.id.icon:
                activity.finish();
                break;

            case R.id.right_title:
                UILauncher.launchEditConfideUI(activity);
                break;
            default:
                break;

        }
    }

    private void praiseOnlick(Confide confide) {
        mConfideManager.praiseConfide(confide.getConfideId(), new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {

            }
        });
        if ("Y".equals(confide.getConfideIsPraise())) {
            confide.setConfideIsPraise("N");
            confide.setConfidePraiseNum(confide.getConfidePraiseNum() - 1);
        } else {
            confide.setConfideIsPraise("Y");
            confide.setConfidePraiseNum(confide.getConfidePraiseNum() + 1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUIEventBus.unregister(this);
    }

    private ConfirmDialogFragment.AbsConfirmListener confirmListener = new ConfirmDialogFragment.AbsConfirmListener() {
        @Override
        public void onConfirm() {
            showProgressDialog();
            deleteConfide();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    private SimpleProgressDialog mProgressDialog;

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = SimpleProgressDialog.newInstance();
            mProgressDialog.show(getFragmentManager(), "SimpleProgressDialog");
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showConfirmDialog() {
        ConfirmDialogFragment.UiConfig uiConfig = new ConfirmDialogFragment.UiConfig(activity.getString(R.string.delete_confide), null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        confirmDialogFragment.show(getFragmentManager(), "ConfideShowFragment");
    }
}
