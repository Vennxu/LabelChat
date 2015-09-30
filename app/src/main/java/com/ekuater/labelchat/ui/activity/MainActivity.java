package com.ekuater.labelchat.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Dynamic.DynamicPublicEvent;
import com.ekuater.labelchat.datastruct.SystemPush;
import com.ekuater.labelchat.delegate.PushMessageManager;
import com.ekuater.labelchat.notificationcenter.NotificationCenter;
import com.ekuater.labelchat.ui.UIEventBusHub;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.base.TitleIconActivity;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.fragment.main.LabelStoryPageFragment;
import com.ekuater.labelchat.ui.fragment.main.MessageFragment;
import com.ekuater.labelchat.ui.fragment.main.PersonalInfoFragment;
import com.ekuater.labelchat.ui.fragment.main.RelationshipPageFragment;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;

import de.greenrobot.event.EventBus;

public class MainActivity extends TitleIconActivity {

    private static final String ACTION_TAG_STORY = "action_tag_story";
    private static final String ACTION_TAG_CONFIDE = "action_tag_confide";
    private static final String ACTION_TAG_RECORD = "action_tag_record";
    private static final String ACTION_TAG_FACE = "action_tag_face_banknote";

    private static final float ROTATE_DEGREE = 135;

    private static boolean sJustLogin = false;

    public static synchronized void setJustLogin() {
        sJustLogin = true;
    }

    private static synchronized boolean getJustLogin() {
        boolean tmp = sJustLogin;
        sJustLogin = false;
        return tmp;
    }

    private NotificationCenter mNotificationCenter;
    private SparseArrayCompat<Fragment> mPageArray = new SparseArrayCompat<>();
    private Fragment mCurrentPage;
    private FragmentManager mFragmentManager;
    private RadioGroup mTabBar;
    private EventBus mUIEventBus;

    private View mActionCover;
    private ImageView mActionButton;
    private FloatingActionMenu mActionMenu;

    private PushMessageManager mPushMessageManager;
    private TextView mStateView;

    private final View.OnClickListener mActionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = MainActivity.this;

            switch ((String) v.getTag()) {
                case ACTION_TAG_STORY:
                    UILauncher.launchFragmentSendLabelStoryUI(activity,
                            LabelStoryUtils.RESULT_LABEL_STORY_CODE);
                    break;
                case ACTION_TAG_CONFIDE:
                    UILauncher.launchEditConfideUI(activity);
                    break;
                case ACTION_TAG_RECORD:
                    UILauncher.launchVoiceUI(activity);
                    break;
                case ACTION_TAG_FACE:
                    UILauncher.launchFaceBanknoteUI(activity);
                    break;
                default:
                    break;
            }

