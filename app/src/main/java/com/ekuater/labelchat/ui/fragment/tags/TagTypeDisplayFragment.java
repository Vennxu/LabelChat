package com.ekuater.labelchat.ui.fragment.tags;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.TagType;
import com.ekuater.labelchat.datastruct.UserTag;
import com.ekuater.labelchat.delegate.TagManager;
import com.ekuater.labelchat.ui.util.CompatUtils;
import com.ekuater.labelchat.ui.util.ShowToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/16.
 *
 * @author LinYong
 */
public class TagTypeDisplayFragment extends Fragment implements View.OnClickListener {

    private static int[][] COUNT_LAYOUT_ARRAY = {
            {4, R.layout.label_select_layout4},
            {5, R.layout.label_select_layout5},
            {10, R.layout.label_select_layout10},
            {21, R.layout.label_select_layout21},
    };

    private static int[] IDS = {
            R.id.label_select_text1,
            R.id.label_select_text2,
            R.id.label_select_text3,
            R.id.label_select_text4,
            R.id.label_select_text5,
            R.id.label_select_text6,
            R.id.label_select_text7,
            R.id.label_select_text8,
            R.id.label_select_text9,
            R.id.label_select_text10,
            R.id.label_select_text11,
            R.id.label_select_text12,
            R.id.label_select_text13,
            R.id.label_select_text14,
            R.id.label_select_text15,
            R.id.label_select_text16,
            R.id.label_select_text17,
            R.id.label_select_text18,
            R.id.label_select_text19,
            R.id.label_select_text20,
            R.id.label_select_text21,
            R.id.label_select_text22,
            R.id.label_select_text23,
            R.id.label_select_text24,
            R.id.label_select_text25,
    };

    private static int getCountLayoutIdx(int tagCount) {
        final int[][] array = COUNT_LAYOUT_ARRAY;

        for (int i = 0; i < array.length; ++i) {
            if (array[i][0] >= tagCount) {
                return i;
            }
        }
        return -1;
    }

    private TagType mTagType;
    private TagSelectListener mTagSelectListener;
    private TagWrapper[] mTagWrappers;
    private View mPrevSelectTagView;

    public void setTagSelectListener(TagSelectListener listener) {
        mTagSelectListener = listener;
    }

    public void setTagType(TagType tagType) {
        mTagType = tagType;
    }

    public UserTag[] getSelectedTags() {
        List<UserTag> tagList = new ArrayList<>();

        if (mTagWrappers != null && mTagWrappers.length > 0) {
            for (TagWrapper wrapper : mTagWrappers) {
                if (wrapper.isSelected()) {
                    tagList.add(wrapper.getUserTag());
                }
            }
        }

        return tagList.toArray(new UserTag[tagList.size()]);
    }

    private int getSelectedCount() {
        return getSelectedTags().length;
    }

    public String getTagTypeName() {
        return mTagType != null ? mTagType.getTypeName() : "";
    }

