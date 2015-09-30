package com.ekuater.labelchat.ui.fragment.usershowpage;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;


/**
 * Created by Leo on 2015/2/3.
 *
 * @author LinYong
 */
public class MyThrowPhotosPage extends BasePage {

    private static final int MSG_GET_MY_THROW_PHOTOS = 101;
    private static final int MSG_HANDLE_GET_MY_THROW_PHOTOS = 102;
    private static final int MSG_DELETE_THROW_PHOTOS=103;

    private ThrowPhotoManager mThrowPhotoManager;
    private ThrowPhotoAdapter mThrowPhotoAdapter;
    private SimpleProgressDialog mProgressDialog;
    private int mDeleteIndex;
    private String mThrowPhotoId;

    private ThrowPhotoAdapter.DeleteListener deleteListener = new ThrowPhotoAdapter.DeleteListener() {
        @Override
        public void onDelete(String throwPhotoId,int position) {
            mDeleteIndex = position;
            mThrowPhotoId = throwPhotoId;
            showConfirmDialog();

        }
    };
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
                case MSG_DELETE_THROW_PHOTOS:
                    handlerDeleteThrowPhoto((ThrowPhotoManager.ResultCode)msg.obj,msg.arg1);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean mNowLoading;
    private View mForegroundView;

    public MyThrowPhotosPage(Fragment fragment) {
        super(fragment);
        mThrowPhotoAdapter = new ThrowPhotoAdapter(mContext,deleteListener);
        mThrowPhotoManager = ThrowPhotoManager.getInstance(mContext);
        // To get my throw photo
        mNowLoading = false;
        mForegroundView = null;
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
        return mThrowPhotoAdapter;
    }

    @Override
    public boolean isLoading() {
        return mNowLoading;
    }

    @Override
    public void onAddToContentForeground(ViewGroup container) {
        if (mForegroundView == null) {
            mForegroundView = LayoutInflater.from(mContext).inflate(
                    R.layout.no_throw_photo_layout, container, false);
            mForegroundView.findViewById(R.id.btn_throw_photo).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UILauncher.launchThrowPhotoUI(mContext);
                        }
                    });
        }
        ViewGroup parent = (ViewGroup) mForegroundView.getParent();
        if (parent != null) {
            parent.removeView(mForegroundView);
        }
        container.addView(mForegroundView);
        mForegroundView.setVisibility(mThrowPhotoAdapter.getCount() > 0
                ? View.GONE : View.VISIBLE);
    }

    private void getMyThrowPhotos() {
        mNowLoading = true;
        mThrowPhotoManager.getMyThrowPhotos(
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
    private void deleteThrowPhotos(){
        ThrowPhotoManager.DeleteThrowPhotoObserver observer=new ThrowPhotoManager.DeleteThrowPhotoObserver() {
            @Override
            public void onDeleteResult(ThrowPhotoManager.ResultCode result) {
                Message message=Message.obtain(mHandler,MSG_DELETE_THROW_PHOTOS,mDeleteIndex,0,result);
                mHandler.sendMessage(message);
            }
        };
        mThrowPhotoManager.deleteThrowPhoto(mThrowPhotoId,observer);
    }
    private void handleGetMyThrowPhotos(ThrowPhoto[] throwPhotos) {
        int count = throwPhotos != null ? throwPhotos.length : 0;
        if (count != mThrowPhotoAdapter.getCount()) {
            mThrowPhotoAdapter.updateThrowPhotos(throwPhotos);
        }
        mNowLoading = false;
        postEvent(new PageEvent(this, PageEvent.Event.LOAD_DONE));

        if (mForegroundView != null) {
            mForegroundView.setVisibility(mThrowPhotoAdapter.getCount() > 0
                    ? View.GONE : View.VISIBLE);
        }
    }
    private void handlerDeleteThrowPhoto(ThrowPhotoManager.ResultCode resultCode,int position){
        dismissProgressDialog();
        if (resultCode== ThrowPhotoManager.ResultCode.SUCCESS){
            mThrowPhotoAdapter.deleteThrowPhoto(position);
        }else{
            Toast.makeText(mContext,R.string.labelstory_delete, Toast.LENGTH_SHORT).show();
        }
    }
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
    private ConfirmDialogFragment.AbsConfirmListener confirmListener =new ConfirmDialogFragment.AbsConfirmListener(){
        @Override
        public void onConfirm() {
            showProgressDialog();
            deleteThrowPhotos();
        }

        @Override
        public void onCancel() {
            super.onCancel();
        }

    };

    private void showConfirmDialog(){
        ConfirmDialogFragment.UiConfig uiConfig=new ConfirmDialogFragment.UiConfig(mFragment.getActivity().getString(R.string.throwphoto_is_delete),null);
        ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(uiConfig,confirmListener);
        confirmDialogFragment.show(getFragmentManager(),"MyLabelStoryPage");
    }
}
