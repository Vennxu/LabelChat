package com.ekuater.labelchat.ui.fragment.friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.UserLabelManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Leo on 2015/1/15.
 *
 * @author LinYong
 */
/*package*/ class UserPraiseLabelAdapter extends BaseAdapter
        implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    public interface AdapterListener {

        public void onLabelClick(UserLabel label);

        public void onLabelLongClick(UserLabel label);

        public void onLabelRecommend();
    }

    private AdapterListener mListener;
    private LayoutInflater mInflater;
    private List<String> mMyLabelIdList;
    private final List<PraiseLabelItem.Item> mItemList;
    private final PraiseLabelItem.LabelClickListener mLabelClickListener
            = new PraiseLabelItem.LabelClickListener() {
        @Override
        public void onClick(UserLabel label) {
            if (mListener != null) {
                mListener.onLabelClick(label);
            }
        }

        @Override
        public boolean onLongClick(UserLabel label) {
            if (mListener != null) {
                mListener.onLabelLongClick(label);
            }
            return true;
        }
    };

    private Comparator<PraiseLabelItem.Item> mComparator
            = new Comparator<PraiseLabelItem.Item>() {

        @Override
        public int compare(PraiseLabelItem.Item lhs, PraiseLabelItem.Item rhs) {
            return rhs.getCompareValue() - lhs.getCompareValue();
        }
    };

    public UserPraiseLabelAdapter(Context context, AdapterListener listener) {
        mListener = listener;
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mMyLabelIdList = getMyLabelIds(context);
        mItemList = new ArrayList<PraiseLabelItem.Item>();
    }

    public void updateUserLabels(UserLabel[] labels) {
        mItemList.clear();

        if (labels != null) {
            for (UserLabel label : labels) {
                mItemList.add(new PraiseLabelItem.LabelItem(label,
                        isMyLabel(label), mLabelClickListener));
            }
        }
    }

    public void addLabelRecommendItem() {
        mItemList.add(new PraiseLabelItem.RecommendItem(
                new PraiseLabelItem.RecommendListener() {
                    @Override
                    public void onRecommend() {
                        if (mListener != null) {
                            mListener.onLabelRecommend();
                        }
                    }
                }));
    }

    public void updateItemDone() {
        Collections.sort(mItemList, mComparator);
        notifyDataSetChanged();
    }

    private List<String> getMyLabelIds(Context context) {
        UserLabelManager labelManager = UserLabelManager.getInstance(context);
        UserLabel[] userLabels = labelManager.getAllLabels();
        List<String> list = new ArrayList<String>();

        if (userLabels != null) {
            for (UserLabel label : userLabels) {
                list.add(label.getId());
            }
        }

        return list;
    }

    private boolean isMyLabel(UserLabel label) {
        return mMyLabelIdList.contains(label.getId());
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public PraiseLabelItem.Item getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public int getViewTypeCount() {
        return PraiseLabelItem.getViewTypeCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PraiseLabelItem.Item item = getItem(position);

        if (convertView == null) {
            convertView = item.newView(mInflater, parent);
        }
        item.bindView(convertView);

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        getItem(position).onClick();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        return getItem(position).onLongClick();
    }
}