    private int getMaxSelect() {
        return mTagType != null ? mTagType.getMaxSelect() : 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        parseArgs(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final int tagCount = getTagCount();
        final int idx = getCountLayoutIdx(tagCount);
        final View rootView;

        if (idx >= 0) {
            final int[] array = COUNT_LAYOUT_ARRAY[idx];
            final int maxCount = array[0];
            final int layout = array[1];

            rootView = inflater.inflate(layout, container, false);
            mPrevSelectTagView = null;
            for (int i = 0; i < maxCount; ++i) {
                TextView tagView = (TextView) rootView.findViewById(IDS[i]);

                if (i < tagCount && mTagWrappers != null) {
                    TagWrapper wrapper = mTagWrappers[i];
                    UserTag tag = wrapper.getUserTag();

                    tagView.setText(tag.getTagName());
                    tagView.setTag(wrapper);
                    tagView.setOnClickListener(this);
                    CompatUtils.setBackground(tagView, newTagDrawable(
                            tagView.getBackground(), tag));
                    tagView.setSelected(wrapper.isSelected());
                    if (wrapper.isSelected()) {
                        mPrevSelectTagView = tagView;
                    }
                } else {
                    tagView.setVisibility(View.GONE);
                }
            }
        } else {
            rootView = inflater.inflate(R.layout.label_select_layout_grid, container, false);
            GridView gridView = (GridView) rootView.findViewById(R.id.grid);
            TagAdapter tagAdapter = new TagAdapter(getActivity(), mTagWrappers);
            gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
            gridView.setAdapter(tagAdapter);
            gridView.setOnItemClickListener(tagAdapter);
        }
        return rootView;
    }

    @Override
    public void onClick(View v) {
        Object obj = v.getTag();
        if (obj != null && obj instanceof TagWrapper) {
            onTagViewClick(v);
        }
    }

    private void onTagViewClick(View tagView) {
        TagWrapper wrapper = (TagWrapper) tagView.getTag();
        boolean selected = !wrapper.isSelected();
        boolean selectChanged = true;

        if (selected) {
            if (getSelectedCount() < getMaxSelect()) {
                mPrevSelectTagView = tagView;
            } else if (mPrevSelectTagView != null) {
                cancelSelect(mPrevSelectTagView);
                mPrevSelectTagView = tagView;
            } else {
                selectChanged = false;
            }
        }

        if (selectChanged) {
            wrapper.setSelected(!wrapper.isSelected());
            tagView.startAnimation(AnimationUtils.loadAnimation(
                    tagView.getContext(), R.anim.tag_scale_a));
            tagView.setSelected(wrapper.isSelected());
            onTagSelectChanged(wrapper);
        }
    }

    private void cancelSelect(View tagView) {
        TagWrapper wrapper = (TagWrapper) tagView.getTag();
        wrapper.setSelected(false);
        tagView.setSelected(wrapper.isSelected());
        onTagSelectChanged(wrapper);
        notifyPrevSelectCancel();
    }

    private void notifyPrevSelectCancel() {
        Activity activity = getActivity();
        if (activity != null) {
            Resources res = activity.getResources();
            String typeName = mTagType.getTypeName();
            int maxSelect = mTagType.getMaxSelect();
            String toast = res.getString(R.string.tag_cancel_toast, typeName,
                    maxSelect > 1 ? res.getString(R.string.tag_select_other, maxSelect)
                            : res.getString(R.string.tag_select_one));
            ShowToast.makeText(activity, R.drawable.emoji_sad, toast).show();
        }
    }

    private void parseArgs(Context context) {
        TagManager tagManager = TagManager.getInstance(context);
        UserTag[] myTags = tagManager.getUserTags();

        if (mTagType != null && mTagType.getTags() != null) {
            UserTag[] tags = mTagType.getTags();
            mTagWrappers = new TagWrapper[tags.length];

            for (int i = 0; i < tags.length; ++i) {
                mTagWrappers[i] = new TagWrapper(tags[i], isMyTag(tags[i], myTags));
            }
        } else {
            mTagWrappers = null;
        }
    }

    private boolean isMyTag(UserTag tag, UserTag[] myTags) {
        if (myTags != null && myTags.length > 0) {
            for (UserTag myTag : myTags) {
                if (tag.getTagId() == myTag.getTagId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getTagCount() {
        return mTagWrappers != null ? mTagWrappers.length : 0;
    }

    private void onTagSelectChanged(TagWrapper wrapper) {
        if (mTagSelectListener != null) {
            mTagSelectListener.onTagSelectChanged(wrapper.getUserTag(),
                    wrapper.isSelected());
        }
    }

    private Drawable newTagDrawable(Drawable defaultDrawable, UserTag tag) {
        int width = defaultDrawable.getIntrinsicWidth();
        int height = defaultDrawable.getIntrinsicHeight();
        Rect bounds = defaultDrawable.copyBounds();
        ShapeDrawable normal = new ShapeDrawable(new OvalShape());
        ShapeDrawable selected = new ShapeDrawable(new OvalShape());
        StateListDrawable drawable = new StateListDrawable();

        normal.getPaint().setColor(tag.parseTagColor());
        normal.setBounds(bounds);
        normal.setIntrinsicWidth(width);
        normal.setIntrinsicHeight(height);

        selected.getPaint().setColor(tag.parseTagSelectedColor());
        selected.setBounds(bounds);
        selected.setIntrinsicWidth(width);
        selected.setIntrinsicHeight(height);

        drawable.addState(new int[]{android.R.attr.state_selected}, selected);
        drawable.addState(new int[0], normal);
        drawable.setBounds(bounds);

        return drawable;
    }

    private class TagAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private LayoutInflater mInflater;
        private TagWrapper[] mTagWrappers;
        private int mPrevSelectPos;

        public TagAdapter(Context context, TagWrapper[] wrappers) {
            mInflater = LayoutInflater.from(context);
            mTagWrappers = wrappers;
            mPrevSelectPos = -1;
        }

        @Override
        public int getCount() {
            return mTagWrappers != null ? mTagWrappers.length : 0;
        }

        @Override
        public TagWrapper getItem(int position) {
            return mTagWrappers != null ? mTagWrappers[position] : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TagWrapper wrapper = getItem(position);
            UserTag tag = wrapper.getUserTag();
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.label_select_item, parent, false);
                holder.tagView = (TextView) convertView.findViewById(R.id.tag);
                convertView.setTag(holder);
                CompatUtils.setBackground(holder.tagView, newTagDrawable(
                        holder.tagView.getBackground(), tag));
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tagView.setText(tag.getTagName());
            holder.tagView.setSelected(wrapper.isSelected());
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object obj = parent.getItemAtPosition(position);
            if (obj != null && obj instanceof TagWrapper) {
                onTagItemClick(view, position);
            }
        }

        private void onTagItemClick(View view, int position) {
            TagWrapper wrapper = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();
            boolean selected = !wrapper.isSelected();

            if (selected) {
                if (getSelectedCount() < getMaxSelect()) {
                    mPrevSelectPos = position;
                } else if (mPrevSelectPos >= 0) {
                    cancelTagSelect(getItem(mPrevSelectPos));
                    mPrevSelectPos = position;
                } else {
                    mPrevSelectPos = getLastSelectPosition();
                    if (mPrevSelectPos >= 0) {
                        cancelTagSelect(getItem(mPrevSelectPos));
                        mPrevSelectPos = position;
                    }
                }
            }

            wrapper.setSelected(!wrapper.isSelected());
            view.startAnimation(AnimationUtils.loadAnimation(
                    view.getContext(), R.anim.tag_scale_a));
            holder.tagView.setSelected(wrapper.isSelected());
            onTagSelectChanged(wrapper);
            notifyDataSetChanged();
        }

        private int getLastSelectPosition() {
            int pos = -1;
            for (int i = 0; i < getCount(); ++i) {
                if (getItem(i).isSelected()) {
                    pos = i;
                }
            }
            return pos;
        }

        private void cancelTagSelect(TagWrapper wrapper) {
            wrapper.setSelected(false);
            notifyPrevSelectCancel();
        }

        private class ViewHolder {
            public TextView tagView;
        }
    }
}
