package com.ekuater.labelchat.ui.fragment.get;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.LoginPromptDialog;
import com.ekuater.labelchat.ui.util.ShowToast;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import android.os.*;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.*;

public class GetGameFragment extends Fragment {
    /**
     * Called when the activity is first created.
     */
    // 控件定义
    private ImageView image1; // 9个窗口
    private ImageView image2;
    private ImageView image3;
    private ImageView image4;
    private ImageView image5;
    private ImageView image6;
    private ImageView image7;
    private ImageView image8;
    private ImageView image9;
    private LinearLayout mLinear, mLinearLoading;
    private ImageView mImageLoading;
    private MyHandler myHandler; // 子线程循环队列

    private static int index = 100; // 判断窗口touch事件发生时，窗口是否处在打开状态
    private static int temp = 101; // 记录哪个窗口打开，以这个参数找到该窗口及时关闭
    private static int score = 0; // 分数变量
    private Handler changeMe; // 更改UI线程，通过message与子线程通信
    private Handler changeWindow; // 更改UI线程，通过message与子线程通信
    private static boolean close = false; // 结束线程的开关，true时线程和looper结束。
    private static boolean beginning = true; // 开始开关，值为true时开始键可用，修复可以多次点击开始键的BUG
    private int basetime;
    private MyCount mCountdownTimer;
    private RectProgressView mGetScore;
    private RectProgressView mGetTime;
    ArrayList<ImageView> imageList = new ArrayList<ImageView>();
    private Bitmap mBitmap;
    private MediaPlayer mCatch;
    private MediaPlayer waitCatch;
    private MediaPlayer mEndMedia;
    private Activity mContext;
    private Stranger mStranger;
    //    private int[] mMaxTime = {5000, 6000, 7000, 8000, 9000, 10000,11000,12000,13000,14000,15000};
    private int[] mScorePic = {0, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5};
    private int mDonwTime = 0;
    private long mCountTime;
    private int mCountScore;
    private boolean isMusic;
    private AccountManager mAccountManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        mAccountManager=AccountManager.getInstance(mContext);
        puaseDate();
        mCatch = MediaPlayer.create(mContext, R.raw.ccatch);
        waitCatch = MediaPlayer.create(mContext, R.raw.waitbreak);
        if (isMusic) {
            waitCatch.start();
        }
        waitCatch.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    // @Override
                    public void onCompletion(MediaPlayer arg0) {
                        try {
                            waitCatch.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        final ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.get_game);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.music_ui_menu, menu);
        MenuItem view = menu.findItem(R.id.music);
        if (isMusic) {
            view.setIcon(getResources().getDrawable(R.drawable.music_on));
        } else {
            view.setIcon(getResources().getDrawable(R.drawable.music_off));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.music:
                if (isMusic) {
                    isMusic = false;
                    SettingHelper.getInstance(mContext).setAccountMusic(isMusic);
                    waitCatch.pause();
                    item.setIcon(getResources().getDrawable(R.drawable.music_off));
                } else {
                    isMusic = true;
//                    waitCatch = MediaPlayer.create(mContext, R.raw.waitbreak);
                    SettingHelper.getInstance(mContext).setAccountMusic(isMusic);
                    waitCatch.start();
                    item.setIcon(getResources().getDrawable(R.drawable.music_on));
                }

                break;
        }
        return super.onOptionsItemSelected(item);

    }

    public void puaseDate() {

        Bundle bundle = getArguments();
        mStranger = bundle.getParcelable(GetFragment.GET_GAME_INFO);
        isMusic = bundle.getBoolean(GetFragment.GET_GAME_MUSIC);
    }

    //    public void setProgress() {
