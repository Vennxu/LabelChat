package com.ekuater.labelchat.ui.fragment.labels;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.ui.util.ChsLengthFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class AddUserLabelFragment extends Fragment {

    public static final String ARG_SEARCH_KEYWORD = "search_keyword";

    private static final String PROGRESS_DIALOG_TAG = "ProgressDialog";

    private static final int LABEL_LIST_MODE_LIST = 0;
    private static final int LABEL_LIST_MODE_QUERY = 1;

    private static final int MSG_QUERY_LABEL = 100;
    private static final int MSG_ADD_LABEL_RESULT = 101;
    private static final int MSG_HANDLE_QUERY_RESULT = 102;
    private static final int MSG_HANDLE_LOAD_MORE_RESULT = 103;

    private static final long QUERY_LABEL_DELAY = 2000;

    private static class QueryResult {

        public final List<SystemLabel> labelList;
        public final boolean remaining;
        public final String keyword;

        public QueryResult(List<SystemLabel> labelList, boolean remaining, String keyword) {
            this.labelList = labelList;
            this.remaining = remaining;
            this.keyword = keyword;
        }
    }

    private String mSearchKeyword;
    private int mMaxLabelLength;
    private int mLabelListMode = -1;
    private int mListRequestTime = 1;
    private boolean mRemaining = false;
    private UserLabelManager mLabelManager;
    private LabelAdapter mLabelAdapter;
    private EditText mKeywordEdit;
    private ListView mListView;
    private ProgressBar mLoadMoreView;
    private TextView mNotFoundView;
    private View mCustomizeArea;
    private TextView mCustomizeView;
    private ProgressBar mLoadProgress;
    private boolean mAddingLabel = false;
    private SimpleProgressDialog mProgressDialog;
    private MenuItem mAddMenuItem;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUERY_LABEL:
                    queryLabel((String) msg.obj);
                    break;
                case MSG_ADD_LABEL_RESULT:
                    handleLabelAddedResult(msg.arg1);
                    break;
                case MSG_HANDLE_QUERY_RESULT:
                    handleQueryResult(msg.obj);
                    break;
                case MSG_HANDLE_LOAD_MORE_RESULT:
                    handleLoadMoreResult(msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private final TextWatcher mKeywordWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSearchKeyword = s.toString().trim();
            sendQueryLabel();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private final OnScrollListener mListScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (mListView == view) {
                if (mListView.getLastVisiblePosition() == totalItemCount - 1) {
                    loadMoreLabel();
                }
            }
        }
    };

    private final UserLabelManager.IListener mLabelListener
            = new UserLabelManager.AbsListener() {
        @Override
        public void onLabelAdded(int result) {
            Message msg = mHandler.obtainMessage(MSG_ADD_LABEL_RESULT, result, 0);
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.add_labels);
        }

        mMaxLabelLength = activity.getResources().getInteger(R.integer.label_max_length);
        mLabelManager = UserLabelManager.getInstance(activity);
        mLabelManager.registerListener(mLabelListener);
        mLabelAdapter = new LabelAdapter(activity, mLabelManager.getAllLabels(),
                AccountManager.getInstance(activity).getUserId());

        setHasOptionsMenu(true);

        // get arguments
        Bundle args = getArguments();
        if (args != null) {
            mSearchKeyword = args.getString(ARG_SEARCH_KEYWORD);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLabelManager.unregisterListener(mLabelListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_label_add_user_label, container, false);
        mKeywordEdit = (EditText) view.findViewById(R.id.keyword);
        mKeywordEdit.addTextChangedListener(mKeywordWatcher);
        mKeywordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideInputMethod(v);
                }
            }
        });
        addMaxLabelLengthFilter(mKeywordEdit);
        mListView = (ListView) view.findViewById(R.id.label_list);
        mListView.setAdapter(mLabelAdapter);
        mListView.setOnScrollListener(mListScrollListener);
        mLoadMoreView = (ProgressBar) view.findViewById(R.id.load_more_progress);
        mNotFoundView = (TextView) view.findViewById(R.id.not_found_label);
        mCustomizeView = (TextView) view.findViewById(R.id.customize_label);
        mCustomizeArea = view.findViewById(R.id.customize_label_area);
        mCustomizeArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLabelManager.addUserLabels(new BaseLabel[]{
                        new BaseLabel(mSearchKeyword, null),
                });
                finish();
            }
        });
        mLoadProgress = (ProgressBar) view.findViewById(R.id.progress);

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mListView.requestFocus();
                }
                return false;
            }
        });

        queryLabel(mSearchKeyword);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mKeywordEdit.removeTextChangedListener(mKeywordWatcher);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_menu, menu);
        mAddMenuItem = menu.findItem(R.id.menu_add);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateAddMenuEnable();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.menu_add:
                handleAddLabels();
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    private void addMaxLabelLengthFilter(EditText editText) {
        final InputFilter[] filters = editText.getFilters();
        final InputFilter lengthFilter = new ChsLengthFilter(mMaxLabelLength);
        InputFilter[] newFilters;

        if (filters != null && filters.length > 0) {
            final int length = filters.length;
            newFilters = new InputFilter[length + 1];
            System.arraycopy(filters, 0, newFilters, 0, length);
            newFilters[length] = lengthFilter;
        } else {
            newFilters = new InputFilter[]{lengthFilter};
        }
        editText.setFilters(newFilters);
    }

    private void updateAddMenuEnable() {
        if (mAddMenuItem != null) {
            SystemLabel[] labels = mLabelAdapter.getCheckedLabels();
            mAddMenuItem.setEnabled(labels != null && labels.length > 0);
        }
    }

    private void hideInputMethod(View view) {
        final Activity activity = getActivity();
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void handleAddLabels() {
        if (!mAddingLabel) {
            SystemLabel[] labels = mLabelAdapter.getCheckedLabels();

            if (labels != null && labels.length > 0) {
                BaseLabel[] baseLabels = new BaseLabel[labels.length];
                for (int i = 0; i < labels.length; ++i) {
                    baseLabels[i] = labels[i].toBaseLabel();
                }
                mAddingLabel = true;
                showProgressDialog();
                mLabelManager.addUserLabels(baseLabels);
            } else {
                Toast.makeText(getActivity(), R.string.not_select_label,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleLabelAddedResult(int result) {
        if (mAddingLabel) {
            mAddingLabel = false;
            dismissProgressDialog();

            if (result == ConstantCode.LABEL_OPERATION_SUCCESS) {
                Toast.makeText(getActivity(), R.string.add_label_success,
                        Toast.LENGTH_SHORT).show();
                if (mLabelManager.isInGuestMode()) {
                    UILauncher.launchMainUIWhenJustLogin(getActivity());
                }
                finish();
            } else {
                Toast.makeText(getActivity(), R.string.add_label_failure,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void queryLabel(String keyword) {
        LabelQueryObserver observer = new LabelQueryObserver(keyword) {
            @Override
            public void onQueryResult(int result, SystemLabel[] labels, boolean remaining) {
                final List<SystemLabel> labelList = new ArrayList<SystemLabel>();

                if (labels != null && labels.length > 0) {
                    for (SystemLabel label : labels) {
                        if (label != null) {
                            labelList.add(label);
                        }
                    }
                }

                Message message = mHandler.obtainMessage(MSG_HANDLE_QUERY_RESULT,
                        new QueryResult(labelList, remaining, mKeyword));
                mHandler.sendMessage(message);
            }
        };

        mLoadProgress.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(keyword)) {
            switchListMode(LABEL_LIST_MODE_LIST);
            mLabelManager.listSystemLabels(mListRequestTime, observer);
        } else {
            switchListMode(LABEL_LIST_MODE_QUERY);
            mLabelManager.querySystemLabels(keyword, observer);
        }
    }

    private void handleQueryResult(Object object) {
        if (object instanceof QueryResult) {
            final QueryResult queryResult = (QueryResult) object;

            if (queryResult.keyword != null && !queryResult.keyword.equals(mSearchKeyword)) {
                // skip the result if keyword changed.
                return;
            }

            mRemaining = queryResult.remaining;
            mLabelAdapter.updateLabelList(queryResult.labelList);
            mLoadProgress.setVisibility(View.GONE);
            mLoadMoreView.setVisibility(View.GONE);
            mCustomizeArea.setVisibility(View.GONE);
            if (queryResult.labelList.size() > 0) {
                mNotFoundView.setVisibility(View.GONE);
            } else {
                mNotFoundView.setVisibility(View.VISIBLE);
                if (!isInListMode() && queryResult.keyword != null
                        && queryResult.keyword.length() > 1) {
                    mCustomizeArea.setVisibility(View.VISIBLE);
                    mCustomizeView.setText(queryResult.keyword);
                }
            }
        }
    }

    private void loadMoreLabel() {
        if (!isInListMode() || !mRemaining) {
            return;
        }

        mLoadMoreView.setVisibility(View.VISIBLE);
        mListRequestTime++;
        LabelQueryObserver observer = new LabelQueryObserver(null) {
            @Override
            public void onQueryResult(int result, SystemLabel[] labels, boolean remaining) {
                final List<SystemLabel> labelList = new ArrayList<SystemLabel>();

                if (labels != null && labels.length > 0) {
                    for (SystemLabel label : labels) {
                        if (label != null) {
                            labelList.add(label);
                        }
                    }
                }

                Message message = mHandler.obtainMessage(MSG_HANDLE_LOAD_MORE_RESULT,
                        new QueryResult(labelList, remaining, mKeyword));
                mHandler.sendMessage(message);
            }
        };
        mLabelManager.listSystemLabels(mListRequestTime, observer);
    }

    private void handleLoadMoreResult(Object object) {
        if (isInListMode() && (object instanceof QueryResult)) {
            final QueryResult queryResult = (QueryResult) object;

            mRemaining = queryResult.remaining;
            mLabelAdapter.addLabelList(queryResult.labelList);
            mLoadMoreView.setVisibility(View.GONE);
        }
    }

    private boolean isInListMode() {
        return mLabelListMode == LABEL_LIST_MODE_LIST;
    }

    private void switchListMode(int newMode) {
        if (mLabelListMode == newMode) {
            return;
        }

        mListRequestTime = 1;

        switch (newMode) {
            case LABEL_LIST_MODE_QUERY:
                mLabelListMode = LABEL_LIST_MODE_QUERY;
                break;
            case LABEL_LIST_MODE_LIST:
            default:
                mLabelListMode = LABEL_LIST_MODE_LIST;
                break;
        }
    }

    private void sendQueryLabel() {
        mHandler.removeMessages(MSG_QUERY_LABEL);
        Message msg = mHandler.obtainMessage(MSG_QUERY_LABEL, mSearchKeyword);
        mHandler.sendMessageDelayed(msg, QUERY_LABEL_DELAY);
    }

    private void finish() {
        getActivity().finish();
    }

    private static abstract class LabelQueryObserver
            implements UserLabelManager.ISystemLabelQueryObserver {

        protected final String mKeyword;

        public LabelQueryObserver(String keyword) {
            mKeyword = keyword;
        }
    }

    private class LabelAdapter extends BaseAdapter {

        private final int OWN_STATE_NOT_SET = 0;
        private final int OWN_STATE_OWN = 1;
        private final int OWN_STATE_NOT_OWN = 2;

        private final List<SystemLabel> mLabelsList = new ArrayList<SystemLabel>();
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final SparseBooleanArray mCheckStates;
        private final SparseIntArray mOwnStates;
        private final UserLabel[] mOwnLabels;
        private final String mUserId;

        public LabelAdapter(Context context, UserLabel[] ownLabels, String userId) {
            super();
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mCheckStates = new SparseBooleanArray();
            mOwnStates = new SparseIntArray();
            mOwnLabels = ownLabels;
            mUserId = userId;
        }

        public synchronized void updateLabelList(List<SystemLabel> list) {
            mLabelsList.clear();
            mCheckStates.clear();
            mOwnStates.clear();
            if (list != null) {
                mLabelsList.addAll(list);
            }
            notifyDataSetChanged();
        }

        public synchronized void addLabelList(List<SystemLabel> list) {
            mLabelsList.addAll(list);
            notifyDataSetChanged();
        }

        private boolean isOwnLabel(SystemLabel label) {
            if (mOwnLabels != null && mOwnLabels.length > 0) {
                for (UserLabel userLabel : mOwnLabels) {
                    if (userLabel.getId().equals(label.getId())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int getCount() {
            return mLabelsList.size();
        }

        @Override
        public SystemLabel getItem(int position) {
            return mLabelsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = mInflater.inflate(R.layout.label_add_select_item, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.holder = view.findViewById(R.id.holder);
                holder.nameText = (TextView) view.findViewById(R.id.name);
                holder.selectView = (CheckBox) view.findViewById(R.id.select);
                holder.countText = (TextView) view.findViewById(R.id.count);
                view.setTag(holder);
            }
            bindView(view, position);
            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return !isOwn(position) || !isLabelUserCreated(getItem(position));
        }

        private boolean isOwn(int position) {
            boolean isOwn;

            switch (mOwnStates.get(position, OWN_STATE_NOT_SET)) {
                case OWN_STATE_OWN:
                    isOwn = true;
                    break;
                case OWN_STATE_NOT_OWN:
                    isOwn = false;
                    break;
                case OWN_STATE_NOT_SET:
                default:
                    isOwn = isOwnLabel(getItem(position));
                    mOwnStates.put(position, isOwn ? OWN_STATE_OWN : OWN_STATE_NOT_OWN);
                    break;
            }

            return isOwn;
        }

        public boolean getChecked(int position) {
            return mCheckStates.get(position, false);
        }

        public SystemLabel[] getCheckedLabels() {
            List<SystemLabel> labels = new ArrayList<SystemLabel>();

            for (int i = 0; i < getCount(); ++i) {
                if (getChecked(i)) {
                    labels.add(getItem(i));
                }
            }

            final int count = labels.size();
            return (count > 0) ? labels.toArray(new SystemLabel[count]) : null;
        }

        private boolean isLabelUserCreated(SystemLabel label) {
            return mUserId.equals(label.getCreateUserId());
        }

        private void bindView(View view, int position) {
            SystemLabel label = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.nameText.setText(label.getName());
            holder.countText.setText(mContext.getString(R.string.label_use_count,
                    label.getTotalUser()));
            if (isOwn(position) && isLabelUserCreated(label)) {
                holder.selectView.setChecked(true);
                holder.selectView.setEnabled(false);
                holder.holder.setVisibility(View.VISIBLE);
            } else {
                holder.selectView.setChecked(getChecked(position));
                holder.selectView.setEnabled(true);
                holder.holder.setVisibility(!isOwn(position) ? View.VISIBLE : View.GONE);
            }
            final int p = position;
            holder.nameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    UILauncher.launchFragmentLabelStoryUI(getActivity(), getItem(p).toBaseLabel(), null);
                }
            });
            holder.selectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    mCheckStates.put(p, cb.isChecked());
                    updateAddMenuEnable();
                }
            });
        }

        private class ViewHolder {
            public View holder;
            public TextView nameText;
            public CheckBox selectView;
            public TextView countText;
        }
    }

    private void showProgressDialog() {
        dismissProgressDialog();
        mProgressDialog = SimpleProgressDialog.newInstance();
        mProgressDialog.show(getFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
