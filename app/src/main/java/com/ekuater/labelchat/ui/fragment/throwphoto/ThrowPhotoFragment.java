package com.ekuater.labelchat.ui.fragment.throwphoto;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.ThrowPhoto;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.ThrowPhotoManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.SimpleProgressDialog;
import com.ekuater.labelchat.util.L;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Leo on 2015/1/10.
 *
 * @author LinYong
 */
public class ThrowPhotoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = ThrowPhotoFragment.class.getSimpleName();
    private static final double MAX_THROW_DISTANCE = 5 * 1000; // 5km
    private static final long PLAY_THROW_SOUND_DELAY = 300;
    private static final int REQUEST_SELECT_PHOTO = 1001;

    private static final int MSG_HANDLE_THROW_RESULT = 100;
    private static final int MSG_PLAY_THROW_SOUND = 101;

    private static class ThrowResult {

        private final ThrowPhotoManager.ResultCode result;
        private final ThrowPhoto throwPhoto;

        public ThrowResult(ThrowPhotoManager.ResultCode result, ThrowPhoto throwPhoto) {
            this.result = result;
            this.throwPhoto = throwPhoto;
        }
    }

    private ThrowPhotoManager mThrowPhotoManager;
    private AccountManager mAccountManager;
    private ThrowStrengthCollector mStrengthCollector;
    private ActionBar mActionBar;
    private ImageView mThrowingImage;
    private View mSelectView;
    private View mPromptView;
    private SimpleProgressDialog mProgressDialog;

    private String[] mSelectedPhotoPaths;
    private boolean mIsPhotoTemp;
    private File[] mCompressedPhotoFiles;
    private double mThrowDistance;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_THROW_RESULT:
                    onPhotoThrowResult((ThrowResult) msg.obj);
                    break;
                case MSG_PLAY_THROW_SOUND:
                    handlePlayThrowSound();
                    break;
                default:
                    break;
            }
        }
    };

    private final ThrowStrengthCollector.Listener mStrengthCollectorListener
            = new ThrowStrengthCollector.Listener() {
        @Override
        public void onCollectDone(float strength, double azimuth) {
            onStrengthCollectDone(strength, azimuth);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        mThrowPhotoManager = ThrowPhotoManager.getInstance(activity);
        mAccountManager = AccountManager.getInstance(activity);
        mStrengthCollector = new ThrowStrengthCollector(activity,
                mStrengthCollectorListener);
        mSelectedPhotoPaths = null;
        mIsPhotoTemp = false;

        setHasOptionsMenu(true);
        mActionBar = activity.getActionBar();
        mActionBar.hide();
        setInitTitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_throw_photo, container, false);
        TextView title= (TextView) view.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.throw_photo));
        mSelectView = view.findViewById(R.id.select_photo);
        mPromptView = view.findViewById(R.id.throw_prompt);
        mThrowingImage = (ImageView) view.findViewById(R.id.throw_animation);
        mSelectView.setOnClickListener(this);
        stopThrowAnimation();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStrengthCollector.stop();
        mStrengthCollector.destroy();
        deleteTempPhotoFiles();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SELECT_PHOTO:
                onSelectPhotoResult(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pick_photo_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = true;

        switch (item.getItemId()) {
            case R.id.menu_pick_photo:
                UILauncher.launchFragmentInNewActivity(getActivity(),
                        NearByThrowPhotosFragment.class, null);
                break;
            default:
                handled = false;
                break;
        }

        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_photo:
                selectPhoto();
                break;
            default:
                break;
        }
    }

    private void selectPhoto() {
        UILauncher.launchMultiSelectImageUI(this, REQUEST_SELECT_PHOTO,
                getString(R.string.select_one_photo),
                getString(R.string.start_throw_photo),
                1);
        mSelectedPhotoPaths = null;
        mIsPhotoTemp = false;
    }

    private void onSelectPhotoResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            mSelectedPhotoPaths = data.getStringArrayExtra("file_path");
            mIsPhotoTemp = data.getBooleanExtra("isTemp", false);
            startCollectStrength();
            startThrowAnimation();
            mSelectView.setVisibility(View.GONE);
            mPromptView.setVisibility(View.VISIBLE);

            if (mActionBar != null) {
                mActionBar.setTitle(R.string.start_throw_photo);
            }
        }
    }

    private void startCollectStrength() {
        mStrengthCollector.start();
    }

    private void onStrengthCollectDone(float strength, double azimuth) {
        L.v(TAG, "onStrengthCollectDone(),strength=%1$f, azimuth=%2$f", strength, azimuth);
        stopThrowAnimation();
        mThrowDistance = calcThrowDistance(strength);
        startThrowPhoto(calcThrowPosition(mThrowDistance, azimuth));
        playThrowSound();
    }

    private double calcThrowDistance(float strength) {
        return MAX_THROW_DISTANCE * strength;
    }

    private LocationInfo calcThrowPosition(double distance, double azimuth) {
        LocationInfo myLocation = mAccountManager.getLocation();
        LocationInfo location = LocUtil.calcLocation(myLocation, distance, azimuth);

        L.v(TAG, "calcThrowPosition(), distance=%1$f, new distance=%2$f", distance,
                location.getDistance(myLocation));

        return location;
    }

    private void startThrowAnimation() {
        if (mThrowingImage != null) {
            Drawable drawable = mThrowingImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.start();
            }
        }
    }

    private void stopThrowAnimation() {
        if (mThrowingImage != null) {
            Drawable drawable = mThrowingImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.stop();
                animationDrawable.selectDrawable(0);
            }
        }
    }

    private void startThrowPhoto(LocationInfo location) {
        showProgressDialog();

        File[] photos = new File[mSelectedPhotoPaths.length];

        for (int i = 0; i < mSelectedPhotoPaths.length; ++i) {
            photos[i] = new File(mSelectedPhotoPaths[i]);
        }

        PhotoCompressor.compress(photos, new CompressListener(location));
    }

    private void onPhotoCompressDone(File[] compressedPhotoFiles, LocationInfo location) {
        try {
            mCompressedPhotoFiles = compressedPhotoFiles;
            mThrowPhotoManager.throwPhoto(compressedPhotoFiles,
                    location, new ThrowPhotoObserver());
        } catch (FileNotFoundException e) {
            L.w(TAG, e);
            dismissProgressDialog();
            Toast.makeText(getActivity(), R.string.throw_photo_failed,
                    Toast.LENGTH_SHORT).show();
            deleteTempPhotoFiles();
        }
    }

    private void deleteTempPhotoFiles() {
        if (mIsPhotoTemp) {
            if (mSelectedPhotoPaths != null) {
                for (String path : mSelectedPhotoPaths) {
                    if (!new File(path).delete()) {
                        L.v(TAG, "onPhotoThrowResult(), delete file %1$s failed.", path);
                    }
                }
                mSelectedPhotoPaths = null;
            }
        }

        if (mCompressedPhotoFiles != null) {
            for (File file : mCompressedPhotoFiles) {
                if (!file.delete()) {
                    L.v(TAG, "onPhotoThrowResult(), delete file %1$s failed.", file.getPath());
                }
            }
            mCompressedPhotoFiles = null;
        }
    }

    private void onPhotoThrowResult(ThrowResult throwResult) {
        deleteTempPhotoFiles();
        dismissProgressDialog();

        final Activity activity = getActivity();

        switch (throwResult.result) {
            case SUCCESS:
                Toast.makeText(activity, getThrowAwayString(),
                        Toast.LENGTH_LONG).show();
                showThrowPhotoMap(throwResult.throwPhoto);
                break;
            default:
                Toast.makeText(activity, R.string.throw_photo_failed,
                        Toast.LENGTH_SHORT).show();
                break;
        }

        startNewThrow();
    }

    private String getThrowAwayString() {
        final long distance = (long) (mThrowDistance + .5D);
        return getString((distance < 1000) ? R.string.throw_photo_away
                : R.string.throw_photo_far_away, distance);
    }

    private void showThrowPhotoMap(ThrowPhoto throwPhoto) {
        if (throwPhoto != null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MyThrowPhotoDetailFragment.EXTRA_MY_THROW_PHOTO,
                    throwPhoto);
            UILauncher.launchFragmentInNewActivity(getActivity(),
                    MyThrowPhotoDetailFragment.class, arguments);
        }
    }

    private void startNewThrow() {
        setInitTitle();
        mSelectView.setVisibility(View.VISIBLE);
        mPromptView.setVisibility(View.GONE);
    }

    private void setInitTitle() {
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.throw_photo);
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

    private void playThrowSound() {
        mHandler.removeMessages(MSG_PLAY_THROW_SOUND);
        mHandler.sendEmptyMessageDelayed(MSG_PLAY_THROW_SOUND, PLAY_THROW_SOUND_DELAY);
    }

    private void handlePlayThrowSound() {
        MediaPlayer player = MediaPlayer.create(getActivity(), R.raw.throw_photo_sound);
        player.start();
    }

    private class CompressListener implements PhotoCompressor.OnCompressListener {

        private final LocationInfo location;

        public CompressListener(LocationInfo location) {
            this.location = location;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onFinish(File[] compressedPhotoFiles) {
            onPhotoCompressDone(compressedPhotoFiles, location);
        }
    }

    private class ThrowPhotoObserver implements ThrowPhotoManager.ThrowObserver {

        @Override
        public void onThrowResult(ThrowPhotoManager.ResultCode result, ThrowPhoto throwPhoto) {
            Message msg = mHandler.obtainMessage(MSG_HANDLE_THROW_RESULT,
                    new ThrowResult(result, throwPhoto));
            mHandler.sendMessage(msg);
        }
    }
}
