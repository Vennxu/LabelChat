package com.ekuater.labelchat.ui.fragment.friends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.ConfirmDialogFragment;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.ClearEditText;

/**
 * @author LinYong
 */
public class AddFriendMainFragment extends Fragment implements View.OnClickListener {

    private static final int MSG_HANDLE_EXACT_SEARCH_RESULT = 101;

    private int mMobileLength;
    private int mLabelCodeMinLength;
    private ContactsManager mContactsManager;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_EXACT_SEARCH_RESULT:
                    handleExactSearchResult(msg.arg1, (Stranger) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    public AddFriendMainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActivity().getActionBar();
        final Resources res = getResources();
        if (actionBar != null) {
            actionBar.setTitle(R.string.add_friend);
        }
        mContactsManager = ContactsManager.getInstance(getActivity());
        mMobileLength = res.getInteger(R.integer.mobile_length);
        mLabelCodeMinLength = res.getInteger(R.integer.label_code_min_length);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_add_main, container, false);
        view.findViewById(R.id.exact_search).setOnClickListener(this);
        view.findViewById(R.id.people_around).setOnClickListener(this);
        view.findViewById(R.id.today_recommended).setOnClickListener(this);
        view.findViewById(R.id.bubble_up).setOnClickListener(this);
        ClearEditText searchEdit = (ClearEditText) view.findViewById(R.id.filter_edit);
        searchEdit.setFunctionListener(new ClearEditText.FunctionListener() {
            @Override
            public void onFunctionButtonClick(CharSequence sequence) {
                onExactSearch(sequence.toString());
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exact_search:
                showExactSearchConfirm();
                break;
            case R.id.people_around:
                showPeopleAroundUI();
                break;
            case R.id.today_recommended:
                showTodayRecommendedConfirm();
                break;
            case R.id.bubble_up:
                showBubbleUpConfirm();
                break;
            default:
                break;
        }
    }

    private void onExactSearch(String searchWord) {
        mContactsManager.exactSearchUser(searchWord,
                new ContactsManager.UserQueryObserver() {
                    @Override
                    public void onQueryResult(int result, Stranger user) {
                        Message message = mHandler.obtainMessage(MSG_HANDLE_EXACT_SEARCH_RESULT,
                                result, 0, user);
                        mHandler.sendMessage(message);
                    }
                });
    }

    private void handleExactSearchResult(int result, Stranger user) {
        final Activity activity = getActivity();

        switch (result) {
            case ContactsManager.QUERY_RESULT_SUCCESS:
                if (user != null) {
                    UILauncher.launchStrangerDetailUI(activity, user);
                } else {
                    ShowToast.makeText(activity, R.drawable.emoji_cry, activity.
                            getResources().getString(R.string.search_no_match_user)).show();

                }
                break;
            case ContactsManager.QUERY_RESULT_ILLEGAL_ARGUMENTS:
                ShowToast.makeText(activity, R.drawable.emoji_sad, activity.
                        getResources().getString(R.string.input_mobile_or_label_code_prompt,
                        mMobileLength, mLabelCodeMinLength)).show();
                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_cry, activity.
                        getResources().getString(R.string.search_no_match_user)).show();
                break;
        }
    }

    private void showExactSearchConfirm() {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(true,
                getString(R.string.exact_search), null,
                getString(R.string.exact_search_prompt_message),
                null);
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ConfirmDialogFragment.AbsConfirmListener() {
            @Override
            public void onConfirm() {
                if (mContactsManager.isInGuestMode()) {
                    UILauncher.launchLoginPromptUI(getFragmentManager());
                } else {
                    UILauncher.launchExactSearchFriendUI(getActivity());
                }
            }
        };
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig,
                confirmListener);
        fragment.show(getFragmentManager(), null);
    }

    private void showPeopleAroundUI() {
        if (mContactsManager.isInGuestMode()) {
            UILauncher.launchLoginPromptUI(getFragmentManager());
        } else {
            UILauncher.launchPeopleAroundUI(getActivity());
        }
    }

    private void showTodayRecommendedConfirm() {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(true,
                getString(R.string.today_recommended), null,
                getString(R.string.today_recommended_prompt_message),
                getString(R.string.today_recommended_prompt_sub_message));
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ConfirmDialogFragment.AbsConfirmListener() {
            @Override
            public void onConfirm() {
                if (mContactsManager.isInGuestMode()) {
                    UILauncher.launchLoginPromptUI(getFragmentManager());
                } else {
                    mContactsManager.requestTodayRecommended(null);
                }
            }
        };
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        fragment.show(getFragmentManager(), null);
    }

    private void showBubbleUpConfirm() {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(true,
                getString(R.string.bubble_up), null,
                getString(R.string.bubble_up_prompt_message),
                getString(R.string.bubble_up_prompt_sub_message));
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ConfirmDialogFragment.AbsConfirmListener() {
            @Override
            public void onConfirm() {
                if (canBubbleUpNow()) {
                    mContactsManager.requestBubbling(null);
                } else {
                    showBubbleUpFillInfoConfirm();
                }
            }
        };
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        fragment.show(getFragmentManager(), null);
    }

    private boolean canBubbleUpNow() {
        final SettingHelper helper = SettingHelper.getInstance(getActivity());
        return !TextUtils.isEmpty(helper.getAccountProvince())
                && !TextUtils.isEmpty(helper.getAccountCity())
                && !TextUtils.isEmpty(helper.getAccountSchool())
                && (helper.getAccountAge() >= 0)
                && (helper.getAccountConstellation() >= 0);
    }

    private void showBubbleUpFillInfoConfirm() {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(
                getString(R.string.bubble_up_fill_information_prompt_message),
                null);
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ConfirmDialogFragment.AbsConfirmListener() {
            @Override
            public void onConfirm() {
                if (mContactsManager.isInGuestMode()) {
                    UILauncher.launchLoginPromptUI(getFragmentManager());
                } else {
                    // Launch personal information settings UI.
                    UILauncher.launchUserInfoSettingUI(getActivity());
                }
            }
        };
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig, confirmListener);
        fragment.show(getFragmentManager(), null);
    }
}
