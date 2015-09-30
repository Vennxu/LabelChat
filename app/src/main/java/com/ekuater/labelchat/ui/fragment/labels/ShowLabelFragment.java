package com.ekuater.labelchat.ui.fragment.labels;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.SystemLabel;
import com.ekuater.labelchat.delegate.UserLabelManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.widget.LabelImageView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by FC on 2015/3/2.
 *
 * @author FanChong
 */
public class ShowLabelFragment extends Fragment {
    private static final int MSG_QUERY_HOT_LABEL_RESULT = 101;
    private static final int MSG_QUERY_LABEL_RESULT = 102;
    private static final int MSG_HANDLE_LOAD_MORE_RESULT = 103;

    private Context mContext;
    private MenuItem mSearchMenuItem;
    private int mScreenWidth;
    private int mListRequestTime = 1;
    private boolean mRemaining = false;
    private UserLabelManager mLabelManager;

    private View headView;
    private ProgressBar mLoadHotLabelView;
    private ProgressBar mLoadView;
    private ProgressBar mLoadMoreView;
    private ImageView searchImage;
    private RelativeLayout rl1, rl2, rl3;

    private ListView mLabelListView;
    private SystemLabelAdapter mSystemLabelAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_QUERY_HOT_LABEL_RESULT:
                    handlerHotSystemLabelQueryResult((QueryResult) msg.obj);
                    break;
                case MSG_QUERY_LABEL_RESULT:
                    handlerSystemLabelQueryResult((QueryResult) msg.obj);
                    break;
                case MSG_HANDLE_LOAD_MORE_RESULT:
                    handleLoadMoreResult((QueryResult) msg.obj);
                    break;

            }
        }
    };

    private static class QueryResult {
        public final List<SystemLabel> labelList;
        public final boolean remaining;

        public QueryResult(List<SystemLabel> labelList, boolean remaining) {
            this.labelList = labelList;
            this.remaining = remaining;
        }
    }

    private void queryHotSystemLabel() {
        UserLabelManager.HotLabelQueryObserver observer = new UserLabelManager.HotLabelQueryObserver() {
            @Override
            public void onQueryResult(int result, SystemLabel[] labels) {
                List<SystemLabel> list = new ArrayList<SystemLabel>();
                if (labels != null && labels.length > 0) {
                    for (SystemLabel label : labels) {
                        if (label != null) {
                            list.add(label);
                        }
                    }
                }
                Message message = mHandler.obtainMessage(MSG_QUERY_HOT_LABEL_RESULT, new QueryResult(list, false));
                mHandler.sendMessage(message);
            }
        };
        mLabelManager.queryHotLabel(observer);

    }

    private List<SystemLabel> mHotSystemLabelList = new ArrayList<SystemLabel>();
    private List<SystemLabel> mTempLabelList = new ArrayList<SystemLabel>();
    private Random random = new Random();
    private int index = random.nextInt(3);
    private int index2 = random.nextInt(3);
    private int index3 = random.nextInt(3);
    private int[] array = new int[]{3000, 3500, 4000};
    private int[] duration = new int[]{6000, 6500, 7000};

    private void handlerHotSystemLabelQueryResult(QueryResult result) {
        mTempLabelList = result.labelList;
        mHotSystemLabelList.addAll(result.labelList);
        if (mHotSystemLabelList != null && mHotSystemLabelList.size() > 0) {
            mLoadHotLabelView.setVisibility(View.GONE);
            View labelView = addLayout(mContext, mHotSystemLabelList);
            rl1.addView(labelView);
            addAnimation(labelView, duration[index], array[index]);

            View labelView2 = addLayout(mContext, mHotSystemLabelList);
            rl2.addView(labelView2);
            addAnimation(labelView2, duration[index2], array[index2]);

            View labelView3 = addLayout(mContext, mHotSystemLabelList);
            rl3.addView(labelView3);
            addAnimation(labelView3, duration[index3], array[index3]);
        }

    }

    private void addAnimation(final View view, final long time, final long delay) {
        view.measure(0, 0);
        view.setTranslationX(mScreenWidth);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX",
                mScreenWidth, -view.getMeasuredWidth());
        animator.setDuration(time);
        animator.setStartDelay(delay);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (view.getParent().equals(rl1)) {
                    View labelView = addLayout(mContext, mHotSystemLabelList);
                    rl1.addView(labelView);
                    addAnimation(labelView, duration[index], array[index]);
                }
                if (view.getParent().equals(rl2)) {
                    View labelView2 = addLayout(mContext, mHotSystemLabelList);
                    rl2.addView(labelView2);
                    addAnimation(labelView2, duration[index2], array[index]);
                }
                if (view.getParent().equals(rl3)) {
                    View labelView3 = addLayout(mContext, mHotSystemLabelList);
                    rl3.addView(labelView3);
                    addAnimation(labelView3, duration[index3], array[index]);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mHotSystemLabelList.size() == 0) {
                    mHotSystemLabelList.addAll(mTempLabelList);
                }
                ((ViewGroup) view.getParent()).removeView(view);
            }


            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private View addLayout(Context context, final List<SystemLabel> label) {
        final int position = random.nextInt(label.size());
        LinearLayout ll = newLinearLayout(context);
        View view = getHotLabelView(context, ll);
        LabelImageView liv = (LabelImageView) view.findViewById(R.id.label_avatar_image);
        TextView tv = (TextView) view.findViewById(R.id.label_name);
        String image = label.get(position).getImage();
        String name = label.get(position).getName();
        if (!TextUtils.isEmpty(image)) {
            mLabelManager.displayLabelImage(image, liv, R.drawable.label_ic);
        }
        tv.setText(name);
        mHotSystemLabelList.remove(label.get(position));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        return view;
    }


    private void querySystemLabel() {
        UserLabelManager.RankLabelQueryObserver observer = new UserLabelManager.RankLabelQueryObserver() {
            @Override
            public void onQueryResult(int result, SystemLabel[] labels, boolean remaining) {
                List<SystemLabel> list = new ArrayList<SystemLabel>();
                if (labels != null && labels.length > 0) {
                    for (SystemLabel label : labels) {
                        if (label != null) {
                            list.add(label);
                        }
                    }
                }
                Message message = mHandler.obtainMessage(MSG_QUERY_LABEL_RESULT, new QueryResult(list, remaining));
                mHandler.sendMessage(message);
            }
        };
        mLabelManager.queryRankLabel(mListRequestTime, observer);
    }

    private void handlerSystemLabelQueryResult(QueryResult result) {
        mRemaining = result.remaining;
        mSystemLabelAdapter.updateLabelList(result.labelList);
        mLoadView.setVisibility(View.GONE);
    }

    private void loadMoreLabel() {
        if (!mRemaining) {
            return;
        }
        mListRequestTime++;
        UserLabelManager.RankLabelQueryObserver observer = new UserLabelManager.RankLabelQueryObserver() {
            @Override
            public void onQueryResult(int result, SystemLabel[] labels, boolean remaining) {
                List<SystemLabel> list = new ArrayList<SystemLabel>();
                if (labels != null && labels.length > 0) {
                    for (SystemLabel label : labels) {
                        if (label != null) {
                            list.add(label);
                        }
                    }
                }
                Message message = mHandler.obtainMessage(MSG_HANDLE_LOAD_MORE_RESULT, new QueryResult(list, remaining));
                mHandler.sendMessage(message);
            }
        };
        mLabelManager.queryRankLabel(mListRequestTime, observer);
        mLoadMoreView.setVisibility(View.VISIBLE);
    }

    private void handleLoadMoreResult(QueryResult result) {
        mRemaining = result.remaining;
        mSystemLabelAdapter.addLabelList(result.labelList);
        mLoadMoreView.setVisibility(View.GONE);
    }

    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        mLabelManager = UserLabelManager.getInstance(mContext);
        mSystemLabelAdapter = new SystemLabelAdapter(mContext);
        mScreenWidth = getScreenWidth(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_label, container, false);
        mLabelListView = (ListView) view.findViewById(R.id.show_label_list);
        headView = inflater.inflate(R.layout.freagem_show_label_header, mLabelListView, false);
        rl1 = (RelativeLayout) headView.findViewById(R.id.hot_lable_arer1);
        rl2 = (RelativeLayout) headView.findViewById(R.id.hot_lable_arer2);
        rl3 = (RelativeLayout) headView.findViewById(R.id.hot_lable_arer3);
        searchImage = (ImageView) headView.findViewById(R.id.search);
        mLoadHotLabelView = (ProgressBar) headView.findViewById(R.id.load_hot_label_progress);
        mLoadView = (ProgressBar) view.findViewById(R.id.progress);
        mLoadMoreView = (ProgressBar) view.findViewById(R.id.load_more_progress);
        queryHotSystemLabel();
        querySystemLabel();
        mLabelListView.setDividerHeight(0);
        mLabelListView.addHeaderView(headView);
        mLabelListView.setAdapter(mSystemLabelAdapter);
        mLabelListView.setAdapter(mSystemLabelAdapter);
        mLabelListView.setOnScrollListener(mListScrollListener);
        mLabelListView.setOnItemClickListener(mOnItemClickListener);
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UILauncher.launchExactSearchFriendUI(mContext);
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.exact_search_menu, menu);
        mSearchMenuItem = menu.findItem(R.id.menu_search);
        mSearchMenuItem.setEnabled(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UILauncher.launchSearchFriendByLabelsUI(mContext, mSystemLabelAdapter.getBaseLabel());
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private LinearLayout newLinearLayout(Context context) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        return ll;
    }


    private View getHotLabelView(Context context, ViewGroup parent) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.hot_system_label_item, parent, false);
        return view;
    }

    private final AbsListView.OnScrollListener mListScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (mLabelListView == view) {
                if (mLabelListView.getLastVisiblePosition() == totalItemCount - 1) {
                    loadMoreLabel();
                }
            }
        }
    };
    private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.select);
            SystemLabel label;
            Object object = parent.getItemAtPosition(position);
            if (object instanceof SystemLabel) {
                label = (SystemLabel) object;
                if (!checkBox.isChecked()) {
                    mSystemLabelAdapter.addSelectLabel(label);
                } else {
                    mSystemLabelAdapter.removeSelectLabel(label);
                }
                mSystemLabelAdapter.notifyDataSetChanged();
                mSearchMenuItem.setEnabled((mSystemLabelAdapter.getSelectLabel() != null && mSystemLabelAdapter.getSelectLabel().size() > 0));
            }
        }
    };


    public class SystemLabelAdapter extends BaseAdapter {
        private List<SystemLabel> mSystemLabelList = new ArrayList<SystemLabel>();
        private List<SystemLabel> mSelectedLabelList = new ArrayList<SystemLabel>();
        private LayoutInflater mInflater;
        private UserLabelManager mLabelManager;
        private Context mContext;


        public SystemLabelAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLabelManager = UserLabelManager.getInstance(context);
        }

        public synchronized void updateLabelList(List<SystemLabel> list) {
            mSystemLabelList.clear();
            mSystemLabelList.addAll(list);
            notifyDataSetChanged();
        }

        public synchronized void addLabelList(List<SystemLabel> list) {
            mSystemLabelList.addAll(list);
            sortSystemLabels(mSystemLabelList);
            notifyDataSetChanged();
        }


        public synchronized void addSelectLabel(SystemLabel label) {
            mSelectedLabelList.add(label);
            notifyDataSetChanged();
        }

        public synchronized void removeSelectLabel(SystemLabel label) {
            mSelectedLabelList.remove(label);
            notifyDataSetChanged();
        }

        public List<SystemLabel> getSelectLabel() {
            return mSelectedLabelList;
        }

        public synchronized BaseLabel[] getBaseLabel() {
            final int size = mSelectedLabelList.size();
            BaseLabel[] labels = null;
            if (size > 0) {
                labels = new BaseLabel[size];
                for (int i = 0; i < size; i++) {
                    labels[i] = mSelectedLabelList.get(i).toBaseLabel();
                }
            }
            return labels;
        }

        private final Comparator<SystemLabel> mComparator = new Comparator<SystemLabel>() {
            @Override
            public int compare(SystemLabel lhs, SystemLabel rhs) {
                long diff = rhs.getTotalUser() - lhs.getTotalUser();
                diff = (diff != 0) ? diff : (rhs.getTime() - lhs.getTime());
                return (diff > 0) ? 1 : (diff < 0) ? -1 : 0;
            }
        };

        private void sortSystemLabels(List<SystemLabel> labelList) {
            Collections.sort(labelList, mComparator);
        }


        @Override
        public int getCount() {
            return mSystemLabelList == null ? 0 : mSystemLabelList.size();
        }

        @Override
        public SystemLabel getItem(int position) {
            return mSystemLabelList == null ? null : mSystemLabelList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(position, convertView);
            return convertView;
        }

        private boolean isShow(int position) {
            if (mSelectedLabelList != null && mSelectedLabelList.size() > 0) {
                for (SystemLabel selectLabel : mSelectedLabelList) {
                    if (getItem(position).getId().equals(selectLabel.getId())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(R.layout.show_label_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.labelImageView = (LabelImageView) view.findViewById(R.id.label_avatar_image);
            holder.labelName = (TextView) view.findViewById(R.id.label_name);
            holder.userQuantity = (TextView) view.findViewById(R.id.user_quantity);
            holder.rl = (RelativeLayout) view.findViewById(R.id.label_content);
            holder.isSelect = (CheckBox) view.findViewById(R.id.select);
            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            SystemLabel label = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();
            String labelAvatarImage = label.getImage();
            String labelName = label.getName();
            long userQuantity = label.getTotalUser();
            if (TextUtils.isEmpty(labelAvatarImage)) {
                holder.labelImageView.setImageResource(R.drawable.label_ic);
            } else {
                mLabelManager.displayLabelImage(labelAvatarImage, holder.labelImageView, R.drawable.label_ic);
            }
            holder.labelName.setText(labelName);
            holder.userQuantity.setText(mContext.getString(R.string.label_use_count, userQuantity));
            holder.isSelect.setChecked(isShow(position));
            holder.rl.setBackgroundResource(isShow(position) ? R.color.colorPrimary : R.color.label_background);
        }

        private class ViewHolder {
            LabelImageView labelImageView;
            TextView labelName, userQuantity;
            CheckBox isSelect;
            RelativeLayout rl;

        }

    }
}
