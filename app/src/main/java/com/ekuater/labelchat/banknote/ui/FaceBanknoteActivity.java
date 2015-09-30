package com.ekuater.labelchat.banknote.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.banknote.BanknoteComposer;
import com.ekuater.labelchat.banknote.BanknoteParser;
import com.ekuater.labelchat.banknote.ComposeError;
import com.ekuater.labelchat.banknote.ComposeResult;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.delegate.PostStoryListener;
import com.ekuater.labelchat.delegate.QueryResult;
import com.ekuater.labelchat.ui.ContentSharer;
import com.ekuater.labelchat.ui.ShareContent;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.ui.fragment.SimpleProgressHelper;
import com.ekuater.labelchat.ui.util.ShowToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Leo on 2015/5/11.
 *
 * @author LinYong
 */
public class FaceBanknoteActivity extends BackIconActivity implements View.OnClickListener,
        Handler.Callback {

    private static final int REQUEST_PICK_PHOTO = 10;

    private static final int MSG_ON_PUBLISH_STORY = 100;
    private static final int MSG_UPDATE_BANKNOTE_IMAGE = 101;

    private File mBanknoteFile;
    private Bitmap mBanknoteBitmap;
    private Handler mHandler;
    private ContentSharer mContentSharer;
    private ComposeTask mComposeTask;
    private Uri mCroppedImageUri;

    private SimpleProgressHelper mProgressHelper;
    private ImageView mBanknoteImageView;
    private View mPickPanel;
    private View mSendPanel;
    private View mBanknotePanel;
    private TextView mPublishButton;
    private EditText mContentEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_banknote);

        setHasContentSharer();
        mHandler = new Handler(this);
        mProgressHelper = new SimpleProgressHelper(this);
        mContentSharer = getContentSharer();
        mComposeTask = null;

        ImageView icon = (ImageView) findViewById(R.id.icon);
        TextView title = (TextView) findViewById(R.id.title);
        TextView rightTitle = (TextView) findViewById(R.id.right_title);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title.setText(R.string.face_banknote);
        rightTitle.setText(R.string.publish_to_story);
        rightTitle.setTextColor(getResources().getColorStateList(
                R.color.face_banknote_publish_text_color));

        mBanknoteImageView = (ImageView) findViewById(R.id.banknote);
        mBanknoteImageView.setOnClickListener(this);
        mPickPanel = findViewById(R.id.pick_panel);
        mSendPanel = findViewById(R.id.send_panel);
        findViewById(R.id.pick_photo).setOnClickListener(this);
        mPublishButton = rightTitle;
        mPublishButton.setOnClickListener(this);
        findViewById(R.id.weixin_friend).setOnClickListener(this);
        findViewById(R.id.weixin_circle).setOnClickListener(this);
        findViewById(R.id.try_again).setOnClickListener(this);
        mPickPanel.setVisibility(View.VISIBLE);
        mSendPanel.setVisibility(View.GONE);
        mBanknotePanel = findViewById(R.id.banknote_panel);
        mContentEdit = (EditText) findViewById(R.id.story_content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopComposeTask();
        deleteCroppedImage();
        recycleBanknote();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_PHOTO:
                onPhotoPick(resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pick_photo:
                pickPhoto();
                break;
            case R.id.right_title:
                onPublishStory();
                break;
            case R.id.weixin_friend:
                shareFaceBanknote(ShareContent.Platform.WEIXIN);
                break;
            case R.id.weixin_circle:
                shareFaceBanknote(ShareContent.Platform.WEIXIN_CIRCLE);
                break;
            case R.id.try_again:
                pickPhoto();
                break;
            case R.id.banknote:
                if (mBanknoteBitmap != null) {
                    UILauncher.launchStoryShowPhotoUI(this, mBanknoteBitmap);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_ON_PUBLISH_STORY:
                onPublishStoryResult(msg.arg1);
                break;
            case MSG_UPDATE_BANKNOTE_IMAGE:
                updateBanknoteImage();
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private String getStoryContent() {
        return mContentEdit.getText().toString();
    }

    private void pickPhoto() {
        startActivityForResult(new Intent(this, CropPhotoAvatarActivity.class),
                REQUEST_PICK_PHOTO);
    }

    private void onPhotoPick(int resultCode, Intent data) {
        if (RESULT_OK == resultCode && data != null) {
            deleteCroppedImage();
            mCroppedImageUri = data.getData();
            stopComposeTask();
            mComposeTask = new ComposeTask();
            mComposeTask.start(mCroppedImageUri.getPath());
        }
    }

    private void onPublishStory() {
        try {
            LabelStoryManager storyManager = LabelStoryManager.getInstance(this);
            PostStoryListener listener = new PostStoryListener() {
                @Override
                public void onPostResult(int result, int errorCode, String errorDesc,
                                         LabelStory[] labelStories) {
                    mHandler.obtainMessage(MSG_ON_PUBLISH_STORY, result, errorCode,
                            labelStories).sendToTarget();
                }
            };
            storyManager.sendBanknoteStory(mBanknoteFile, getStoryContent(), listener);
            mProgressHelper.show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ShowToast.makeText(this, R.drawable.emoji_sad,
                    getString(R.string.publish_failed)).show();
        }
    }

    private void onPublishStoryResult(int result) {
        mProgressHelper.dismiss();
        if (result == QueryResult.RESULT_SUCCESS) {
            ShowToast.makeText(this, R.drawable.emoji_smile,
                    getString(R.string.publish_success)).show();
            mPublishButton.setEnabled(false);
            mPublishButton.setText(R.string.published);
            mContentEdit.setVisibility(View.INVISIBLE);
            mBanknotePanel.setBackgroundResource(R.color.transparent);
        } else {
            ShowToast.makeText(this, R.drawable.emoji_sad,
                    getString(R.string.publish_failed)).show();
        }
    }

    private void deleteCroppedImage() {
        if (mCroppedImageUri != null && mCroppedImageUri.getScheme().equals("file")) {
            //noinspection ResultOfMethodCallIgnored
            new File(mCroppedImageUri.getPath()).delete();
            mCroppedImageUri = null;
        }
    }

    private void recycleBanknote() {
        if (mBanknoteFile != null) {
            //noinspection ResultOfMethodCallIgnored
            mBanknoteFile.delete();
            mBanknoteFile = null;
        }
        if (mBanknoteBitmap != null) {
            mBanknoteBitmap.recycle();
            mBanknoteBitmap = null;
        }
    }

    private void shareFaceBanknote(ShareContent.Platform platform) {
        ShareContent shareContent = new ShareContent();
        shareContent.setSharePlatform(platform);
        shareContent.setShareMedia(mBanknoteFile.getAbsolutePath(),
                ShareContent.MediaType.IMAGE);
        mContentSharer.directShareContent(shareContent);
    }

    private void onComposeResult(TaskResult result) {
        deleteCroppedImage();

        switch (result.composeResult.error) {
            case SUCCESS:
                if (result.banknoteFile != null) {
                    recycleBanknote();
                    mBanknoteFile = result.banknoteFile;
                    mBanknoteBitmap = result.composeResult.faceBanknote;
                    mPickPanel.setVisibility(View.GONE);
                    mSendPanel.setVisibility(View.VISIBLE);
                    mPublishButton.setVisibility(View.VISIBLE);
                    mPublishButton.setEnabled(true);
                    mPublishButton.setText(R.string.publish_to_story);
                    mContentEdit.setVisibility(View.VISIBLE);
                    mContentEdit.setText("");
                    mBanknotePanel.setBackgroundResource(R.color.white);
                    mHandler.obtainMessage(MSG_UPDATE_BANKNOTE_IMAGE).sendToTarget();
                } else {
                    if (result.composeResult.faceBanknote != null) {
                        result.composeResult.faceBanknote.recycle();
                    }
                    ShowToast.makeText(this, R.drawable.emoji_sad,
                            getString(R.string.banknote_failed)).show();
                }
                break;
            case NO_FACE:
                ShowToast.makeText(this, R.drawable.emoji_sad,
                        getString(R.string.banknote_detect_no_face))
                        .show();
                break;
            case BANKNOTE_DECODE_ERROR:
            default:
                ShowToast.makeText(this, R.drawable.emoji_sad,
                        getString(R.string.banknote_failed)).show();
                break;
        }
    }

    private void updateBanknoteImage() {
        if (mBanknoteImageView.getMeasuredWidth() <= 0) {
            mBanknoteImageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    mBanknoteImageView.removeOnLayoutChangeListener(this);
                    updateBanknoteImageInternal();
                }
            });
            mBanknoteImageView.requestLayout();
        } else {
            updateBanknoteImageInternal();
        }
    }

    private void updateBanknoteImageInternal() {
        ViewGroup.LayoutParams lp = mBanknoteImageView.getLayoutParams();
        int height = (int) (mBanknoteImageView.getMeasuredWidth()
                / (float) mBanknoteBitmap.getWidth()
                * mBanknoteBitmap.getHeight());

        mBanknoteImageView.setImageBitmap(mBanknoteBitmap);
        if (lp.height != height) {
            lp.height = height;
            mBanknoteImageView.setLayoutParams(lp);
        }
    }

    private void stopComposeTask() {
        if (mComposeTask != null && mComposeTask.getStatus()
                != ComposeTask.Status.FINISHED) {
            mComposeTask.cancel(true);
        }
        mComposeTask = null;
    }

    private static class TaskResult {

        public ComposeResult composeResult;
        public File banknoteFile;

        public TaskResult(ComposeResult composeResult, File banknoteFile) {
            this.composeResult = composeResult;
            this.banknoteFile = banknoteFile;
        }
    }

    private class ComposeTask extends AsyncTask<String, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(String... params) {
            String photoPath = params[0];
            BanknoteComposer composer = new BanknoteComposer(FaceBanknoteActivity.this);
            ComposeResult result = composer.compose(photoPath,
                    BanknoteParser.randomBanknote(getResources()));
            File banknoteFile = null;

            if (result.error == ComposeError.SUCCESS) {
                banknoteFile = saveToFile(result.faceBanknote);
            }
            return new TaskResult(result, banknoteFile);
        }

        @Override
        protected void onPreExecute() {
            mProgressHelper.show();
        }

        @Override
        protected void onPostExecute(TaskResult result) {
            mProgressHelper.dismiss();
            onComposeResult(result);
        }

        private File saveToFile(Bitmap bitmap) {
            File tempFile = EnvConfig.genTempFile(".jpg");

            if (tempFile != null) {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(tempFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    tempFile = null;
                } finally {
                    if (out != null) {
                        try {
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            tempFile = null;
                        }
                    }
                }
            }
            return tempFile;
        }

        public void start(String photoPath) {
            execute(photoPath);
        }
    }
}
