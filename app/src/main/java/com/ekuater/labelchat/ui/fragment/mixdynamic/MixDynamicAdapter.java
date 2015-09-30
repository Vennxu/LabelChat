package com.ekuater.labelchat.ui.fragment.mixdynamic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicType;
import com.ekuater.labelchat.datastruct.mixdynamic.DynamicWrapper;
import com.ekuater.labelchat.delegate.ContactsManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Leo on 2015/4/16.
 *
 * @author LinYong
 */
public class MixDynamicAdapter extends BaseAdapter implements AbsListView.RecyclerListener {

    private final Context mContext;
    private final boolean mLoadComments;
    private final EventBus mEventBus;
    private final TotalDynamicListener mDynamicListener;
    private final LayoutInflater mInflater;
    private List<DynamicWrapper> mWrapperList;

    public MixDynamicAdapter(Context context, boolean loadComments, EventBus eventBus,
                             TotalDynamicListener listener) {
        mContext = context;
        mLoadComments = loadComments;
        mEventBus = eventBus;
        mDynamicListener = listener;
        mInflater = LayoutInflater.from(context);
    }

    public void updateWrapperList(List<DynamicWrapper> wrapperList) {
        mWrapperList = wrapperList;
        notifyDataSetChanged();
    }

    public void addWrapperList(List<DynamicWrapper> wrapperList) {
        if (mWrapperList != null) {
            mWrapperList.addAll(wrapperList);
        } else {
            mWrapperList = wrapperList;
        }
        notifyDataSetChanged();
    }

    public void addNewWrapper(DynamicWrapper wrapper) {
        if (wrapper == null) {
            return;
        }

        if (mWrapperList == null) {
            mWrapperList = new ArrayList<>();
        }
        mWrapperList.add(0, wrapper);
        notifyDataSetChanged();
    }

    public void removeWrapper(int position) {
        if (checkPosition(position)) {
            mWrapperList.remove(position);
            notifyDataSetChanged();
        }
    }

    private boolean checkPosition(int position) {
        final int count = getCount();
        return 0 <= position && position < count;
    }

    @Override
    public int getCount() {
        return mWrapperList != null ? mWrapperList.size() : 0;
    }

    @Override
    public DynamicWrapper getItem(int position) {
        return checkPosition(position) ? mWrapperList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().getType();
    }

    @Override
    public int getViewTypeCount() {
        return DynamicType.values().length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DynamicRender render;

        if (convertView == null) {
            render = getStoryRender(getItemViewType(position));
            convertView = render.getView(mInflater, parent);
            convertView.setTag(render);
            render.bindEvents();
        } else {
            render = (DynamicRender) convertView.getTag();
        }

        render.bindView(getItem(position), position);
        return convertView;
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        Object tag = view.getTag();
        if (tag != null && tag instanceof DynamicRender) {
            DynamicRender render = (DynamicRender) tag;
            render.unbindView();
        }
    }

    private DynamicRender getStoryRender(int viewType) {
        final DynamicRender render;

        switch (DynamicType.toType(viewType)) {
            case TXT:
                render = new TxtDynamicRender(mContext, mLoadComments, mDynamicListener);
                break;
            case AUDIO:
                render = new AudioDynamicRender(mContext, mLoadComments, mEventBus,
                        mDynamicListener);
                break;
            case CONFIDE:
                render = new ConfideDynamicRender(mContext, mLoadComments, mDynamicListener);
                break;
            case BANKNOTE:
                render = new BanknoteDynamicRender(mContext, mLoadComments, mDynamicListener);
                break;
            case ONLINEAUDIO:
                render = new AudioDynamicRender(mContext, mLoadComments, mEventBus,
                    mDynamicListener);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported DynamicType!");
        }
        return render;
    }

    public static UserContact getEmpContact(Context context, Stranger stranger) {
        UserContact userContact = null;
        ContactsManager contactsManager = ContactsManager.getInstance(context);
        UserContact[] userContacts = contactsManager.getAllUserContact();
        if (userContacts != null && userContacts.length > 0) {
            for (UserContact contact : contactsManager.getAllUserContact()) {
                if (contact.getUserId().equals(stranger.getUserId())) {
                    userContact = contact;
                    break;
                } else {
                    userContact = new UserContact(stranger);
                    break;
                }
            }
            return userContact;
        } else {
            return new UserContact(stranger);
        }
    }
}
