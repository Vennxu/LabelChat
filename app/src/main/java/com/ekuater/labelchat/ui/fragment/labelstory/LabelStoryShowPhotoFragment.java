package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;

/**
 * Created by wenxiang on 2015/2/28.
 */
public class LabelStoryShowPhotoFragment extends Fragment {

    private DragImageView mShowPhoto;
    private ProgressBar mProgressBar;
    private static final int MSG_HANDLE_PICK_RESULT = 101;
    private static final int MSG_HANDLE_THROW_PHOTO_RESULT = 102;
    private String mUrl;
    private Bitmap mBitmap;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_THROW_PHOTO_RESULT:
                    handlerLoadThrowPhoto(msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        paramArgment();
    }

    private void paramArgment() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.hide();
        Bundle argment = getArguments();
        if (argment != null) {
            mUrl = argment.getString(LabelStoryUtils.SHOW_PHOTO_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.throw_photo_show_item, container, false);
        mShowPhoto = (DragImageView) view.findViewById(R.id.photo);
//        mShowPhoto.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                KeepPhotoDialog keepPhotoDialog = new KeepPhotoDialog(mBitmap);
//                keepPhotoDialog.show(getFragmentManager(),"LabelStoryShowPhotoFragment");
//                return false;
//            }
//        });
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        loadContentImage();
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        createContextMenu(mShowPhoto);
        return view;
    }
    private void createContextMenu(final View view) {
        view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.save_picture_menu, menu);
                menu.findItem(R.id.save).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mBitmap, null, null);
                        Toast.makeText(getActivity(), R.string.saved + path, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });
    }

    private ShortUrlImageLoadListener mLoadListener = new ShortUrlImageLoadListener() {
        @Override
        public void onLoadFailed(String shortUrl, LoadFailType loadFailType) {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadComplete(String shortUrl, Bitmap loadedImage) {
            loadThrowPhoto(loadedImage);
            mProgressBar.setVisibility(View.GONE);
        }
    };

    public void loadContentImage() {
        Bitmap bitmap = null;
        if (mUrl != null) {
            mShowPhoto.setVisibility(View.VISIBLE);
            bitmap = AvatarManager.getInstance(getActivity()).getLabelStoryImage(
                    mUrl, mLoadListener);
        }
        if (bitmap != null) {
            loadThrowPhoto(bitmap);
        }
    }

    private void loadThrowPhoto(Bitmap bitmap) {
        Message message = mHandler.obtainMessage(MSG_HANDLE_THROW_PHOTO_RESULT, bitmap);
        mHandler.sendMessage(message);
    }

    private void handlerLoadThrowPhoto(Object object) {
        if (object instanceof Bitmap) {
            mBitmap = (Bitmap) object;
            BitmapDrawable bd=new BitmapDrawable(mBitmap);
            mShowPhoto.setmDrawable(bd);
        } else {
            mBitmap = null;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBitmap!=null){
            mBitmap.recycle();
            mBitmap=null;
        }
    }
}
