package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.throwphoto.ThrowPhotoShowFragment;

/**
 * Created by Leo on 2015/2/4.
 *
 * @author LinYong
 */
public class UserThrowPhotosPage extends BasePage {

    private static final int MSG_GET_MY_THROW_PHOTOS = 101;
    private static final int MSG_HANDLE_GET_MY_THROW_PHOTOS = 102;

    private String mUserId;
    private ThrowPhotoManager mThrowPhotoManager;
    private ThrowPhotoAdapter mThrowPhotoAdapter;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_MY_THROW_PHOTOS:
                    getMyThrowPhotos();
                    break;
                case MSG_HANDLE_GET_MY_THROW_PHOTOS:
                    handleGetMyThrowPhotos((ThrowPhoto[]) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean mNowLoading;
    private View mBackgroundView;

    private final AdapterView.OnItemClickListener mItemClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            Object item = parent.getAdapter().getItem(position);
            if (item instanceof ThrowPhoto) {
                ThrowPhoto throwPhoto = (ThrowPhoto) item;
                Bundle arguments = new Bundle();
                arguments.putParcelable(ThrowPhotoShowFragment.EXTRA_THROW_PHOTO,
                        throwPhoto);
                UILauncher.launchFragmentInNewActivity(mContext,
                        ThrowPhotoShowFragment.class, arguments);
            }
        }
    };

    public UserThrowPhotosPage(Fragment fragment, String userId) {
        super(fragment);
        mUserId = userId;
        mThrowPhotoAdapter = new ThrowPhotoAdapter(mContext);
        mThrowPhotoManager = ThrowPhotoManager.getInstance(mContext);
        // To get my throw photo
        mNowLoading = false;
        mBackgroundView = null;
        mHandler.sendEmptyMessage(MSG_GET_MY_THROW_PHOTOS);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MSG_GET_MY_THROW_PHOTOS);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_GET_MY_THROW_PHOTOS);
    }

    @Override
    public ListAdapter getContentAdapter() {
        return mThrowPhotoAdapter;
    }

    @Override
    public AdapterView.OnItemClickListener getContentItemClickListener() {
        return mItemClickListener;
    }

    @Override
    public boolean isLoading() {
        return mNowLoading;
    }

    @Override
    public void onAddToContentBackground(ViewGroup container) {
        if (mBackgroundView == null) {
            mBackgroundView = LayoutInflater.from(mContext).inflate(
                    R.layout.user_no_throw_photo_layout, container, false);
        }
        ViewGroup parent = (ViewGroup) mBackgroundView.getParent();
        if (parent != null) {
            parent.removeView(mBackgroundView);
        }
        container.addView(mBackgroundView);
        mBackgroundView.setVisibility(mThrowPhotoAdapter.getCount() > 0
                ? View.GONE : View.VISIBLE);
    }

    private void getMyThrowPhotos() {
        mNowLoading = true;
        mThrowPhotoManager.getUserThrowPhotos(mUserId,
                new ThrowPhotoManager.ThrowPhotoQueryObserver() {
                    @Override
                    public void onQueryResult(ThrowPhotoManager.ResultCode result,
                                              ThrowPhoto[] throwPhotos) {
                        Message msg = mHandler.obtainMessage(MSG_HANDLE_GET_MY_THROW_PHOTOS,
                                throwPhotos);
                        mHandler.sendMessage(msg);
                    }
                });
    }

    private void handleGetMyThrowPhotos(ThrowPhoto[] throwPhotos) {
        int count = throwPhotos != null ? throwPhotos.length : 0;
        if (count != mThrowPhotoAdapter.getCount()) {
            mThrowPhotoAdapter.updateThrowPhotos(throwPhotos);
        }
        mNowLoading = false;
        postEvent(new PageEvent(this, PageEvent.Event.LOAD_DONE));

        if (mBackgroundView != null) {
            mBackgroundView.setVisibility(mThrowPhotoAdapter.getCount() > 0
                    ? View.GONE : View.VISIBLE);
        }
    }
}
