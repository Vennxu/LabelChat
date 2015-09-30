package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.CommandFields;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Leo on 2015/1/9.
 *
 * @author LinYong
 */
public class ThrowPhotoShowFragment extends Fragment {

    public static final String EXTRA_THROW_PHOTO = "extra_throw_photo";
    public static final String EXTRA_SCENARIO_PICK = "extra_scenario_pick";

    private static final int MSG_HANDLE_PICK_RESULT = 101;
    private static final int MSG_HANDLE_THROW_PHOTO_RESULT = 102;

    private static class PickResult {

        public final ThrowPhotoManager.ResultCode result;
        public final Stranger userInfo;

        public PickResult(ThrowPhotoManager.ResultCode result,
                          Stranger userInfo) {
            this.result = result;
            this.userInfo = userInfo;
        }
    }

    private ThrowPhotoManager mManager;
    private ThrowPhoto mThrowPhoto;
    private boolean mScenarioPick;
    private Stranger mThrowUser;
    private MenuItem mThrowUserMenu;
    private PhotoView mPhotoView;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_PICK_RESULT:
                    handlePickResult((PickResult) msg.obj);
                    break;
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
        parseArguments();
        final Activity activity = getActivity();
        mManager = ThrowPhotoManager.getInstance(activity);
        mThrowUser = null;

        ActionBar actionBar = activity.getActionBar();

        if (isOneself()) {
            setHasOptionsMenu(false);
            if (actionBar != null) {
                actionBar.setTitle(R.string.preview);
            }
        } else {
            setHasOptionsMenu(mScenarioPick);
            if (actionBar != null) {
                actionBar.setTitle(R.string.show_photos);
            }
            pickThisPhoto();
        }

    }

    private void load() {
        Bitmap bitmap = mManager.getPhotoItemBitmap(mThrowPhoto.getPhotoArray()[0].getPhoto(),
                new ShortUrlImageLoadListener() {
                    @Override
                    public void onLoadFailed(String url, LoadFailType loadFailType) {
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadComplete(String url, Bitmap loadedImage) {
                        loadThrowPhoto(loadedImage);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
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
            mPhotoView.setImageBitmap(mBitmap);
        } else {
            mBitmap = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.throw_photo_show_item, container, false);
        mPhotoView = (PhotoView) view.findViewById(R.id.photo);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        load();
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        createContextMenu(mPhotoView);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.throw_photo_show_menu, menu);
        mThrowUserMenu = menu.findItem(R.id.friend_detail);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateThrowUserMenuEnable();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.friend_detail:
                showThrowUserInfo();
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    private void updateThrowUserMenuEnable() {
        if (mThrowUserMenu != null) {
            mThrowUserMenu.setEnabled(mThrowUser != null);
        }
    }

    private void parseArguments() {
        Bundle args = getArguments();
        mThrowPhoto = args.getParcelable(EXTRA_THROW_PHOTO);
        mScenarioPick = args.getBoolean(EXTRA_SCENARIO_PICK, false);
    }

    private boolean isOneself() {
        SettingHelper helper = SettingHelper.getInstance(getActivity());
        return mThrowPhoto.getUserId().equals(helper.getAccountUserId());
    }

    private void pickThisPhoto() {
        mThrowUser = null;
        updateThrowUserMenuEnable();
        String scenario = mScenarioPick ? CommandFields.Normal.SCENARIO_PICK
                : CommandFields.Normal.SCENARIO_BROWSE;
        mManager.pickThrowPhoto(mThrowPhoto.getId(), scenario,
                new ThrowPhotoManager.PickThrowPhotoObserver() {
                    @Override
                    public void onPickResult(ThrowPhotoManager.ResultCode result,
                                             Stranger userInfo) {
                        Message msg = mHandler.obtainMessage(MSG_HANDLE_PICK_RESULT,
                                new PickResult(result, userInfo));
                        mHandler.sendMessage(msg);
                    }
                });
    }

    private void handlePickResult(PickResult pickResult) {
        final ThrowPhotoManager.ResultCode result = pickResult.result;
        final Stranger userInfo = pickResult.userInfo;

        switch (result) {
            case SUCCESS:
                mThrowUser = userInfo;
                updateThrowUserMenuEnable();
                updateTitle();
                break;
            default:
                break;
        }
    }

    private void updateTitle() {
        if (mThrowUser != null) {
            Activity activity = getActivity();
            ActionBar actionBar = (activity != null) ? activity.getActionBar() : null;
            String title = mThrowUser.getNickname();
            if (actionBar != null && !TextUtils.isEmpty(title)) {
                actionBar.setTitle(title);
            }
        }
    }

    private void showThrowUserInfo() {
        if (mThrowUser != null) {
            UILauncher.launchStrangerDetailUI(getActivity(), mThrowUser);
        }
    }
}