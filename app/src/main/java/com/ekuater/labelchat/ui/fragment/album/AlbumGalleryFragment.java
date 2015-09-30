package com.ekuater.labelchat.ui.fragment.album;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/24.
 *
 * @author LinYong
 */
public class AlbumGalleryFragment extends Fragment implements Handler.Callback,
        ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final String EXTRA_QUERY_USER = "extra_query_user";
    public static final String EXTRA_PHOTO_ID = "extra_photo_id";

    private static final int MSG_GET_USER_PHOTOS = 101;
    private static final int MSG_LIKE_PHOTO = 102;
    private static final int MSG_SEND_PHOTO_NOTIFY = 103;

    private AlbumManager mAlbumManager;
    private Handler mHandler;
    private PhotoPagerAdapter mPagerAdapter;
    private SimpleProgressHelper mProgressHelper;
    private LiteStranger mQueryUser;
    private TextView mIdxTextView;
    private ViewPager mViewPager;
    private TextView mLikeView;
    private TextView mSeeView;
    private TextView mRemindView;
    private TextView mPraiseNum;
    private TextView mNotifyUploadNum;
    private String photoId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();

        mAlbumManager = AlbumManager.getInstance(activity);
        mHandler = new Handler(this);
        mPagerAdapter = new PhotoPagerAdapter(getChildFragmentManager());
        mProgressHelper = new SimpleProgressHelper(this);
        if (actionBar != null) {
            actionBar.hide();
        }
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        parseArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_gallery, container, false);
        mIdxTextView = (TextView) rootView.findViewById(R.id.title);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mLikeView = (TextView) rootView.findViewById(R.id.like);
        mSeeView = (TextView) rootView.findViewById(R.id.see);
        mRemindView = (TextView) rootView.findViewById(R.id.remind);
        mPraiseNum = (TextView) rootView.findViewById(R.id.praise_num);
        mNotifyUploadNum = (TextView) rootView.findViewById(R.id.notify_upload_num);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);
        mLikeView.setOnClickListener(this);
        mSeeView.setOnClickListener(this);
        mRemindView.setOnClickListener(this);
        updateIdxText();
        updateLikeRemindState();
        return rootView;
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_GET_USER_PHOTOS:
                handleGetUserPhotos((AlbumPhoto[]) msg.obj);
                break;
            case MSG_LIKE_PHOTO:
                handleLikePhoto(msg.arg1, (AlbumPhoto) msg.obj);
                break;
            case MSG_SEND_PHOTO_NOTIFY:
                handleSendPhotoNotify((NotifyResult) msg.obj);
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        updateIdxText();
        updateLikeRemindState();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.like:
                onLikeClick();
                break;
            case R.id.see:
                onSeeClick();
                break;
            case R.id.remind:
                onRemindClick();
                break;
            default:
                break;
        }
    }

    private void updateIdxText() {
        int count = mViewPager.getAdapter().getCount();
        int idx = (count > 0) ? mViewPager.getCurrentItem() + 1 : 0;
        mIdxTextView.setText(getString(R.string.photo_index, idx, count));
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void parseArguments() {
        Bundle bundle = getArguments();
        mQueryUser = bundle != null ? bundle.<LiteStranger>getParcelable(EXTRA_QUERY_USER) : null;
        photoId = bundle != null ? bundle.getString(EXTRA_PHOTO_ID):null;
        String queryUserId = mQueryUser != null ? mQueryUser.getUserId() : "";

        if (!TextUtils.isEmpty(queryUserId)) {
            mAlbumManager.getUserPhotos(queryUserId, new AlbumManager.PhotoObserver() {
                @Override
                public void onQueryResult(int result, AlbumPhoto[] photos) {
                    mHandler.obtainMessage(MSG_GET_USER_PHOTOS, photos).sendToTarget();
                }
            });
            mProgressHelper.show();
        } else {
            finish();
        }
    }

    private void handleGetUserPhotos(AlbumPhoto[] photos) {
        mProgressHelper.dismiss();
        mPagerAdapter.updateAlbumPhotos(photos);
        if (!TextUtil.isEmpty(photoId) && photos != null) {
            for (int i = 0; i < photos.length; i++) {
                if (photos[i].getPhotoId().equals(photoId)) {
                    mViewPager.setCurrentItem(i);
                    break;
                }
            }
        }
        updateIdxText();
        updateLikeRemindState();
    }

    private void onLikeClick() {
        final AlbumPhoto photo = getCurrentPhoto();
        if (photo == null || checkOperationDone(photo.isLiked(), R.string.has_liked)) {
            return;
        }
        mAlbumManager.likePhoto(photo.getPhotoId(), new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                if (result == 0) {
                    photo.setPraiseNum(photo.getPraiseNum() + 1);
                }
                mHandler.obtainMessage(MSG_LIKE_PHOTO, result, errorCode, photo)
                        .sendToTarget();
            }
        });
        mProgressHelper.show();
    }

    private void handleLikePhoto(int result, AlbumPhoto photo) {
        mProgressHelper.dismiss();
        doneToast(R.string.liked_done);
        if (result == FunctionCallListener.RESULT_CALL_SUCCESS) {
            photo.setLiked(true);
            updateLikeRemindState();
        }
    }

    private void onSeeClick() {
        AlbumPhoto photo = getCurrentPhoto();
        if (photo == null || checkOperationDone(photo.isSaw(), R.string.has_reminded)) {
            return;
        }
        sendHasReadPhotoNotify();
    }

    private void onRemindClick() {
        AlbumPhoto photo = getCurrentPhoto();
        if (photo == null || checkOperationDone(photo.isReminded(), R.string.has_upload_reminded)) {
            return;
        }
        sendUploadMorePhotoNotify();
    }

    private boolean checkOperationDone(boolean done, CharSequence text) {
        if (done) {
            ShowToast.makeText(getActivity(), R.drawable.emoji_sad,
                    text, ShowToast.LENGTH_SHORT).show();
        }
        return done;
    }

    private boolean checkOperationDone(boolean done, int stringId) {
        return checkOperationDone(done, getString(stringId));
    }

    private void sendHasReadPhotoNotify() {
        sendPhotoNotify(AlbumManager.NOTIFY_TYPE_HAS_SEEN);
    }

    private void sendUploadMorePhotoNotify() {
        sendPhotoNotify(AlbumManager.NOTIFY_TYPE_UPLOAD_MORE);
    }

    private static class NotifyResult {

        public String notifyType;
        public AlbumPhoto photo;
        public int result;

        public NotifyResult(String notifyType, AlbumPhoto photo, int result) {
            this.notifyType = notifyType;
            this.photo = photo;
            this.result = result;
        }
    }

    private void sendPhotoNotify(final String notifyType) {
        final AlbumPhoto photo = getCurrentPhoto();
        if (photo != null) {
            mAlbumManager.sendPhotoNotify(photo, notifyType, new FunctionCallListener() {
                @Override
                public void onCallResult(int result, int errorCode, String errorDesc) {
                    if (result == 0) {
                        photo.setNotifyUploadNum(photo.getNotifyUploadNum() + 1);
                    }
                    mHandler.obtainMessage(MSG_SEND_PHOTO_NOTIFY,
                            new NotifyResult(notifyType, photo, result))
                            .sendToTarget();
                }
            });
            mProgressHelper.show();
        }
    }

    private void handleSendPhotoNotify(NotifyResult result) {
        mProgressHelper.dismiss();
        doneToast(result.notifyType.equals(AlbumManager.NOTIFY_TYPE_UPLOAD_MORE)
                ? R.string.upload_reminded_done : R.string.reminded_done);
        if (result.photo != null && result.result == FunctionCallListener.RESULT_CALL_SUCCESS) {
            switch (result.notifyType) {
                case AlbumManager.NOTIFY_TYPE_HAS_SEEN:
                    result.photo.setSaw(true);
                    break;
                case AlbumManager.NOTIFY_TYPE_UPLOAD_MORE:
                    result.photo.setReminded(true);
                    break;
                default:
                    break;
            }
            updateLikeRemindState();
        }
    }

    private void doneToast(String toastText) {
        ShowToast.makeText(getActivity(), R.drawable.emoji_smile,
                toastText, ShowToast.LENGTH_SHORT).show();
    }

    private void doneToast(int stringId) {
        doneToast(getString(stringId, getGender()));
    }

    private String getGender() {
        return getString((mQueryUser.getGender() == ConstantCode.USER_SEX_FEMALE)
                ? R.string.her : R.string.he);
    }

    private AlbumPhoto getCurrentPhoto() {
        int idx = mViewPager.getCurrentItem();
        int count = mPagerAdapter.getCount();
        PhotoDisplayFragment fragment = (0 <= idx && idx < count)
                ? mPagerAdapter.getItem(idx) : null;
        return (fragment != null) ? fragment.getAlbumPhoto() : null;
    }

    private void updateLikeRemindState() {
        AlbumPhoto photo = getCurrentPhoto();
        if (photo != null) {
            mLikeView.setSelected(photo.isLiked());
            mPraiseNum.setText(photo.getPraiseNum() > 0 ? String.valueOf(photo.getPraiseNum()) : "");
            mSeeView.setSelected(photo.isSaw());
            mNotifyUploadNum.setText(photo.getNotifyUploadNum() > 0 ? String.valueOf(photo.getPraiseNum()) : "");
            mRemindView.setSelected(photo.isReminded());
        }
    }

    private static class PhotoPagerAdapter extends FragmentPagerAdapter {

        private List<PhotoDisplayFragment> mPageList;

        public PhotoPagerAdapter(FragmentManager fm) {
            super(fm);
            mPageList = setupPages(null);
        }

        public void updateAlbumPhotos(AlbumPhoto[] photos) {
            mPageList = setupPages(photos);
            notifyDataSetChanged();
        }

        private List<PhotoDisplayFragment> setupPages(AlbumPhoto[] photos) {
            List<PhotoDisplayFragment> pageList = new ArrayList<>();

            if (photos != null) {
                for (AlbumPhoto photo : photos) {
                    pageList.add(newPage(photo));
                }
            }
            return pageList;
        }

        private PhotoDisplayFragment newPage(AlbumPhoto photo) {
            PhotoDisplayFragment page = new PhotoDisplayFragment();
            page.setAlbumPhoto(photo);
            return page;
        }

        @Override
        public PhotoDisplayFragment getItem(int position) {
            return mPageList.get(position);
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }
    }
}