//        mCountTime = (long) mMaxTime[new Random().nextInt( mMaxTime.length)];
//        mGetTime.setmMax((int) mCountTime/1000);
//        mGetTime.setmProgress((int) mCountTime / 1000);
//        mGetTime.setmText(getResources().getString(R.string.time_limit) + mCountTime);
//    }
    public void startAnimation() {

        mLinear.setVisibility(View.GONE);
        mLinearLoading.setVisibility(View.VISIBLE);
        Drawable drawable = mImageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    public void stopAnimation() {
        mLinear.setVisibility(View.VISIBLE);
        mLinearLoading.setVisibility(View.GONE);
        Drawable drawable = mImageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.get_game_fragment, container, false);
        mGetScore = (RectProgressView) view.findViewById(R.id.get_progress_score);
        mGetTime = (RectProgressView) view.findViewById(R.id.get_progress_time);
        mLinear = (LinearLayout) view.findViewById(R.id.linear_game_linear);
        mImageLoading = (ImageView) view.findViewById(R.id.image_game_loading);
        mLinearLoading = (LinearLayout) view.findViewById(R.id.linear_game_loading);
        startAnimation();
//        setProgress();
        image1 = (ImageView) view.findViewById(R.id.image1);
        image2 = (ImageView) view.findViewById(R.id.image2);
        image3 = (ImageView) view.findViewById(R.id.image3);
        image4 = (ImageView) view.findViewById(R.id.image4);
        image5 = (ImageView) view.findViewById(R.id.image5);
        image6 = (ImageView) view.findViewById(R.id.image6);
        image7 = (ImageView) view.findViewById(R.id.image7);
        image8 = (ImageView) view.findViewById(R.id.image8);
        image9 = (ImageView) view.findViewById(R.id.image9);
        // 将按键加入集合
        imageList.add(image1);
        imageList.add(image2);
        imageList.add(image3);
        imageList.add(image4);
        imageList.add(image5);
        imageList.add(image6);
        imageList.add(image7);
        imageList.add(image8);
        imageList.add(image9);
        mCountdownTimer = new MyCount(20000, 100);
        basetime = 2;
        // 开启消息循环队列并与myHandler绑定
        HandlerThread handlerThread = new HandlerThread("thread");
        handlerThread.start();
        myHandler = new MyHandler(handlerThread.getLooper());
        changeMe = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 != 102) {
                    imageList.get(msg.arg1).setImageBitmap(mBitmap);
                } else if (msg.arg1 == 102) {
                    VibraterUtils.Vibrate(mContext, 100);
                    int scores = mScorePic[(new Random().nextInt(mScorePic.length))];
                    imageList.get(msg.arg2).setImageDrawable(
                            getResources().getDrawable(GetScorePicUtils.getScorePic(scores)));
                    score = score + scores;
                    if (score >= 100) {
                        mGetScore.setmProgress(score);
                        mGetScore.setmText(getString(R.string.get_score) + score);
                        close = true;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        index = 100;
                        beginning = true;
                        Message changeMsg = changeWindow.obtainMessage();
                        changeMsg.arg2 = 1;
                        changeWindow.sendMessage(changeMsg);
                    } else {
                        mGetScore.setmProgress(score);
                        mGetScore.setmText(getString(R.string.get_score) + score);
                    }
                }
            }
        };
        // 更换窗口图片队列
        changeWindow = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                if (msg.arg2 == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    imageList.get(msg.arg1).setImageDrawable(
                            mContext.getResources().getDrawable(R.drawable.default_get));
                } else if (msg.arg2 == 1) {
                    for (int i = 0; i < 9; i++) {
                        imageList.get(i).setImageDrawable(
                                mContext.getResources().getDrawable(R.drawable.default_get));
                    }
                    mCountdownTimer.cancel();
                    if (isMusic) {
                        waitCatch.stop();
                    }

                    if (score >= 100) {
                        mEndMedia = MediaPlayer.create(mContext, R.raw.succesebreak);
                        showDialog(mContext, getString(R.string.get_congratulate), getString(R.string.get_success) +
                                getString(ConstantCode.getSexStringResource(mStranger.getSex())) + getString(R.string.le), getString(R.string.know) +
                                getString(ConstantCode.getSexStringResource(mStranger.getSex())), R.drawable.smile);
                    } else {
                        mEndMedia = MediaPlayer.create(mContext, R.raw.failedbreak);
                        showDialog(mContext, getString(R.string.get_sorry), getString(R.string.get_need), null, R.drawable.cry);
                    }
                    if (isMusic) {
                        mEndMedia.start();
                    }

                }
            }

        };
        image1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (index == 0) {
                    openMusic();
                    imageAnimotion(image1);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 0;
                    changeMe.sendMessage(changeMsg);
                }
            }
        });
        image2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (index == 1) {
                    openMusic();
                    imageAnimotion(image2);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 1;
                    changeMe.sendMessage(changeMsg);
                }
            }

        });
        image3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (index == 2) {
                    openMusic();
                    imageAnimotion(image3);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 2;
                    changeMe.sendMessage(changeMsg);
                }
            }

        });
        image4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (index == 3) {
                    openMusic();
                    imageAnimotion(image4);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 3;
                    changeMe.sendMessage(changeMsg);
                }
            }

        });
        image5.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (index == 4) {
                    openMusic();
                    imageAnimotion(image5);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 4;
                    changeMe.sendMessage(changeMsg);
                }
            }

        });
        image6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (index == 5) {
                    openMusic();
                    imageAnimotion(image6);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 5;
                    changeMe.sendMessage(changeMsg);
                }
            }

        });
        image7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (index == 6) {
                    openMusic();
                    imageAnimotion(image7);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 6;
                    changeMe.sendMessage(changeMsg);
                }
            }

        });
        image8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                System.out.println("press 8 ,index == " + index);
                if (index == 7) {
                    openMusic();
                    imageAnimotion(image8);
                    System.out.println("change coin");
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 7;
                    changeMe.sendMessage(changeMsg);
                }
            }

        });
        image9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (index == 8) {
                    openMusic();
                    imageAnimotion(image9);
                    Message changeMsg = changeMe.obtainMessage();
                    changeMsg.arg1 = 102;
                    changeMsg.arg2 = 8;
                    changeMe.sendMessage(changeMsg);
                }
            }
        });
        Bitmap bitmap = AvatarManager.getInstance(getActivity()).getAvatarThumbBitmap(mStranger.getAvatarThumb(),
                new ShortUrlImageLoadListener() {
                    @Override
                    public void onLoadFailed(String url, LoadFailType loadFailType) {
                        if (getActivity() != null) {
                            ShowToast.makeText(mContext, R.drawable.emoji_cry, mContext.
                                    getResources().getString(R.string.download_picture_fail)).show();
                        }
                    }

                    @Override
                    public void onLoadComplete(String url, Bitmap loadedImage) {
                        loadAvatarImage(loadedImage);
                    }
                });
        if (bitmap != null) {
            loadAvatarImage(bitmap);

        }

        return view;
    }

    public void openMusic() {
        if (isMusic) {
            mCatch.start();
        }
    }

    private void imageAnimotion(ImageView iv) {
        final TranslateAnimation anim = new TranslateAnimation(0, 0, 10, 0);
        anim.setInterpolator(new CycleInterpolator(2f));
        anim.setDuration(300);
        iv.startAnimation(anim);
    }

    private Handler bitmapHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            stopAnimation();
            onStarGet();

        }
    };

    private void loadAvatarImage(Bitmap bitmap) {
        mBitmap = bitmap;
        Message message = bitmapHandler.obtainMessage();
        bitmapHandler.sendMessage(message);
    }

    // 分线程
    Runnable playThread = new Runnable() {
        Random rand = new Random();

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (temp != 101) {
                Message changeMsg = changeWindow.obtainMessage();
                changeMsg.arg1 = temp;
                changeWindow.sendMessage(changeMsg);
                index = 100;
            }
            // 每1200ms打开一个窗口
            try {
                if (basetime == 5) {
                    Thread.sleep(450);
                } else if (basetime == 6) {
                    Thread.sleep(100);
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            temp = rand.nextInt(9);
            index = temp;
            Message msg = myHandler.obtainMessage();
            msg.arg1 = temp;
            myHandler.sendMessage(msg);
        }
    };

    class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            close = true;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            index = 100;
            beginning = true;
            Message changeMsg = changeWindow.obtainMessage();
            changeMsg.arg2 = 1;
            changeWindow.sendMessage(changeMsg);
            mGetTime.setmProgress(150000);
            mGetTime.setmText(getString(R.string.time_limit) + "0.0" + "s");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mDonwTime++;
            mGetTime.setmProgress(mDonwTime * 1000);
            int mills = (int) (millisUntilFinished / 100) % 10;
            int milsUnti = (int) (millisUntilFinished / 100) / 10;
            mGetTime.setmText(getString(R.string.time_limit) + (milsUnti + "." + mills + "s"));
        }
    }

    public void onStarGet() {
        if (beginning) {
            beginning = false;
            score = 0;
            close = false;
            myHandler.post(playThread);
            mCountdownTimer.start();
        }
    }

    class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 在handlerMessage方法中结束线程和消息队列，最好不要在run方法中结束。
            if (close) {
                myHandler.removeCallbacks(playThread);
                return;
            }
            Message changeMsg = changeMe.obtainMessage();
            changeMsg.arg1 = msg.arg1;
            changeMe.sendMessage(changeMsg);
            // 窗口打开后700ms关闭
            try {
                switch (basetime) {
                    case 1:
                        Thread.sleep(800);
                        break;
                    case 2:
                        Thread.sleep(700);
                        break;
                    case 3:
                        Thread.sleep(600);
                        break;
                    case 4:
                        Thread.sleep(490);
                        break;
                    case 5:
                        Thread.sleep(430);
                        break;
                    case 6:
                        Thread.sleep(100);
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            myHandler.post(playThread);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        waitCatch.stop();
        mCatch.stop();
        close = true;
        mCountdownTimer.cancel();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        index = 100;
        beginning = true;
    }

    public class DialogOnclickListener implements GetDialogFragment.GetOnclickListener {
        @Override
        public void onChanges() {
            if (mAccountManager.isLogin()) {
                getActivity().finish();
            } else {
                showLoginDialog();
            }

        }

        @Override
        public void onFaileds() {
            if (mAccountManager.isLogin()) {
                getActivity().sendBroadcast(new Intent(GetViewPagerActivity.CLOSE_GET_VIEWPAGER));
                getActivity().finish();
            } else {
                showLoginDialog();
            }

        }

        @Override
        public void onKnowPeople() {
            if (mAccountManager.isLogin()) {
                UILauncher.launchStrangerDetailUI(mContext, mStranger);
                getActivity().finish();
            } else {
                showLoginDialog();
            }

        }
    }

    private void showDialog(Activity activity, String title, String message, String know, int resource) {
        UILauncher.launchFragmentGetDialogUI(activity, getFragmentManager(), title, message, know, resource, new DialogOnclickListener());
    }

    private void showLoginDialog() {
        LoginPromptDialog loginPromptDialog = LoginPromptDialog.newInstance(new LoginPromptDialog.OnLoginOnclickListener() {
            @Override
            public void onLunchLogin() {
                getActivity().sendBroadcast(new Intent(GetViewPagerActivity.CLOSE_GET_VIEWPAGER));
                getActivity().finish();
            }
        });
        loginPromptDialog.show(getFragmentManager(), "loginPromDialog");
    }

}