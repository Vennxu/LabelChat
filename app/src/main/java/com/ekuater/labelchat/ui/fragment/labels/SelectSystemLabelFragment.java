package com.ekuater.labelchat.ui.fragment.labels;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.widget.ClearEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/1/27.
 *
 * @author LinYong
 */
public class SelectSystemLabelFragment extends Fragment {

    public static final String ARG_FILTER_LABEL_IDS = "arg_filter_label_ids";
    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_SELECT_MENU = "arg_select_menu";
    public static final String ARG_SELECT_COUNT = "arg_select_count";

    public static class UiConfig {

        public String[] filterLabelIds;
        public String title;
        public String selectMenu;
        public int selectCount;

        public UiConfig() {
            filterLabelIds = null;
            title = null;
            selectMenu = null;
            selectCount = 1;
        }
    }

    private static final int LABEL_LIST_MODE_LIST = 0;
    private static final int LABEL_LIST_MODE_QUERY = 1;

    private static final int MSG_QUERY_LABEL = 101;
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

    private UiConfig mUiConfig;
    private String mSearchKeyword;
    private int mLabelListMode = -1;
    private int mListRequestTime = 1;
    private boolean mRemaining = false;
    private UserLabelManager mLabelManager;
    private LabelAdapter mLabelAdapter;
    private ClearEditText mKeywordEdit;
    private ListView mListView;
    private ProgressBar mLoadMoreView;
    private TextView mNotFoundView;
    private ListView mSelectedListView;
    private SelectedLabelAdapter mSelectedAdapter;
    private ProgressBar mLoadProgress;
    private MenuItem mSelectMenuItem;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUERY_LABEL:
                    queryLabel((String) msg.obj);
                    break;
                case MSG_HANDLE_QUERY_RESULT:
                    handleQueryResult((QueryResult) msg.obj);
                    break;
                case MSG_HANDLE_LOAD_MORE_RESULT:
                    handleLoadMoreResult((QueryResult) msg.obj);
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

    private final AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {

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

    private final AdapterView.OnItemClickListener mItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent == mListView && parent.getAdapter() == mLabelAdapter) {
                mSelectedAdapter.addLabel(mLabelAdapter.getItem(position));
                mSelectedListView.smoothScrollToPosition(mSelectedListView.getBottom());
            } else if (parent == mSelectedListView
                    && parent.getAdapter() == mSelectedAdapter) {
                mSelectedAdapter.removeLabelAt(position);
            }

            mLabelAdapter.notifyDataSetChanged();
            updateSelectedListHeight();
            updateSelectMenuEnable();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        parseArguments();

        if (actionBar != null) {
            if (mUiConfig.title != null) {
                actionBar.setTitle(mUiConfig.title);
            } else {
                actionBar.setTitle(R.string.select_label);
            }
        }

        mLabelManager = UserLabelManager.getInstance(activity);
        UserLabel[] ownLabels = mLabelManager.getAllLabels();
        mLabelAdapter = new LabelAdapter(activity, ownLabels, mUiConfig.selectCount);
        mSelectedAdapter = new SelectedLabelAdapter(activity, ownLabels, mUiConfig.selectCount);
        mLabelAdapter.setSelectedLabels(mSelectedAdapter.getSelectedLabels());
        mSearchKeyword = null;