            if (mActionMenu.isOpen()) {
                mActionMenu.close(true);
            }
        }
    };

    private PushMessageManager.AbsListener mPushMessageManagerListener
            = new PushMessageManager.AbsListener() {
        @Override
        public void onNewSystemPushReceived(SystemPush systemPush) {
            updateState();
        }

        @Override
        public void onPushMessageDataChanged() {
            updateState();
        }

        private void updateState() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    load();
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionMenu(this, getWindow().getDecorView());
        mNotificationCenter = NotificationCenter.getInstance(this);
        mPushMessageManager = PushMessageManager.getInstance(this);
        mPushMessageManager.registerListener(mPushMessageManagerListener);
        mUIEventBus = UIEventBusHub.getDefaultEventBus();
        mUIEventBus.register(this);
        initChildPages();
        mTabBar = (RadioGroup) findViewById(R.id.main_tab);
        mStateView = (TextView) findViewById(R.id.new_hint_msg);
        if (getJustLogin()) {
            mTabBar.check(R.id.tab_info);
        }
        mTabBar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchChildPage(checkedId);
            }
        });
        switchChildPage(mTabBar.getCheckedRadioButtonId());
        load();
    }


    /**
     * for EventBus Event
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(DynamicPublicEvent event) {
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getJustLogin()) {
            mTabBar.check(R.id.tab_info);
        }
        updateUIScenario();
    }

    @Override
    protected void onPause() {
        super.onPause();
        exitUIScenario();
    }

    @Override
    public void onBackPressed() {
        if (mActionMenu.isOpen()) {
            mActionMenu.close(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPushMessageManager.unregisterListener(mPushMessageManagerListener);
        mUIEventBus.unregister(this);
    }

    private void initChildPages() {
        mFragmentManager = getSupportFragmentManager();
        mPageArray.put(R.id.tab_message, new MessageFragment());
        mPageArray.put(R.id.tab_relationship, new RelationshipPageFragment());
        mPageArray.put(R.id.tab_trends, new LabelStoryPageFragment());
        mPageArray.put(R.id.tab_info, new PersonalInfoFragment());
        mCurrentPage = null;
    }

    private void switchChildPage(int radioId) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        if (mCurrentPage != null) {
            transaction.detach(mCurrentPage);
            mCurrentPage = null;
        }

        String name = makeChildPageName(radioId);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            transaction.attach(fragment);
        } else {
            fragment = mPageArray.get(radioId);

            if (fragment != null) {
                transaction.add(R.id.page_container, fragment, name);
            }
        }

        transaction.commitAllowingStateLoss();
        updateUIScenario();
        mCurrentPage = fragment;
        load();
    }

    private static String makeChildPageName(long radioId) {
        return "main_page:switcher:" + radioId;
    }

    private void updateUIScenario() {
        switch (mTabBar.getCheckedRadioButtonId()) {
            case R.id.tab_message:
                mNotificationCenter.enterMainUIScenario();
                break;
            default:
                mNotificationCenter.exitMainUIScenario();
                break;
        }
    }

    private void exitUIScenario() {
        mNotificationCenter.exitMainUIScenario();
    }

    private void setupActionMenu(Context context, View rootView) {
        View actionAttachView = rootView.findViewById(R.id.action_attach_view);
        mActionCover = rootView.findViewById(R.id.action_cover);
        mActionButton = (ImageView) rootView.findViewById(R.id.action_button);

        mActionCover.setVisibility(View.GONE);
        mActionCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionMenu.close(true);
            }
        });
        FloatingActionMenu.Builder builder = new FloatingActionMenu.Builder(context);

        // sub action menu
        addSubActionMenu(context, builder, R.drawable.ic_action_confide,
                R.string.confide, ACTION_TAG_CONFIDE);
        addSubActionMenu(context, builder, R.drawable.ic_action_story,
                R.string.picture, ACTION_TAG_STORY);
        addSubActionMenu(context, builder, R.drawable.ic_action_record,
                R.string.voice, ACTION_TAG_RECORD);
        addSubActionMenu(context, builder, R.drawable.ic_action_face,
                R.string.face_banknote, ACTION_TAG_FACE);
        // sub action menu end

        builder.attachTo(actionAttachView);
        builder.setStartAngle(210);
        builder.setEndAngle(330);
        builder.setRadius(MiscUtils.dp2px(context, 110));
        mActionMenu = builder.build();
        mActionMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
                onActionMenuOpened();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
                onActionMenuClosed();
            }
        });
    }

    private void addSubActionMenu(Context context, FloatingActionMenu.Builder builder,
                                  int iconId, int stringId, String actionTag) {
        @SuppressLint("InflateParams")
        TextView menuView = (TextView) LayoutInflater.from(context).inflate(
                R.layout.action_menu_layout, null);
        menuView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        menuView.setCompoundDrawablesWithIntrinsicBounds(0, iconId, 0, 0);
        menuView.setText(stringId);
        menuView.setTag(actionTag);
        menuView.setOnClickListener(mActionClickListener);
        builder.addSubActionView(menuView);
    }

    private void onActionMenuOpened() {
        rotateActionButton(0, ROTATE_DEGREE);
        mActionCover.setVisibility(View.VISIBLE);
    }

    private void onActionMenuClosed() {
        rotateActionButton(ROTATE_DEGREE, 0);
        mActionCover.setVisibility(View.GONE);
    }

    private void rotateActionButton(float from, float to) {
        mActionButton.setRotation(from);
        ObjectAnimator.ofPropertyValuesHolder(mActionButton,
                PropertyValuesHolder.ofFloat(View.ROTATION, to)).start();
    }

    private void load() {
        new LoadTask().executeOnExecutor(LoadTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    private class LoadTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return mPushMessageManager.getUnprocessedHintMessage().size();
        }

        @Override
        protected void onPostExecute(Integer count) {
            super.onPostExecute(count);
            if (count > 0) {
                mStateView.setText(String.valueOf(count));
                if (mCurrentPage instanceof LabelStoryPageFragment) {
                    mStateView.setVisibility(View.GONE);
                } else {
                    mStateView.setVisibility(View.VISIBLE);
                }
            } else {
                mStateView.setVisibility(View.GONE);
            }
        }
    }
}
