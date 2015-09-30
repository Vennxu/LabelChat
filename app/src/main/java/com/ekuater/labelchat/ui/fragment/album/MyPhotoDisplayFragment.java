package com.ekuater.labelchat.ui.fragment.album;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.fragment.friends.StrangerHelper;
import com.ekuater.labelchat.ui.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Leo on 2015/3/23.
 *
 * @author LinYong
 */
public class MyPhotoDisplayFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String EXTRA_ALBUM_PHOTO = "extra_album_photo";

    private static final int MSG_QUERY_LIKE_USERS = 101;

    private AlbumPhoto mAlbumPhoto;
    private AlbumManager mAlbumManager;
    private AvatarManager mAvatarManager;
    private LikeUserAdapter mLikeUserAdapter;
    private Handler mHandler;
    private StrangerHelper mStrangerHelper;
    private PhotoView mPhotoView;
    private GridView mGridView;
    private boolean mLikeUserQueried;

    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_QUERY_LIKE_USERS:
                    handleQueryLiteUsers((LiteStranger[]) msg.obj);
                    break;
                default:
                    handled = false;
                    break;
            }
            return handled;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();

        mAlbumManager = AlbumManager.getInstance(activity);
        mAvatarManager = AvatarManager.getInstance(activity);
        mLikeUserAdapter = new LikeUserAdapter(activity);
        mHandler = new Handler(mHandlerCallback);
        mStrangerHelper = new StrangerHelper(this);
        mLikeUserQueried = false;

        if (actionBar != null) {
            actionBar.hide();
        }
        mLikeUserAdapter.updateItems(setupAdapterItems(null));
        parseArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_photo_display, container, false);
        mPhotoView = (PhotoView) rootView.findViewById(R.id.photo);
        mGridView = (GridView) rootView.findViewById(R.id.grid);
        PhotoViewListener listener = new PhotoViewListener();
        mPhotoView.setOnViewTapListener(listener);
        mPhotoView.setOnPhotoTapListener(listener);
        if (mAlbumPhoto != null) {
            bindView();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLikeUserQueried) {
            mLikeUserQueried = true;
            queryLiteUsers();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if (obj != null && obj instanceof AdapterItem) {
            AdapterItem item = (AdapterItem) obj;
            item.onClick();
        }
    }

    private void parseArguments() {
        Bundle args = getArguments();
        mAlbumPhoto = (args != null) ? args.<AlbumPhoto>getParcelable(
                EXTRA_ALBUM_PHOTO) : null;
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void bindView() {
        mAlbumManager.displayPhoto(mAlbumPhoto.getPhoto(), mPhotoView,
                R.drawable.pic_loading);
        mGridView.setAdapter(mLikeUserAdapter);
        mGridView.setOnItemClickListener(this);
    }

    private void queryLiteUsers() {
        mAlbumManager.getPhotoLikeUsers(mAlbumPhoto.getPhotoId(),
                new AlbumManager.LikeUserObserver() {
                    @Override
                    public void onQueryResult(int result, LiteStranger[] users) {
                        mHandler.obtainMessage(MSG_QUERY_LIKE_USERS, users).sendToTarget();
                    }
                });
    }

    private void handleQueryLiteUsers(LiteStranger[] users) {
        mLikeUserAdapter.updateItems(setupAdapterItems(users));
    }

    private List<AdapterItem> setupAdapterItems(LiteStranger[] users) {
        List<AdapterItem> itemList = new ArrayList<>();

        itemList.add(new TipItem());
        if (users != null) {
            for (LiteStranger user : users) {
                itemList.add(new UserItem(mAvatarManager, user, mStrangerHelper));
            }
        }
        return itemList;
    }

    private class PhotoViewListener implements PhotoViewAttacher.OnViewTapListener,
            PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            finish();
        }

        @Override
        public void onViewTap(View view, float x, float y) {
            finish();
        }
    }

    private static class ViewHolder {
        public ImageView avatarView;
    }

    private static final int TYPE_TIP = 0;
    private static final int TYPE_USER = 1;
    private static final int TYPE_COUNT = 2;

    private interface AdapterItem {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getType();

        public void onClick();
    }

    private static class TipItem implements AdapterItem {

        public TipItem() {
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = inflater.inflate(R.layout.like_tip_item, parent, false);
            holder.avatarView = (ImageView) view.findViewById(R.id.avatar_image);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.avatarView.setImageResource(R.drawable.ic_photo_liked);
        }

        @Override
        public int getType() {
            return TYPE_TIP;
        }

        @Override
        public void onClick() {
        }
    }

    private static class UserItem implements AdapterItem {

        private AvatarManager avatarManager;
        private LiteStranger user;
        private StrangerHelper strangerHelper;

        public UserItem(AvatarManager avatarManager, LiteStranger user,
                        StrangerHelper strangerHelper) {
            this.avatarManager = avatarManager;
            this.user = user;
            this.strangerHelper = strangerHelper;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View view = inflater.inflate(R.layout.like_user_item, parent, false);
            holder.avatarView = (ImageView) view.findViewById(R.id.avatar_image);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (this.user != null) {
                MiscUtils.showAvatarThumb(this.avatarManager,
                        this.user.getAvatarThumb(), holder.avatarView);
            }
        }

        @Override
        public int getType() {
            return TYPE_USER;
        }

        @Override
        public void onClick() {
            this.strangerHelper.showStranger(this.user.getUserId());
        }
    }

    private static class LikeUserAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<AdapterItem> mItemList;

        public LikeUserAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mItemList = null;
        }

        public void updateItems(List<AdapterItem> items) {
            mItemList = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItemList != null ? mItemList.size() : 0;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getType();
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public AdapterItem getItem(int position) {
            if (0 <= position && position < getCount()) {
                return mItemList.get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AdapterItem item = getItem(position);
            if (convertView == null) {
                convertView = item.newView(mInflater, parent);
            }
            item.bindView(convertView);
            return convertView;
        }
    }
}