        setHasOptionsMenu(true);
    }

    private void parseArguments() {
        Bundle args = getArguments();
        mUiConfig = new UiConfig();
        mUiConfig.filterLabelIds = args.getStringArray(ARG_FILTER_LABEL_IDS);
        mUiConfig.title = args.getString(ARG_TITLE);
        mUiConfig.selectMenu = args.getString(ARG_SELECT_MENU);
        mUiConfig.selectCount = args.getInt(ARG_SELECT_COUNT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_system_label, container, false);
        mKeywordEdit = (ClearEditText) view.findViewById(R.id.keyword);
        mKeywordEdit.addTextChangedListener(mKeywordWatcher);
        mKeywordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideInputMethod(v);
                }
            }
        });
        mListView = (ListView) view.findViewById(R.id.label_list);
        mListView.setAdapter(mLabelAdapter);
        mListView.setOnScrollListener(mListScrollListener);
        mListView.setOnItemClickListener(mItemClickListener);
        mLoadMoreView = (ProgressBar) view.findViewById(R.id.load_more_progress);
        mNotFoundView = (TextView) view.findViewById(R.id.not_found_label);
        mSelectedListView = (ListView) view.findViewById(R.id.selected_label_list);
        mSelectedListView.setAdapter(mSelectedAdapter);
        mSelectedListView.setOnItemClickListener(mItemClickListener);
        mLoadProgress = (ProgressBar) view.findViewById(R.id.progress);
        if (mUiConfig.selectCount == 1) { // Single select
            mSelectedListView.setVisibility(View.GONE);
            view.findViewById(R.id.shadow_divider).setVisibility(View.GONE);
        }

        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.requestFocus();
                }
                return false;
            }
        };
        mListView.setOnTouchListener(onTouchListener);
        mSelectedListView.setOnTouchListener(onTouchListener);

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
        inflater.inflate(R.menu.select_menu, menu);
        mSelectMenuItem = menu.findItem(R.id.menu_select);
        if (!TextUtils.isEmpty(mUiConfig.selectMenu)) {
            mSelectMenuItem.setTitle(mUiConfig.selectMenu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateSelectMenuEnable();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.menu_select:
                handleSelectDone();
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    private void handleSelectDone() {
        Activity activity = getActivity();
        if (activity != null) {
            List<SystemLabel> labelList = mSelectedAdapter.getSelectedLabels();
            SystemLabel[] labels = labelList.toArray(new SystemLabel[labelList.size()]);
            Intent data = new Intent();

            data.putExtra("selected_labels", labels);
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }
    }

    private void updateSelectMenuEnable() {
        if (mSelectMenuItem != null) {
            BaseLabel[] labels = mSelectedAdapter.getBaseLabels();
            mSelectMenuItem.setEnabled(labels != null && labels.length > 0);
        }
    }

    private void hideInputMethod(View view) {
        final Activity activity = getActivity();
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void queryLabel(String keyword) {
        LabelQueryObserver observer = new LabelQueryObserver(keyword) {
            @Override
            public void onQueryResult(int result, SystemLabel[] labels, boolean remaining) {
                List<SystemLabel> labelList = filterQueryLabels(labels);
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

    private void handleQueryResult(QueryResult queryResult) {
        if (queryResult.keyword != null && !queryResult.keyword.equals(mSearchKeyword)) {
            // skip the result if keyword changed.
            return;
        }

        mRemaining = queryResult.remaining;
        mLabelAdapter.updateLabelList(queryResult.labelList);
        mLoadProgress.setVisibility(View.GONE);
        mLoadMoreView.setVisibility(View.GONE);
        mNotFoundView.setVisibility(queryResult.labelList.size() > 0
                ? View.GONE : View.VISIBLE);
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
                List<SystemLabel> labelList = filterQueryLabels(labels);
                Message message = mHandler.obtainMessage(MSG_HANDLE_LOAD_MORE_RESULT,
                        new QueryResult(labelList, remaining, mKeyword));
                mHandler.sendMessage(message);
            }
        };
        mLabelManager.listSystemLabels(mListRequestTime, observer);
    }

    private List<SystemLabel> filterQueryLabels(SystemLabel[] labels) {
        final List<SystemLabel> labelList = new ArrayList<SystemLabel>();

        if (labels != null && labels.length > 0) {
            for (SystemLabel label : labels) {
                if (label != null && !wantFilterOut(label)) {
                    labelList.add(label);
                }
            }
        }

        return labelList;
    }

    private boolean wantFilterOut(SystemLabel label) {
        String[] filterLabelIds = mUiConfig.filterLabelIds;
        String labelId = label.getId();

        if (filterLabelIds != null && filterLabelIds.length > 0) {
            for (String tmpLabelId : filterLabelIds) {
                if (labelId.equals(tmpLabelId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void handleLoadMoreResult(QueryResult queryResult) {
        if (isInListMode()) {
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

    private void updateSelectedListHeight() {
        setListViewHeightBasedOnChildren(mSelectedListView, 3);
    }

    private void setListViewHeightBasedOnChildren(ListView listView, int maxChildren) {
        final ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {
            return;
        }

        final int totalCount = listAdapter.getCount();
        final int maxCount = (maxChildren > 0 && maxChildren <= totalCount)
                ? maxChildren : totalCount;
        int totalHeight = 0;

        for (int i = 0; i < maxCount; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = ((maxCount > 0) ? (listView.getDividerHeight() * (maxCount - 1))
                : 0) + totalHeight;
        listView.setLayoutParams(params);
    }

    private static abstract class LabelQueryObserver
            implements UserLabelManager.ISystemLabelQueryObserver {

        protected final String mKeyword;

        public LabelQueryObserver(String keyword) {
            mKeyword = keyword;
        }
    }

    private static class LabelAdapter extends BaseAdapter {

        private final int OWN_STATE_NOT_SET = 0;
        private final int OWN_STATE_OWN = 1;
        private final int OWN_STATE_NOT_OWN = 2;

        private final List<SystemLabel> mLabelList;
        private final LayoutInflater mInflater;
        private final Context mContext;
        private final Resources mResources;
        private final SparseIntArray mOwnStates;
        private final UserLabel[] mOwnLabels;
        private final boolean mSingleSelect;
        private List<SystemLabel> mSelectedLabelList;

        public LabelAdapter(Context context, UserLabel[] ownLabels, int maxCount) {
            mContext = context;
            mResources = context.getResources();
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mLabelList = new ArrayList<SystemLabel>();
            mOwnStates = new SparseIntArray();
            mOwnLabels = ownLabels;
            mSingleSelect = (maxCount == 1);
        }

        public void setSelectedLabels(List<SystemLabel> labels) {
            mSelectedLabelList = labels;
        }

        public synchronized void updateLabelList(List<SystemLabel> list) {
            mLabelList.clear();
            mLabelList.addAll(list);
            mOwnStates.clear();
            notifyDataSetChanged();
        }

        public synchronized void addLabelList(List<SystemLabel> list) {
            mLabelList.addAll(list);
            notifyDataSetChanged();
        }

        private boolean isSelected(int position) {
            return isLabelSelected(getItem(position));
        }

        private boolean isLabelSelected(SystemLabel label) {
            for (SystemLabel tmpLabel : mSelectedLabelList) {
                if (tmpLabel.getId().equals(label.getId())) {
                    return true;
                }
            }
            return false;
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

        @Override
        public int getCount() {
            return mLabelList.size();
        }

        @Override
        public SystemLabel getItem(int position) {
            return mLabelList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = mInflater.inflate(R.layout.system_select_label_item, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.holder = view.findViewById(R.id.holder);
                holder.nameText = (TextView) view.findViewById(R.id.name);
                holder.countText = (TextView) view.findViewById(R.id.count);
                holder.checkBox = (CheckBox) view.findViewById(R.id.select);
                if (mSingleSelect) {
                    holder.holder.setBackgroundColor(mResources.getColor(R.color.transparent));
                }
                view.setTag(holder);
            }
            bindView(view, position);
            return view;
        }

        private void bindView(View view, int position) {
            SystemLabel label = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.nameText.setText(label.getName());
            holder.nameText.setSelected(isOwn(position));
            holder.countText.setText(mContext.getString(R.string.label_use_count,
                    label.getTotalUser()));
            boolean isSelected = isSelected(position);
            if (mSingleSelect) {
                holder.holder.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(isSelected);
            } else {
                holder.holder.setVisibility(isSelected ? View.GONE : View.VISIBLE);
                holder.checkBox.setChecked(false);
            }
        }

        private static class ViewHolder {
            public View holder;
            public TextView nameText;
            public TextView countText;
            public CheckBox checkBox;
        }
    }

    private static class SelectedLabelAdapter extends BaseAdapter {

        private final List<SystemLabel> mLabelsList = new ArrayList<SystemLabel>();
        private final LayoutInflater mInflater;
        private final UserLabel[] mOwnLabels;
        private final Context mContext;
        private final int mMaxCount;
        private final boolean mSingleSelect;

        public SelectedLabelAdapter(Context context, UserLabel[] ownLabels, int maxCount) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mOwnLabels = ownLabels;
            mMaxCount = maxCount;
            mSingleSelect = (mMaxCount == 1);
        }

        public List<SystemLabel> getSelectedLabels() {
            return mLabelsList;
        }

        public synchronized void addLabel(SystemLabel label) {
            if (label != null) {
                for (SystemLabel tmpLabel : mLabelsList) {
                    if (tmpLabel.getId().equals(label.getId())) {
                        return;
                    }
                }

                if (mSingleSelect) {
                    mLabelsList.clear();
                    mLabelsList.add(label);
                } else if (mLabelsList.size() < mMaxCount) {
                    mLabelsList.add(label);
                }
                notifyDataSetChanged();
            }
        }

        public synchronized void removeLabelAt(int position) {
            mLabelsList.remove(position);
            notifyDataSetChanged();
        }

        public synchronized BaseLabel[] getBaseLabels() {
            final int size = mLabelsList.size();
            BaseLabel[] labels = null;

            if (size > 0) {
                labels = new BaseLabel[size];
                for (int i = 0; i < size; ++i) {
                    labels[i] = mLabelsList.get(i).toBaseLabel();
                }
            }

            return labels;
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
                view = mInflater.inflate(R.layout.exact_search_label_item, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.nameText = (TextView) view.findViewById(R.id.name);
                holder.selectView = (CheckBox) view.findViewById(R.id.select);
                holder.countText = (TextView) view.findViewById(R.id.count);
                view.setTag(holder);
            }
            bindView(view, position);
            return view;
        }

        private void bindView(View view, int position) {
            SystemLabel label = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.nameText.setText(label.getName());
            holder.nameText.setSelected(isOwnLabel(label.getName()));
            holder.countText.setText(mContext.getString(R.string.label_use_count,
                    label.getTotalUser()));
            holder.selectView.setChecked(true);
        }

        private boolean isOwnLabel(String label) {
            if (mOwnLabels != null && mOwnLabels.length > 0) {
                for (UserLabel tmpLabel : mOwnLabels) {
                    if (tmpLabel.getName().equals(label)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static class ViewHolder {

            public TextView nameText;
            public CheckBox selectView;
            public TextView countText;
        }
    }
}
