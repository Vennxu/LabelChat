package com.ekuater.labelchat.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.util.L;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.BaseShareContent;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.media.UMusic;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

/**
 * Created by Leo on 2015/1/24.
 *
 * @author LinYong
 */
/*package*/ class ContentShareBoard {

    private static final String TAG = ContentShareBoard.class.getSimpleName();

    private Activity mActivity;
    private UMSocialService mController;
    private WindowManager mWM;
    private WindowManager.LayoutParams mWmLp;
    private View mWindowView;
    private boolean mIsShowing;
    private LabelStoryManager mLabelStoryManager;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.weixin_share:
                    startShare(ShareContent.Platform.WEIXIN);
                    break;
                case R.id.circle_share:
                    startShare(ShareContent.Platform.WEIXIN_CIRCLE);
                    break;
                case R.id.qq_share:
                    startShare(ShareContent.Platform.QQ);
                    break;
                case R.id.qzone_share:
                    startShare(ShareContent.Platform.QZONE);
                    break;
                case R.id.sina_share:
                    startShare(ShareContent.Platform.SINA_WEIBO);
                    break;
                case R.id.cancel:
                    break;
                default:
                    break;
            }
            dismissWindow();
        }
    };

    private final SocializeListeners.SnsPostListener mSnsPostListener =
            new SocializeListeners.SnsPostListener() {
                @Override
                public void onStart() {
                    L.v(TAG, "onStart()");
                }

                @Override
                public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
                    L.v(TAG, "onComplete(), platform=%1$s, eCode=%2$d", platform.toString(), eCode);
                    doShareStatistics();
                }
            };

    private ShareContent mShareContent;

    public ContentShareBoard(Activity activity, UMSocialService controller) {
        mActivity = activity;
        mController = controller;
        mWM = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        mLabelStoryManager = LabelStoryManager.getInstance(activity);
        initWindowView();
    }

    public void setShareContent(ShareContent shareContent) {
        if (shareContent == null) {
            throw new NullPointerException("Empty share content");
        }

        mShareContent = shareContent;
    }

    public void showBoard() {
        showWindow();
    }

    public void directShareContent(ShareContent shareContent) {
        if (shareContent == null || shareContent.getSharePlatform() == null) {
            throw new IllegalArgumentException("illegal share content");
        }

        setShareContent(shareContent);
        startShare(shareContent.getSharePlatform());
    }

    private void startShare(ShareContent.Platform sharePlatform) {
        switch (sharePlatform) {
            case WEIXIN:
                setWXShareContent();
                performShare(SHARE_MEDIA.WEIXIN);
                break;
            case WEIXIN_CIRCLE:
                setCircleContent();
                performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
                break;
            case QQ:
                setQQShareContent();
                performShare(SHARE_MEDIA.QQ);
                break;
            case QZONE:
                setQZoneShareContent();
                performShare(SHARE_MEDIA.QZONE);
                break;
            case SINA_WEIBO:
                setSinaShareContent();
                performShare(SHARE_MEDIA.SINA);
                break;
            default:
                break;
        }
    }

    private void doShareStatistics() {
        L.v(TAG, "doShareStatistics()");
        if (mShareContent != null && !TextUtils.isEmpty(mShareContent.getLabelStoryId())
                && mShareContent.getSharePlatform() != null) {
            mLabelStoryManager.doShareStatistics(mShareContent.getLabelStoryId(),
                    mShareContent.getSharePlatform().getPlatform());
        } else {
            L.v(TAG, "doShareStatistics(), illegal arguments");
        }
    }

    @SuppressLint("InflateParams")
    private void initWindowView() {
        mWmLp = new WindowManager.LayoutParams();
        mWmLp.token = mActivity.getWindow().getDecorView().getWindowToken();
        mWmLp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        mWmLp.format = PixelFormat.TRANSLUCENT;
        mWmLp.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWmLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWmLp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        mWmLp.dimAmount = 0.6F;
        mWmLp.windowAnimations = android.R.style.Animation_InputMethod;
        mWmLp.gravity = Gravity.BOTTOM;
        mWmLp.x = 0;
        mWmLp.y = 0;

        View contentView = LayoutInflater.from(mActivity).inflate(
                R.layout.content_share_board, null, false);
        contentView.findViewById(R.id.weixin_share).setOnClickListener(mOnClickListener);
        contentView.findViewById(R.id.circle_share).setOnClickListener(mOnClickListener);
        contentView.findViewById(R.id.qq_share).setOnClickListener(mOnClickListener);
        contentView.findViewById(R.id.qzone_share).setOnClickListener(mOnClickListener);
        contentView.findViewById(R.id.sina_share).setOnClickListener(mOnClickListener);
        contentView.findViewById(R.id.cancel).setOnClickListener(mOnClickListener);

        WindowViewContainer viewContainer = new WindowViewContainer(mActivity);
        WindowViewContainer.LayoutParams lp = new WindowViewContainer.LayoutParams(
                WindowViewContainer.LayoutParams.MATCH_PARENT,
                WindowViewContainer.LayoutParams.WRAP_CONTENT
        );
        viewContainer.addView(contentView, lp);
        mWindowView = viewContainer;

        mIsShowing = false;
    }

    private void showWindow() {
        if (!mIsShowing) {
            mWM.addView(mWindowView, mWmLp);
            mIsShowing = true;
        }
    }

    private void dismissWindow() {
        if (mIsShowing) {
            mWM.removeView(mWindowView);
            mIsShowing = false;
        }
    }

    private void performShare(SHARE_MEDIA platform) {
        mController.postShare(mActivity, platform, mSnsPostListener);
    }

    private void setBaseShareContent(BaseShareContent baseShareContent,
                                     ShareContent shareContent) {
        if (baseShareContent == null) {
            throw new NullPointerException("Empty BaseShareContent");
        }

        if (shareContent == null) {
            throw new NullPointerException("Empty share content");
        }

        baseShareContent.setTitle(shareContent.getTitle());
        baseShareContent.setShareContent(shareContent.getContent());
        baseShareContent.setTargetUrl(shareContent.getUrl());
        if (!shareContent.hasShareMedia()) {
            baseShareContent.setShareImage(new UMImage(mActivity, shareContent.getIcon()));
        }
        mController.setShareMedia(baseShareContent);
    }

    private void setWXShareContent() {
        setBaseShareContent(newWeiXinShareContent(), mShareContent);
        mShareContent.setSharePlatform(ShareContent.Platform.WEIXIN);
    }

    private WeiXinShareContent newWeiXinShareContent() {
        if (mShareContent.hasShareMedia()) {
            WeiXinShareContent shareContent;

            switch (mShareContent.getMediaType()) {
                case IMAGE:
                    shareContent = new WeiXinShareContent(
                            new UMImage(mActivity, mShareContent.getMediaFile()));
                    break;
                case VIDEO:
                    shareContent = new WeiXinShareContent(
                            new UMVideo(mShareContent.getMediaFile()));
                    break;
                case MUSIC:
                    shareContent = new WeiXinShareContent(
                            new UMusic(mShareContent.getMediaFile()));
                    break;
                default:
                    shareContent = new WeiXinShareContent();
                    break;
            }
            return shareContent;
        } else {
            return new WeiXinShareContent();
        }
    }

    private void setCircleContent() {
        setBaseShareContent(newCircleShareContent(), mShareContent);
        mShareContent.setSharePlatform(ShareContent.Platform.WEIXIN_CIRCLE);
    }

    private CircleShareContent newCircleShareContent() {
        if (mShareContent.hasShareMedia()) {
            CircleShareContent shareContent;

            switch (mShareContent.getMediaType()) {
                case IMAGE:
                    shareContent = new CircleShareContent(
                            new UMImage(mActivity, mShareContent.getMediaFile()));
                    break;
                case VIDEO:
                    shareContent = new CircleShareContent(
                            new UMVideo(mShareContent.getMediaFile()));
                    break;
                case MUSIC:
                    shareContent = new CircleShareContent(
                            new UMusic(mShareContent.getMediaFile()));
                    break;
                default:
                    shareContent = new CircleShareContent();
                    break;
            }
            return shareContent;
        } else {
            return new CircleShareContent();
        }
    }

    private void setQQShareContent() {
        setBaseShareContent(newQQShareContent(), mShareContent);
        mShareContent.setSharePlatform(ShareContent.Platform.QQ);
    }

    private QQShareContent newQQShareContent() {
        if (mShareContent.hasShareMedia()) {
            QQShareContent shareContent;

            switch (mShareContent.getMediaType()) {
                case IMAGE:
                    shareContent = new QQShareContent(
                            new UMImage(mActivity, mShareContent.getMediaFile()));
                    break;
                case VIDEO:
                    shareContent = new QQShareContent(
                            new UMVideo(mShareContent.getMediaFile()));
                    break;
                case MUSIC:
                    shareContent = new QQShareContent(
                            new UMusic(mShareContent.getMediaFile()));
                    break;
                default:
                    shareContent = new QQShareContent();
                    break;
            }
            return shareContent;
        } else {
            return new QQShareContent();
        }
    }

    private void setQZoneShareContent() {
        setBaseShareContent(newQZoneShareContent(), mShareContent);
        mShareContent.setSharePlatform(ShareContent.Platform.QZONE);
    }

    private QZoneShareContent newQZoneShareContent() {
        if (mShareContent.hasShareMedia()) {
            QZoneShareContent shareContent;

            switch (mShareContent.getMediaType()) {
                case IMAGE:
                    shareContent = new QZoneShareContent(
                            new UMImage(mActivity, mShareContent.getMediaFile()));
                    break;
                case VIDEO:
                    shareContent = new QZoneShareContent(
                            new UMVideo(mShareContent.getMediaFile()));
                    break;
                case MUSIC:
                    shareContent = new QZoneShareContent(
                            new UMusic(mShareContent.getMediaFile()));
                    break;
                default:
                    shareContent = new QZoneShareContent();
                    break;
            }
            return shareContent;
        } else {
            return new QZoneShareContent();
        }
    }

    private void setSinaShareContent() {
        setBaseShareContent(newSinaShareContent(), mShareContent);
        mShareContent.setSharePlatform(ShareContent.Platform.SINA_WEIBO);
    }

    private SinaShareContent newSinaShareContent() {
        if (mShareContent.hasShareMedia()) {
            SinaShareContent shareContent;

            switch (mShareContent.getMediaType()) {
                case IMAGE:
                    shareContent = new SinaShareContent(
                            new UMImage(mActivity, mShareContent.getMediaFile()));
                    break;
                case VIDEO:
                    shareContent = new SinaShareContent(
                            new UMVideo(mShareContent.getMediaFile()));
                    break;
                case MUSIC:
                    shareContent = new SinaShareContent(
                            new UMusic(mShareContent.getMediaFile()));
                    break;
                default:
                    shareContent = new SinaShareContent();
                    break;
            }
            return shareContent;
        } else {
            return new SinaShareContent();
        }
    }

    private class WindowViewContainer extends FrameLayout {

        public WindowViewContainer(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (getKeyDispatcherState() == null) {
                    return super.dispatchKeyEvent(event);
                }

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getRepeatCount() == 0) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    state.startTracking(event, this);
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state.isTracking(event) && !event.isCanceled()) {
                        dismiss();
                        return true;
                    }
                }
                return super.dispatchKeyEvent(event);
            } else {
                return super.dispatchKeyEvent(event);
            }
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();

            if ((event.getAction() == MotionEvent.ACTION_DOWN)
                    && ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight()))) {
                dismiss();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                dismiss();
                return true;
            } else {
                return super.onTouchEvent(event);
            }
        }

        private void dismiss() {
            dismissWindow();
        }
    }
}
