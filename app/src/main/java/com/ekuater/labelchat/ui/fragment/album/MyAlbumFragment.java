package com.ekuater.labelchat.ui.fragment.album;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.delegate.AlbumManager;
import com.ekuater.labelchat.delegate.FunctionCallListener;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.UploadAlbumPhotoActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.util.L;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 2015/3/21.
 *
 * @author LinYong
 */
public class MyAlbumFragment extends Fragment {

    private static final String TAG = MyAlbumFragment.class.getSimpleName();

    private static final int MSG_GET_MY_PHOTOS = 101;
    private static final int MSG_PHOTO_UPLOADED = 102;
    private static final int MSG_PHOTO_DELETED = 103;

    private static final int REQUEST_SELECT_PHOTO = 1000;

    private AlbumManager mAlbumManager;
    private Handler mHandler;
    private AlbumAdapter mAlbumAdapter;
    private SimpleProgressHelper mProgressHelper;
    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = true;

            switch (msg.what) {
                case MSG_GET_MY_PHOTOS:
                    handleGetMyPhotos((AlbumPhoto[]) msg.obj);
                    break;
                case MSG_PHOTO_UPLOADED:
                    handlePhotoUploaded(msg.arg1, (AlbumPhoto) msg.obj);
                    break;
                case MSG_PHOTO_DELETED:
                    handleAlbumPhotoDeleted(msg.arg1, (AlbumPhoto) msg.obj, msg.arg2);
                    break;
                default:
                    handled = false;
                    break;
            }
            return handled;
        }
    };
    private AlbumItem.PhotoItemListener mPhotoItemListener = new AlbumItem.PhotoItemListener() {

        @Override
        public void onClick(AlbumPhoto photo) {
            launchMyAlbumGalleryUI(photo);
        }
    };

    private GridView mGridView;
    private AlbumPhoto[] mAlbumPhotos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();

        mAlbumManager = AlbumManager.getInstance(activity);
        mHandler = new Handler(mHandlerCallback);
        mAlbumAdapter = new AlbumAdapter(activity);
        mProgressHelper = new SimpleProgressHelper(this);
        mAlbumPhotos = null;

        if (actionBar != null) {
            actionBar.hide();
        }
        setupAlbumItems(null);
        getMyPhotos();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_album, container, false);
        ImageView icon = (ImageView) rootView.findViewById(R.id.icon);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title.setText(R.string.my_album);
        mGridView = (GridView) rootView.findViewById(R.id.grid);
        mGridView.setAdapter(mAlbumAdapter);
        mGridView.setOnItemClickListener(mAlbumAdapter);
        mGridView.setOnCreateContextMenuListener(mAlbumAdapter);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGridView.setOnCreateContextMenuListener(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_PHOTO:
                onSelectImage(resultCode, data);
                break;
            default:
                break;
        }
    }

    private void deleteAlbumPhoto(final AlbumPhoto photo, final int position) {
        mAlbumManager.deletePhoto(photo.getPhotoId(), new FunctionCallListener() {
            @Override
            public void onCallResult(int result, int errorCode, String errorDesc) {
                mHandler.obtainMessage(MSG_PHOTO_DELETED, result, position, photo).sendToTarget();
                L.d(TAG, "onPostResult(), result=" + result);
            }
        });
        mProgressHelper.show();
    }

    private void handleAlbumPhotoDeleted(int result, AlbumPhoto photo, int position) {
        mProgressHelper.dismiss();

        L.d(TAG, "handleAlbumPhotoDeleted(), result=%1$d, position=%2$d", result, position);

        if (result == FunctionCallListener.RESULT_CALL_SUCCESS) {
            AlbumItem.Item item = mAlbumAdapter.getItem(position);
            if (item instanceof AlbumItem.PhotoItem) {
                AlbumItem.PhotoItem photoItem = (AlbumItem.PhotoItem) item;
                if (photo.getPhotoId().equals(photoItem.getPhoto().getPhotoId())) {
                    mAlbumAdapter.deleteItem(position);
                    ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getString(R.string.delete_photo_done)).show();
                }
            }
        } else {
            ShowToast.makeText(getActivity(), R.drawable.emoji_cry, getString(R.string.delete_photo_failed)).show();

        }
    }

    private void getMyPhotos() {
        mAlbumManager.getMyPhotos(new AlbumManager.PhotoObserver() {
            @Override
            public void onQueryResult(int result, AlbumPhoto[] photos) {
                mHandler.obtainMessage(MSG_GET_MY_PHOTOS, result, 0, photos).sendToTarget();
            }
        });
    }

    private void handleGetMyPhotos(AlbumPhoto[] photos) {
        setupAlbumItems(photos);
        mAlbumPhotos = photos;
    }

    private void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void setupAlbumItems(AlbumPhoto[] photos) {
        List<AlbumItem.Item> itemList = new ArrayList<>();
        itemList.add(new AlbumItem.UploadItem(new AlbumItem.UploadItemListener() {
            @Override
            public void onClick() {
                showImageSelect();
            }
        }));
        if (photos != null) {
            for (AlbumPhoto photo : photos) {
                itemList.add(new AlbumItem.PhotoItem(photo, mAlbumManager,
                        mPhotoItemListener));
            }
        }
        mAlbumAdapter.setItems(itemList);
    }

    private void showImageSelect() {
        Intent intent = new Intent(getActivity(), UploadAlbumPhotoActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_PHOTO);
    }

    private void onSelectImage(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && "file".equals(uri.getScheme())) {
                uploadPhoto(new File(uri.getPath()), mAlbumManager.getRelatedUser());
            }
        }
    }

    private void uploadPhoto(final File photo, String userId) {
        try {
            mAlbumManager.uploadPhoto(photo, userId, new AlbumManager.UploadPhotoObserver() {
                @Override
                public void onUploadResult(int result, AlbumPhoto uploadedPhoto) {
                    mHandler.obtainMessage(MSG_PHOTO_UPLOADED, result, 0, uploadedPhoto)
                            .sendToTarget();
                    if (!photo.delete()) {
                        L.w(TAG, "delete file failed, path=" + photo.getPath());
                    }
                }
            });
            mProgressHelper.show();
        } catch (FileNotFoundException e) {
            L.w(TAG, e);
            ShowToast.makeText(getActivity(), R.drawable.emoji_sad, getString(R.string.photo_not_exist)).show();

        }
    }

    private void handlePhotoUploaded(int result, AlbumPhoto photo) {
        mProgressHelper.dismiss();
        if (photo != null) {
            mAlbumAdapter.addAlbumPhoto(new AlbumItem.PhotoItem(photo,
                    mAlbumManager, mPhotoItemListener));
        }

        Activity activity = getActivity();
        if (activity != null) {
            if (result == AlbumManager.UPLOAD_SUCCESS) {
                ShowToast.makeText(activity, R.drawable.emoji_smile,
                        activity.getString(R.string.photo_upload_success)).show();
            } else {
                ShowToast.makeText(activity, R.drawable.emoji_cry,
                        activity.getString(R.string.photo_upload_failed)).show();
            }
        }
    }

    private void launchMyAlbumGalleryUI(AlbumPhoto photo) {
        if (mAlbumPhotos != null && mAlbumPhotos.length > 0) {
            UILauncher.launchMyAlbumGalleryUI(getActivity(), mAlbumPhotos,
                    getDefaultPhotoItem(photo));
        }
    }

    private int getDefaultPhotoItem(AlbumPhoto photo) {
        String photoId = photo.getPhotoId();

        for (int i = 0; i < mAlbumPhotos.length; ++i) {
            if (photoId.equals(mAlbumPhotos[i].getPhotoId())) {
                return i;
            }
        }
        return 0;
    }

    private class AlbumAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener,
            View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

        private LayoutInflater mInflater;
        private List<AlbumItem.Item> mItemList;

        public AlbumAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setItems(List<AlbumItem.Item> itemList) {
            mItemList = itemList;
            notifyDataSetChanged();
        }

        public void deleteItem(int position) {
            if (0 <= position && position < getCount() && mItemList != null) {
                mItemList.remove(position);
                notifyDataSetChanged();
            }
        }

        public void addAlbumPhoto(AlbumItem.Item item) {
            if (mItemList == null) {
                mItemList = new ArrayList<>();
            }
            mItemList.add(item);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItemList != null ? mItemList.size() : 0;
        }

        @Override
        public AlbumItem.Item getItem(int position) {
            return mItemList != null ? mItemList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getType();
        }

        @Override
        public int getViewTypeCount() {
            return AlbumItem.getTypeCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumItem.Item item = getItem(position);
            if (convertView == null) {
                convertView = item.newView(mInflater, parent);
            }
            item.bindView(convertView);
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object item = parent.getItemAtPosition(position);
            if (item != null && item instanceof AlbumItem.Item) {
                ((AlbumItem.Item) item).onClick();
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            AdapterView.AdapterContextMenuInfo ctxMenuInfo
                    = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int position = ctxMenuInfo.position;

            if (getItem(position).getType() == AlbumItem.TYPE_PHOTO) {
                MenuItem deleteItem = menu.add(0, R.id.delete, Menu.NONE, R.string.delete);
                deleteItem.setOnMenuItemClickListener(this);
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            AdapterView.AdapterContextMenuInfo ctxMenuInfo
                    = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int position = ctxMenuInfo.position;
            AlbumItem.PhotoItem photoItem = (AlbumItem.PhotoItem) getItem(position);
            boolean handled = true;

            switch (item.getItemId()) {
                case R.id.delete:
                    deleteAlbumPhoto(photoItem.getPhoto(), position);
                    break;
                default:
                    handled = false;
                    break;
            }
            return handled;
        }
    }
}
