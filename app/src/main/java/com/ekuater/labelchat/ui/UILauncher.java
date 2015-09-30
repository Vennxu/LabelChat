
package com.ekuater.labelchat.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.banknote.ui.FaceBanknoteActivity;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.datastruct.BaseLabel;
import com.ekuater.labelchat.datastruct.ChatMessage;
import com.ekuater.labelchat.datastruct.ChatRoom;
import com.ekuater.labelchat.datastruct.Confide;
import com.ekuater.labelchat.datastruct.InterestType;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.datastruct.LiteStranger;
import com.ekuater.labelchat.datastruct.MoodUser;
import com.ekuater.labelchat.datastruct.PersonalUser;
import com.ekuater.labelchat.datastruct.PickPhotoUser;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.datastruct.UserPraise;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.delegate.StrangerManager;
import com.ekuater.labelchat.notificationcenter.NotificationService;
import com.ekuater.labelchat.ui.activity.AvatarUploadActivity;
import com.ekuater.labelchat.ui.activity.FeedbackActivity;
import com.ekuater.labelchat.ui.activity.FragmentContainerActivity;
import com.ekuater.labelchat.ui.activity.GuideActivity;
import com.ekuater.labelchat.ui.activity.ImageViewActivity;
import com.ekuater.labelchat.ui.activity.ImageViewSelectActivity;
import com.ekuater.labelchat.ui.activity.InputCommentActivity;
import com.ekuater.labelchat.ui.activity.LoginActivity;
import com.ekuater.labelchat.ui.activity.LoginPromptActivity;
import com.ekuater.labelchat.ui.activity.MainActivity;
import com.ekuater.labelchat.ui.activity.MultiSelectImageActivity;
import com.ekuater.labelchat.ui.activity.OAuthBindAccountActivity;
import com.ekuater.labelchat.ui.activity.RegisterActivity;
import com.ekuater.labelchat.ui.activity.ResetPasswordActivity;
import com.ekuater.labelchat.ui.activity.SelectImageActivity;
import com.ekuater.labelchat.ui.activity.SettingMainActivity;
import com.ekuater.labelchat.ui.activity.SignInGuideActivity;
import com.ekuater.labelchat.ui.activity.WebViewActivity;
import com.ekuater.labelchat.ui.activity.chatting.ChattingUI;
import com.ekuater.labelchat.ui.activity.chatting.GroupChattingUI;
import com.ekuater.labelchat.ui.activity.confide.EditConfideActivity;
import com.ekuater.labelchat.ui.fragment.ChatRoomFragment;
import com.ekuater.labelchat.ui.fragment.GroupDismissMessageListFragment;
import com.ekuater.labelchat.ui.fragment.GroupInformationFragment;
import com.ekuater.labelchat.ui.fragment.LoginPromptDialog;
import com.ekuater.labelchat.ui.fragment.NewUserWelcomesFragment;
import com.ekuater.labelchat.ui.fragment.SettingUserInfoFragment;
import com.ekuater.labelchat.ui.fragment.ShowAvatarImageFragment;
import com.ekuater.labelchat.ui.fragment.StrangerFriendShowFragment;
import com.ekuater.labelchat.ui.fragment.UserShowFragment;
import com.ekuater.labelchat.ui.fragment.album.AlbumGalleryFragment;
import com.ekuater.labelchat.ui.fragment.album.MyAlbumFragment;
import com.ekuater.labelchat.ui.fragment.album.MyAlbumGalleryFragment;
import com.ekuater.labelchat.ui.fragment.album.PhotoNotifyFragment;
import com.ekuater.labelchat.ui.fragment.confide.ConfideDetailActivity;
import com.ekuater.labelchat.ui.fragment.confide.ConfideNotifyFragment;
import com.ekuater.labelchat.ui.fragment.confide.ConfideShowFragment;
import com.ekuater.labelchat.ui.fragment.confide.ConfideUtils;
import com.ekuater.labelchat.ui.fragment.confide.ShowConfideRoleFragment;
import com.ekuater.labelchat.ui.fragment.friends.AddFriendMainFragment;
import com.ekuater.labelchat.ui.fragment.friends.BubblingResultFragment;
import com.ekuater.labelchat.ui.fragment.friends.ExactSearchFriendFragment;
import com.ekuater.labelchat.ui.fragment.friends.NewUserFragment;
import com.ekuater.labelchat.ui.fragment.friends.NumberSearchFragment;
import com.ekuater.labelchat.ui.fragment.friends.PeopleAroundFragment;
import com.ekuater.labelchat.ui.fragment.friends.RecommendFriendMainFragment;
import com.ekuater.labelchat.ui.fragment.friends.RejectAddFriendFragment;
import com.ekuater.labelchat.ui.fragment.friends.SearchFriendByLabelFragment;
import com.ekuater.labelchat.ui.fragment.friends.TodayRecommendedFragment;
import com.ekuater.labelchat.ui.fragment.friends.UserDiscoveryFragment;
import com.ekuater.labelchat.ui.fragment.friends.ValidateAddFriendFragment;
import com.ekuater.labelchat.ui.fragment.friends.ValidateAddFriendListFragment;
import com.ekuater.labelchat.ui.fragment.friends.WeeklyStarConfirmFragment;
import com.ekuater.labelchat.ui.fragment.friends.WeeklyStarFragment;
import com.ekuater.labelchat.ui.fragment.get.GetDialogFragment;
import com.ekuater.labelchat.ui.fragment.get.GetFragment;
import com.ekuater.labelchat.ui.fragment.get.GetGameFragment;
import com.ekuater.labelchat.ui.fragment.get.GetViewPagerActivity;
import com.ekuater.labelchat.ui.fragment.image.ImageDisplayFragment;
import com.ekuater.labelchat.ui.fragment.image.ImageGalleryFragment;
import com.ekuater.labelchat.ui.fragment.image.SelectPhotoImageActivity;
import com.ekuater.labelchat.ui.fragment.labels.AddUserLabelFragment;
import com.ekuater.labelchat.ui.fragment.labels.LabelOptionDialogFragment;
import com.ekuater.labelchat.ui.fragment.labels.RecommendLabelShowFragment;
import com.ekuater.labelchat.ui.fragment.labels.RelativeLabelsFragment;
import com.ekuater.labelchat.ui.fragment.labels.SelectSystemLabelFragment;
import com.ekuater.labelchat.ui.fragment.labels.ShowLabelFragment;
import com.ekuater.labelchat.ui.fragment.labels.WeeklyHotLabelFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicArguments;
import com.ekuater.labelchat.ui.fragment.labelstory.DynamicMessageFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.NewMessageFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryDetailPhotoActivity;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryDetailViewPagerActivity;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryDetaileFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryUtils;
import com.ekuater.labelchat.ui.fragment.labelstory.LetterCompleteMsgFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.LetterMessageFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.SendLabelStoryFragment;
import com.ekuater.labelchat.ui.fragment.labelstory.ShowBigImageActivity;
import com.ekuater.labelchat.ui.fragment.labelstory.ShowPraiseCrowdFragment;
import com.ekuater.labelchat.ui.fragment.main.LabelPlayFragment;
import com.ekuater.labelchat.ui.fragment.mixdynamic.DynamicScenario;
import com.ekuater.labelchat.ui.fragment.mixdynamic.MixDynamicAllFragment;
import com.ekuater.labelchat.ui.fragment.mixdynamic.MixDynamicArgs;
import com.ekuater.labelchat.ui.fragment.mood.MoodFragment;
import com.ekuater.labelchat.ui.fragment.mood.MoodUserListFragment;
import com.ekuater.labelchat.ui.fragment.mood.MoodUtils;
import com.ekuater.labelchat.datastruct.UserGroup;
import com.ekuater.labelchat.ui.fragment.push.SystemPushFragment;
import com.ekuater.labelchat.ui.fragment.push.SystemPushUtils;
import com.ekuater.labelchat.ui.fragment.register.ModifyPasswordFragment;
import com.ekuater.labelchat.ui.fragment.settings.ChatBgSelectFragment;
import com.ekuater.labelchat.ui.fragment.settings.MessageNotifySettingFragment;
import com.ekuater.labelchat.ui.fragment.settings.SettingsFragment;
import com.ekuater.labelchat.ui.fragment.tags.SelectUserTagFragment;
import com.ekuater.labelchat.ui.fragment.tags.ShowSelectTagFragment;
import com.ekuater.labelchat.ui.fragment.throwphoto.MyThrowPhotosFragment;
import com.ekuater.labelchat.ui.fragment.throwphoto.PickPhotoUserFragment;
import com.ekuater.labelchat.ui.fragment.throwphoto.ThrowPhotoFragment;
import com.ekuater.labelchat.ui.fragment.userInfo.AddInterestFragment;
import com.ekuater.labelchat.ui.fragment.userInfo.ContactInfoDialog;
import com.ekuater.labelchat.ui.fragment.userInfo.FadingActionBarActivity;
import com.ekuater.labelchat.ui.fragment.userInfo.InterestFragment;
import com.ekuater.labelchat.ui.fragment.userInfo.InviteListFragment;
import com.ekuater.labelchat.ui.fragment.userInfo.MyInfoFragment;
import com.ekuater.labelchat.ui.fragment.userInfo.PersonalInfoFragment;
import com.ekuater.labelchat.ui.fragment.userInfo.RecentVisitorsFragment;
import com.ekuater.labelchat.ui.fragment.userInfo.StrangerInfoDialog;
import com.ekuater.labelchat.ui.fragment.usershowpage.AttentionListFragment;
import com.ekuater.labelchat.ui.fragment.voice.MusicListUI;
import com.ekuater.labelchat.ui.fragment.voice.VoicePushUI;

import java.util.ArrayList;

/**
 * Launch all activities here, please do not start activity directly.
 *
 * @author LinYong
 */
@SuppressWarnings("UnusedDeclaration")
public final class UILauncher {

    public static void launchFragmentByReplaceCurrent(Context context,
                                                      Class<? extends Fragment> fragment,
                                                      Bundle arguments) {
        if (context instanceof FragmentContainerActivity) {
            FragmentContainerActivity activity = (FragmentContainerActivity) context;
            Fragment instance = Fragment.instantiate(activity, fragment.getName(), arguments);
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, instance, fragment.getSimpleName());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        } else {
            launchFragmentInNewActivity(context, fragment, arguments);
        }
    }

    public static void launchFragmentInNewActivity(Context context,
                                                   Class<? extends Fragment> fragment,
                                                   Bundle arguments) {
        context.startActivity(getFragmentInNewActivity(context, fragment, arguments));
    }

    public static Intent getFragmentInNewActivity(Context context,
                                                  Class<? extends Fragment> fragment,
                                                  Bundle arguments) {
        Intent intent = new Intent(context, FragmentContainerActivity.class);
        intent.putExtra(FragmentContainerActivity.EXTRA_SHOW_FRAGMENT, fragment.getName());
        if (arguments != null) {
            intent.putExtra(FragmentContainerActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,
                    arguments);
        }
        return intent;
    }

    public static void launchFragmentForResult(Activity activity, int requestCode,
                                               Class<? extends Fragment> fragment,
                                               Bundle arguments) {
        activity.startActivityForResult(getFragmentInNewActivity(
                activity, fragment, arguments), requestCode);
    }

    public static void launchFragmentForResult(Fragment startFragment, int requestCode,
                                               Class<? extends Fragment> fragment,
                                               Bundle arguments) {
        Activity activity = startFragment.getActivity();
        if (activity == null) {
            return;
        }

        startFragment.startActivityForResult(getFragmentInNewActivity(
                activity, fragment, arguments), requestCode);
    }

    public static void launchGuideUI(Context context) {
        Intent intent = new Intent(context, GuideActivity.class);
        context.startActivity(intent);
    }

    public static void launchSignInGuideUI(Context context) {
        Intent intent = new Intent(context, SignInGuideActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void launchLoginUI(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public static void launchRegisterUI(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    public static void launchPrivacyUI(Context context) {
        launchWebViewUI(context, "file:///android_asset/privacy.html");
    }

    public static void launchWebViewUI(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        context.startActivity(intent);
    }

    public static void launchNewUserWelcomesUI(Context context, long time) {
        Bundle argument = new Bundle();
        argument.putLong(NewUserWelcomesFragment.NEW_USER_WELCOME, time);
        launchFragmentInNewActivity(context, NewUserWelcomesFragment.class, argument);
    }

    public static void launchResetPasswordUI(Context context) {
        Intent intent = new Intent(context, ResetPasswordActivity.class);
        context.startActivity(intent);
    }

    public static void launchModifyPasswordUI(Context context) {
        launchFragmentInNewActivity(context, ModifyPasswordFragment.class, null);
    }

    public static void launchLoginPromptUI(FragmentManager fm) {
        LoginPromptDialog fragment = LoginPromptDialog.newInstance();
        fragment.show(fm, LoginPromptDialog.class.getSimpleName());
    }

    public static void launchLoginPromptUI(Context context) {
        Intent intent = new Intent(context, LoginPromptActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void launchOAuthBindAccountUI(Context context) {
        Intent intent = new Intent(context, OAuthBindAccountActivity.class);
        context.startActivity(intent);
    }

    public static void launchMainUI(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void launchMainUIWhenJustLogin(Context context) {
        // TODO, delete it for no login view
        /*MainActivity.setJustLogin();
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);*/
    }

    public static void launchChattingUI(Context context, String friendUserId) {
        context.startActivity(getChattingUIIntent(context, friendUserId));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NotificationService.NOTIFICATION_AGREE_RESULT);
    }

    public static Intent getChattingUIIntent(Context context, String friendUserId) {
        Intent intent = new Intent(context, ChattingUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra(ChattingUI.EXTRA_TARGET_ID, friendUserId);
        return intent;
    }

    public static void launchNormalChatRoomUI(Context context, ChatRoom chatRoom) {
        Intent intent = new Intent(context, ChattingUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra(ChattingUI.EXTRA_CONVERSATION_TYPE,
                ChatMessage.CONVERSATION_NORMAL_CHAT_ROOM);
        intent.putExtra(ChattingUI.EXTRA_ATTACHMENT, chatRoom);
        intent.putExtra(ChattingUI.EXTRA_TARGET_ID, chatRoom.getChatRoomId());
        context.startActivity(intent);
    }

    public static void launchLabelOptionUI(FragmentManager fm, BaseLabel label) {
        LabelOptionDialogFragment fragment = LabelOptionDialogFragment.newInstance(label);
        fragment.show(fm, LabelOptionDialogFragment.class.getSimpleName());
    }

    public static void launchLabelOptionUI(FragmentManager fm, BaseLabel label, LabelOptionDialogFragment.LabelOptionListener labelOptionListener) {
        LabelOptionDialogFragment fragment = LabelOptionDialogFragment.newInstance(label, labelOptionListener);
        fragment.show(fm, LabelOptionDialogFragment.class.getSimpleName());
    }

    public static void launchLabelOptionUI(DialogFragment callDialog, BaseLabel label) {
        LabelOptionDialogFragment fragment = LabelOptionDialogFragment.newInstance(
                callDialog, label);
        fragment.show(LabelOptionDialogFragment.class.getSimpleName());
    }

    public static void launchWeeklyHotLabelUI(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(WeeklyHotLabelFragment.EXTRA_MESSAGE_ID, msgId);
        launchFragmentInNewActivity(context, WeeklyHotLabelFragment.class, arguments);
    }

    public static void launchRelativeLabelsUI(Context context, UserLabel label) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(RelativeLabelsFragment.EXTRA_USER_LABEL, label);
        launchFragmentInNewActivity(context, RelativeLabelsFragment.class, arguments);
    }

    public static void launchAddUserLabelUI(Context context, String keyword) {
        Bundle arguments = null;

        if (!TextUtils.isEmpty(keyword)) {
            arguments = new Bundle();
            arguments.putString(AddUserLabelFragment.ARG_SEARCH_KEYWORD, keyword);
        }
        launchFragmentInNewActivity(context, AddUserLabelFragment.class, arguments);
    }

    public static void launchFriendDetailUI(Context context, String friendUserId) {
        Bundle arguments = new Bundle();
        UserContact contact = ContactsManager.getInstance(context)
                .getUserContactByUserId(friendUserId);
        if (contact != null) {
            launchPersonalDetailUI(context, new PersonalUser(PersonalUser.CONTACT, contact));
        }
    }

    public static void launchShowFriendAvatarImage(Context context, String contactImage) {
        Bundle arguments = new Bundle();
        arguments.putString(ShowAvatarImageFragment.SHOW_FRIEND_AVATAR_IMAGE, contactImage);
        launchFragmentInNewActivity(context, ShowAvatarImageFragment.class, arguments);
    }

    public static void launchUserInfoSettingUI(Context context) {
        launchFragmentInNewActivity(context, SettingUserInfoFragment.class, null);
    }

    public static void launchSettingMainUI(Context context) {
        Intent intent = new Intent(context, SettingMainActivity.class);
        context.startActivity(intent);
    }

    public static void launchAddFriendMainUI(Context context) {
        launchFragmentInNewActivity(context, AddFriendMainFragment.class, null);
    }

    public static void launchNumberSearchUI(Context context) {
        launchFragmentInNewActivity(context, NumberSearchFragment.class, null);
    }

    public static void launchExactSearchFriendUI(Context context) {
        launchFragmentInNewActivity(context, ExactSearchFriendFragment.class, null);
    }

    public static void launchExactGetGameFriendUI(Context context) {
        launchFragmentByReplaceCurrent(context, GetFragment.class, null);
    }

    public static void launchValidateAddFriendListUI(Context context) {
        context.startActivity(getValidateAddFriendListUIIntent(context));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NotificationService.NOTIFICATION_VALIDATE_ADD_FRIEND);
    }

    public static Intent getValidateAddFriendListUIIntent(Context context) {
        return getFragmentInNewActivity(context, ValidateAddFriendListFragment.class, null);
    }

    public static void launchValidateAddFriend(Context context, long msgId) {
        context.startActivity(getValidateAddFriendUIIntent(context, msgId));
    }

    public static Intent getValidateAddFriendUIIntent(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ValidateAddFriendFragment.EXTRA_MESSAGE_ID, msgId);
        return getFragmentInNewActivity(context, ValidateAddFriendFragment.class, arguments);
    }

    public static void launchRejectAddFriend(Context context, long msgId) {
        context.startActivity(getRejectAddFriendUIIntent(context, msgId));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NotificationService.NOTIFICATION_REJECT_RESULT);
    }

    public static Intent getRejectAddFriendUIIntent(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(RejectAddFriendFragment.EXTRA_MESSAGE_ID, msgId);
        return getFragmentInNewActivity(context, RejectAddFriendFragment.class, arguments);
    }

    public static void launchPeopleAroundUI(Context context) {
        launchFragmentInNewActivity(context, PeopleAroundFragment.class, null);
    }

    public static void launchChatRoomUI(Context context) {
        launchFragmentInNewActivity(context, ChatRoomFragment.class, null);
    }

    public static void launchCheckStrangerUserUI(Context context, BaseLabel labels, boolean isShow) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(StrangerFriendShowFragment.EXTRA_SEARCH_LABELS, labels);
        arguments.putBoolean(StrangerFriendShowFragment.SHOW_MENU, isShow);
        launchFragmentByReplaceCurrent(context, StrangerFriendShowFragment.class, arguments);
    }

    public static void launchGetViewPager(Activity context) {
        Intent intent = new Intent(context, GetViewPagerActivity.class);
        context.startActivity(intent);
    }

    public static void launchGroupChattingUI(Activity activity, String groupLabelName,
                                             ArrayList<Stranger> strangerList) {
        Intent intent = new Intent(activity, GroupChattingUI.class);
        intent.putExtra(GroupChattingUI.GROUP_LABEL_NAME, groupLabelName);
        intent.putParcelableArrayListExtra(GroupChattingUI.GROUP_FRIEND, strangerList);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void launchGroupDismissListUI(Context context) {
        launchFragmentInNewActivity(context, GroupDismissMessageListFragment.class, null);
    }

    public static void launchGroupInformationUI(Context context, String groupId, String groupLabelName) {
        Bundle arguments = new Bundle();
        arguments.putString(GroupInformationFragment.GROUP_INFORMATION, groupId);
        arguments.putString(GroupChattingUI.GROUP_LABEL_NAME, groupLabelName);
        launchFragmentByReplaceCurrent(context, GroupInformationFragment.class, arguments);
    }

    public static void launchSearchFriendByLabelsUI(Context context, UserLabel[] labels) {
        if (labels == null || labels.length <= 0) {
            return;
        }

        int length = labels.length;
        BaseLabel[] labelNames = new BaseLabel[length];

        for (int i = 0; i < length; ++i) {
            labelNames[i] = labels[i].toBaseLabel();
        }
        launchSearchFriendByLabelsUI(context, labelNames);
    }

    public static void launchSearchFriendByLabelsUI(Context context, BaseLabel[] labels) {
        Bundle arguments = new Bundle();
        arguments.putParcelableArray(SearchFriendByLabelFragment.EXTRA_SEARCH_LABELS, labels);
        launchFragmentByReplaceCurrent(context, SearchFriendByLabelFragment.class, arguments);
    }

    public static void launchGetByLabelsUI(Context context, String time, Stranger stranger, boolean isMusic) {
        Bundle arguments = new Bundle();
        arguments.putString(GetFragment.GET_GAME_TIME, time);
        arguments.putParcelable(GetFragment.GET_GAME_INFO, stranger);
        arguments.putBoolean(GetFragment.GET_GAME_MUSIC, isMusic);
        launchFragmentByReplaceCurrent(context, GetGameFragment.class, arguments);
    }

    public static void launchStrangerDetailUI(Context context, Stranger stranger) {
        ContactsManager contactsManager = ContactsManager.getInstance(context);
        Bundle arguments = new Bundle();
        arguments.putParcelable(UserShowFragment.EXTRA_STRANGER, stranger);
        if (contactsManager.getUserContactByUserId(stranger.getUserId()) == null) {
            launchPersonalDetailUI(context, new PersonalUser(PersonalUser.STRANGER, new UserContact(stranger)));
        } else {
            launchPersonalDetailUI(context, new PersonalUser(PersonalUser.CONTACT, new UserContact(stranger)));
        }
    }

    public static void launchPersonalDetailUI(Context context, PersonalUser personalUser) {
        ContactsManager contactsManager = ContactsManager.getInstance(context);
        Bundle arguments = new Bundle();
        arguments.putParcelable(UserShowFragment.EXTRA_PERSONAL, personalUser);
        launchActionBarInNewActivity(context, PersonalInfoFragment.class, arguments);
    }

    public static void launchStrangerDetailUI(Activity activity, int requestCode, Stranger stranger) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(UserShowFragment.EXTRA_STRANGER, stranger);
        launchFragmentForResult(activity, requestCode, UserShowFragment.class, arguments);
    }

    public static void launchStrangerDetailUI(Fragment fragment, int requestCode, Stranger stranger) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(UserShowFragment.EXTRA_STRANGER, stranger);
        launchFragmentForResult(fragment, requestCode, UserShowFragment.class, arguments);
    }

    public static void launchTodayRecommendedUI(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(TodayRecommendedFragment.EXTRA_MESSAGE_ID, msgId);
        launchFragmentInNewActivity(context, TodayRecommendedFragment.class, arguments);
    }

    public static void launchWeeklyStarUI(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(WeeklyStarFragment.EXTRA_MESSAGE_ID, msgId);
        launchFragmentInNewActivity(context, WeeklyStarFragment.class, arguments);
    }

    public static void launchWeeklyStarConfirmUI(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(WeeklyStarConfirmFragment.EXTRA_MESSAGE_ID, msgId);
        launchFragmentInNewActivity(context, WeeklyStarConfirmFragment.class, arguments);
    }

    public static void launchBubblingResultUI(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(BubblingResultFragment.EXTRA_MESSAGE_ID, msgId);
        launchFragmentInNewActivity(context, BubblingResultFragment.class, arguments);
    }

    public static void launchUploadAvatarUI(Context context) {
        Intent intent = new Intent(context, AvatarUploadActivity.class);
        context.startActivity(intent);
    }

    public static void launchUploadAvatarUIAndSave(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, AvatarUploadActivity.class);
        intent.putExtra(AvatarUploadActivity.EXTRA_SAVE_CROPPED_IMAGE, true);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launchUploadAvatarUIAndSave(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), AvatarUploadActivity.class);
        intent.putExtra(AvatarUploadActivity.EXTRA_SAVE_CROPPED_IMAGE, true);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchSelectImageUI(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, SelectImageActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launchMultiSelectImageUI(Activity activity, int requestCode) {
        launchMultiSelectImageUI(activity, requestCode, activity.getString(R.string.send),
                activity.getString(R.string.send), 3, null);
    }

    public static void launchMultiSelectImageUI(Activity activity, int requestCode,
                                                String title, String done, int count, ArrayList<String> imageUrls) {
        Intent intent = new Intent(activity, MultiSelectImageActivity.class);
        intent.putExtra(MultiSelectImageActivity.ACTIONBAR_TITLE, title);
        intent.putExtra(MultiSelectImageActivity.SELECT_BUTTON_TEXT, done);
        intent.putExtra(MultiSelectImageActivity.SELECT_IMAGE_PAGER, count);
        intent.putExtra(MultiSelectImageActivity.IMAGE_URLS, imageUrls);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launchMultiSelectImageUI(Fragment fragment, int requestCode) {
        Resources res = fragment.getResources();
        launchMultiSelectImageUI(fragment, requestCode, res.getString(R.string.send),
                res.getString(R.string.send), 3);
    }

    public static void launchMultiSelectImageUI(Fragment fragment, int requestCode,
                                                String title, String done, int count) {
        Activity activity = fragment.getActivity();
        Intent intent = new Intent(activity, MultiSelectImageActivity.class);

        intent.putExtra(MultiSelectImageActivity.ACTIONBAR_TITLE, title);
        intent.putExtra(MultiSelectImageActivity.SELECT_BUTTON_TEXT, done);
        intent.putExtra(MultiSelectImageActivity.SELECT_IMAGE_PAGER, count);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchSelectImageUI(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), SelectImageActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchLabelStoryDetailPhotoUI(Activity activity, int requestCode,
                                                     ArrayList<String> arrayList, int selected) {
        Intent intent = new Intent(activity, LabelStoryDetailPhotoActivity.class);
        intent.putStringArrayListExtra(SendLabelStoryFragment.DETAIL_IMAGE_LIST, arrayList);
        intent.putExtra(SendLabelStoryFragment.DETAIL_IMAGE_SELECTED, selected);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launchImageViewSelectUI(Fragment fragment, int requestCode, Uri imageUri) {
        Intent intent = new Intent(fragment.getActivity(), ImageViewSelectActivity.class);
        intent.setData(imageUri);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchImageViewSelectUI(Activity activity, int requestCode, Uri imageUri) {
        Intent intent = new Intent(activity, ImageViewSelectActivity.class);
        intent.setData(imageUri);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launchImageViewUI(Context context, Uri imageUri) {
        Intent intent = new Intent(context, ImageViewActivity.class);
        intent.setData(imageUri);
        context.startActivity(intent);
    }

    public static void launchFeedbackUI(Context context) {
        Intent intent = new Intent(context, FeedbackActivity.class);
        context.startActivity(intent);
    }

    public static void launchSettingsUI(Context context) {
        launchFragmentInNewActivity(context, SettingsFragment.class, null);
    }

    public static void launchFragmentGetDialogUI(Activity activity, FragmentManager fm,
                                                 String title, String message, String know,
                                                 int resource,
                                                 GetDialogFragment.GetOnclickListener getOnclickListener) {
        GetDialogFragment getDialogFragment = GetDialogFragment.newInstance(activity, title,
                message, know, resource);
        getDialogFragment.setGetOnclickListener(getOnclickListener);
        getDialogFragment.show(fm, GetDialogFragment.class.getSimpleName());
    }

    public static void launchFragmentLabelStoryUI(Context context, LabelStoryCategory category, String userId) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(LabelStoryUtils.CATEGORY, category);
        arguments.putString(LabelStoryUtils.LABEL_STORY_USER_ID, userId);
        launchFragmentInNewActivity(context, LabelStoryFragment.class, arguments);
    }

    public static void launchFragmentLabelStoryUI(Context context, BaseLabel label, String userId) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(LabelStoryFragment.LABEL, label);
        arguments.putString(LabelStoryUtils.LABEL_STORY_USER_ID, userId);
        launchFragmentInNewActivity(context, LabelStoryFragment.class, arguments);
    }

    public static void launchFragmentSendLabelStoryUI(Activity context, Fragment fragment, LabelStoryCategory category, boolean isShowBunding, int resultCode) {
        Intent intent = new Intent(context, SendLabelStoryFragment.class);
        intent.putExtra(LabelStoryUtils.CATEGORY, category);
        intent.putExtra(LabelStoryUtils.LABEL_ISSHOW_BUNDING, isShowBunding);
        fragment.startActivityForResult(intent, resultCode);
    }

    public static void launchFragmentSendLabelStoryUI(Activity context, BaseLabel label, int resultCode) {
        Intent intent = new Intent(context, SendLabelStoryFragment.class);
        intent.putExtra(LabelStoryFragment.LABEL, label);
        context.startActivityForResult(intent, resultCode);
    }

    public static void launchFragmentSendLabelStoryUI(Activity context, int resultCode) {
        Intent intent = new Intent(context, SendLabelStoryFragment.class);
        context.startActivityForResult(intent, resultCode);
    }

    public static void launchFragmentLabelStoryDetaileUI(Context context, DynamicArguments arg) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(LabelStoryFragment.LABEL_STORY, arg.getLabelStory());
        arguments.putInt(LabelStoryUtils.TAG, arg.getTag());
        arguments.putBoolean(LabelStoryUtils.LABEL_STORY_SHOW, arg.isShowFragment());
        arguments.putBoolean(LabelStoryUtils.LABEL_STORY_TITLE_SHOW, arg.isShowTitle());
        arguments.putBoolean(LabelStoryUtils.IS_PRAISE, arg.isPraise());
        arguments.putBoolean(LabelStoryUtils.IS_COMMENT, arg.isComment());
        arguments.putBoolean(LabelStoryUtils.IS_KEYBROAD, arg.isShowKeyBroad());
        launchFragmentInNewActivity(context, LabelStoryDetaileFragment.class, arguments);
    }

    public static void launchFragmentLabelStoryDetaileActivityUI(Fragment fragment, ArrayList<LabelStory> labelStory, int position, String categoryName, int tag, int resultCode) {
        Intent intent = new Intent(fragment.getActivity(), LabelStoryDetailViewPagerActivity.class);
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(LabelStoryDetailViewPagerActivity.VIEW_PAGER_LIST_INFO, labelStory);
        arguments.putString(LabelStoryUtils.CATEGORY_NAME, categoryName);
        arguments.putInt(LabelStoryUtils.TAG, tag);
        arguments.putInt(LabelStoryDetailViewPagerActivity.LABEL_STORY_POSITION, position);
        intent.putExtras(arguments);
        fragment.startActivityForResult(intent, resultCode);
    }

    public static void launchFragmentLabelStoryDetaileActivityUI(Fragment fragment, ArrayList<LabelStory> labelStory, int position, int tag, int resultCode, Stranger stranger) {
        Intent intent = new Intent(fragment.getActivity(), LabelStoryDetailViewPagerActivity.class);
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(LabelStoryDetailViewPagerActivity.VIEW_PAGER_LIST_INFO, labelStory);
        arguments.putParcelable(LabelStoryUtils.STRANGER, stranger);
        arguments.putInt(LabelStoryUtils.TAG, tag);
        arguments.putInt(LabelStoryDetailViewPagerActivity.LABEL_STORY_POSITION, position);
        intent.putExtras(arguments);
        fragment.startActivityForResult(intent, resultCode);
    }

    public static void launchFragmentPraiseCrowd(Context context, UserPraise[] user) {
        Bundle arguments = new Bundle();
        arguments.putParcelableArray(ShowPraiseCrowdFragment.PRAISE_CROWD, user);
        launchFragmentInNewActivity(context, ShowPraiseCrowdFragment.class, arguments);
    }

    public static void launchFragmentPickPhotoCrowd(Context context, PickPhotoUser[] user, String title) {
        Bundle arguments = new Bundle();
        arguments.putString(PickPhotoUserFragment.PICK_PHOTO_TITLE, title);
        arguments.putParcelableArray(PickPhotoUserFragment.PICK_PHOTO_CROWD, user);
        launchFragmentInNewActivity(context, PickPhotoUserFragment.class, arguments);
    }

    public static void launchMyThrowPhotosUI(Context context) {
        UILauncher.launchFragmentInNewActivity(context,
                MyThrowPhotosFragment.class, null);
    }

    public static void launchThrowPhotoUI(Context context) {
        UILauncher.launchFragmentInNewActivity(context,
                ThrowPhotoFragment.class, null);
    }

    public static void launchFindLabelUI(Context context) {
        UILauncher.launchFragmentInNewActivity(context, ShowLabelFragment.class, null);
    }

    public static void launchRecommendLabelShowUI(Context context, long msgId) {
        Bundle arguments = new Bundle();
        arguments.putLong(RecommendLabelShowFragment.EXTRA_MESSAGE_ID, msgId);
        launchFragmentInNewActivity(context, RecommendLabelShowFragment.class, arguments);
    }

    public static void launchRecommendFriendShowUI(Context context, long messageId) {
        Bundle arguments = new Bundle();
        arguments.putLong(RecommendFriendMainFragment.EXTRA_MESSAGE_ID, messageId);
        launchFragmentInNewActivity(context, RecommendFriendMainFragment.class, arguments);
    }

    public static void launchSelectSystemLabelUI(Fragment fragment, int requestCode,
                                                 String[] filterLabelIds,
                                                 String title, String selectMenu,
                                                 int selectCount) {
        Bundle arguments = new Bundle();
        arguments.putStringArray(SelectSystemLabelFragment.ARG_FILTER_LABEL_IDS, filterLabelIds);
        arguments.putString(SelectSystemLabelFragment.ARG_TITLE, title);
        arguments.putString(SelectSystemLabelFragment.ARG_SELECT_MENU, selectMenu);
        arguments.putInt(SelectSystemLabelFragment.ARG_SELECT_COUNT, selectCount);
        launchFragmentForResult(fragment, requestCode, SelectSystemLabelFragment.class, arguments);
    }

    public static void launchLabelStoryLetterCompleterMsgUI(Context context) {
        Bundle arguments = new Bundle();
        launchFragmentInNewActivity(context, LetterCompleteMsgFragment.class, arguments);
    }

    public static void launchLabelStoryLetterMsgUI(Context context, String flags) {
        context.startActivity(getLabelStoryLetterMsgUIIntent(context, flags));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NotificationService.NOTIFICATION_PRIVATE_LETTER_NOTIFY);
    }

    public static Intent getLabelStoryLetterMsgUIIntent(Context context, String flags) {
        Bundle arguments = new Bundle();
        arguments.putString(LetterMessageFragment.LETTER_FLAGS, flags);
        return getFragmentInNewActivity(context, LetterMessageFragment.class, arguments);
    }


    public static void launchStrangerChattingUI(Context context, Stranger stranger) {
        ContactsManager contactsManager = ContactsManager.getInstance(context);
        StrangerManager strangerManager = StrangerManager.getInstance(context);

        if (contactsManager.getUserContactByUserId(stranger.getUserId()) == null) {
            strangerManager.addStranger(stranger);
        }
        context.startActivity(getChattingUIIntent(context, stranger.getUserId()));
    }

    public static void launchLabelStoryShowPhotoUI(Context context, String avatar) {
        Intent intent = new Intent(context, ShowBigImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LabelStoryUtils.SHOW_PHOTO_URL, avatar);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void launchStoryShowPhotoUI(Context context, Bitmap bitmap) {
        Intent intent = new Intent(context, ShowBigImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ShowBigImageActivity.EXTRA_BITMAP, bitmap);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void launchLabelPlayUI(Context context) {
        UILauncher.launchFragmentInNewActivity(context, LabelPlayFragment.class, null);
    }

    public static void launchNewUserUI(Context context) {
        UILauncher.launchFragmentInNewActivity(context, NewUserFragment.class, null);
    }

    public static void launchUserDiscoveryUI(Context context) {
        UILauncher.launchFragmentInNewActivity(context, UserDiscoveryFragment.class, null);
    }

    public static void launchSelectUserTagUI(Context context) {
        launchFragmentInNewActivity(context, SelectUserTagFragment.class, null);
    }

    public static void launchShowSelectUserTagUI(Context context) {
        launchFragmentInNewActivity(context, ShowSelectTagFragment.class, null);
    }

    public static void launchMyAlbumUI(Context context) {
        launchFragmentInNewActivity(context, MyAlbumFragment.class, null);
    }

    public static void launchMyAlbumGalleryUI(Context context, AlbumPhoto[] photos,
                                              int defaultItem) {
        Bundle arguments = new Bundle();
        arguments.putParcelableArray(MyAlbumGalleryFragment.EXTRA_ALBUM_PHOTOS, photos);
        arguments.putInt(MyAlbumGalleryFragment.EXTRA_DEFAULT_ITEM, defaultItem);
        launchFragmentInNewActivity(context, MyAlbumGalleryFragment.class, arguments);
    }

    public static void launchAlbumGalleryUI(Context context, LiteStranger user) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(AlbumGalleryFragment.EXTRA_QUERY_USER, user);
        launchFragmentInNewActivity(context, AlbumGalleryFragment.class, arguments);
    }

    public static void launchAlbumGalleryUI(Context context, LiteStranger user, String photoId) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(AlbumGalleryFragment.EXTRA_QUERY_USER, user);
        arguments.putString(AlbumGalleryFragment.EXTRA_PHOTO_ID, photoId);
        launchFragmentInNewActivity(context, AlbumGalleryFragment.class, arguments);
    }

    public static void launchPhotoNotifyUI(Context context, int pushType) {
        context.startActivity(getPhotoNotifyUIIntent(context, pushType));
    }

    public static Intent getPhotoNotifyUIIntent(Context context, int pushType) {
        Bundle arguments = new Bundle();
        arguments.putInt(PhotoNotifyFragment.EXTRA_PUSH_TYPE, pushType);
        return getFragmentInNewActivity(context, PhotoNotifyFragment.class, arguments);
    }

    public static void launchDynamicNotifyUI(Context context, int pushType) {
        context.startActivity(getDynamicNotifyUIIntent(context, pushType));
    }

    public static Intent getDynamicNotifyUIIntent(Context context, int pushType) {
        Bundle arguments = new Bundle();
        arguments.putInt(DynamicMessageFragment.EXTRA_PUSH_TYPE, pushType);
        return getFragmentInNewActivity(context, DynamicMessageFragment.class, arguments);
    }

    public static void launchStrangerBaseInfo(FragmentManager fm, Stranger stranger) {
        StrangerInfoDialog fragment = StrangerInfoDialog.newInstance(stranger);
        fragment.show(fm, StrangerInfoDialog.class.getSimpleName());
    }

    public static void launchContactBaseInfo(FragmentManager fm, UserContact contact) {
        ContactInfoDialog fragment = ContactInfoDialog.newInstance(contact);

        fragment.show(fm, ContactInfoDialog.class.getSimpleName());
    }

    public static void launchActionBarInNewActivity(Context context,
                                                    Class<? extends Fragment> fragment,
                                                    Bundle arguments) {
        context.startActivity(getActionBarInNewActivity(context, fragment, arguments));
    }

    public static Intent getActionBarInNewActivity(Context context,
                                                   Class<? extends Fragment> fragment,
                                                   Bundle arguments) {
        Intent intent = new Intent(context, FadingActionBarActivity.class);
        intent.putExtra(FadingActionBarActivity.EXTRA_SHOW_FRAGMENT, fragment.getName());
        if (arguments != null) {
            intent.putExtra(FadingActionBarActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, arguments);
        }
        return intent;
    }

    public static void launchMyLabelStoryUI(Context context, String userId, Stranger stranger) {
        Bundle args = new Bundle();
        args.putInt(MixDynamicArgs.ARGS_SCENARIO_TYPE, DynamicScenario.USER_OWN.toInt());
        args.putString(MixDynamicArgs.ARGS_USER_ID, userId);
        args.putParcelable(MixDynamicArgs.ARGS_USER_STRANGER, stranger);
        launchFragmentInNewActivity(context, MixDynamicAllFragment.class, args);
    }

    public static void launchInterestUI(Context context, String userId) {
        Bundle arguments = new Bundle();
        arguments.putString(LabelStoryUtils.LABEL_STORY_USER_ID, userId);
        launchFragmentInNewActivity(context, InterestFragment.class, arguments);
    }

    public static void launchRecentVisitorUI(Context context, String userId) {
        Bundle arguments = new Bundle();
        arguments.putString(LabelStoryUtils.LABEL_STORY_USER_ID, userId);
        launchFragmentInNewActivity(context, RecentVisitorsFragment.class, arguments);
    }

    public static void launchAddInterestUI(Fragment fragment, Context context,
                                           int resultCode, InterestType interestType) {
        Intent intent = new Intent(context, AddInterestFragment.class);
        Bundle arguments = new Bundle();
        arguments.putParcelable(InterestFragment.INTEREST_TYPE, interestType);
        intent.putExtras(arguments);
        fragment.startActivityForResult(intent, resultCode);
    }

    public static void launchInviteUserUI(Context context) {
        context.startActivity(getInviteUserUIIntent(context));
    }

    public static Intent getInviteUserUIIntent(Context context) {
        return getFragmentInNewActivity(context, InviteListFragment.class, null);
    }

    public static void launchBigActivityUI(Context context, String url) {
        Intent intent = new Intent(context, ShowBigImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LabelStoryUtils.SHOW_PHOTO_URL, url);
        context.startActivity(intent);
    }

    public static void launchAttentionListUI(Context context) {
        context.startActivity(getAttentionListUIIntent(context));
    }

    public static Intent getAttentionListUIIntent(Context context) {
        return getFragmentInNewActivity(context, AttentionListFragment.class, null);
    }

    public static void launchConfideUI(Context context, boolean isMyConfide) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ConfideUtils.IS_MY_CONFIDE, isMyConfide);
        launchFragmentInNewActivity(context, ConfideShowFragment.class, bundle);
    }

    public static void launchEditConfideUI(Context context) {
        context.startActivity(new Intent(context, EditConfideActivity.class));
    }

    public static void launchConfideDetaileUI(Fragment fragment, Confide confide, int requestCode, int index) {
        Intent intent = new Intent(fragment.getActivity(), ConfideDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ConfideUtils.CONFIDE, confide);
        bundle.putInt(ConfideUtils.CONFIDE_INDEX, index);
        intent.putExtras(bundle);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchConfideDetaileUI(Fragment fragment, Confide confide, int requestCode, int index, boolean isShowSoft) {
        Intent intent = new Intent(fragment.getActivity(), ConfideDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ConfideUtils.CONFIDE, confide);
        bundle.putInt(ConfideUtils.CONFIDE_INDEX, index);
        bundle.putBoolean(ConfideUtils.IS_SHOW_SOFT, isShowSoft);
        intent.putExtras(bundle);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void launchConfideDetaileUI(Context context, Confide confide, int index) {
        Intent intent = new Intent(context, ConfideDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ConfideUtils.CONFIDE, confide);
        bundle.putInt(ConfideUtils.CONFIDE_INDEX, index);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void launchShowConfideRoleUI(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ShowConfideRoleFragment.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launchMyInfoUI(Context context) {
        launchActionBarInNewActivity(context, MyInfoFragment.class, null);
    }

    public static void launchSelectChatBgUI(Context context) {
        launchFragmentInNewActivity(context, ChatBgSelectFragment.class, null);
    }

    public static void launchVoiceUI(Activity activity) {
        Intent intent = new Intent(activity, VoicePushUI.class);
        activity.startActivity(intent);
    }

    public static void launchMessageNotifySettingUI(Context context) {
        launchFragmentInNewActivity(context, MessageNotifySettingFragment.class, null);
    }

    public static void launchInputCommentUI(Fragment fragment, int resultCode) {
        Intent intent = new Intent(fragment.getActivity(), InputCommentActivity.class);
        fragment.startActivityForResult(intent, resultCode);
    }

    public static void launchInputCommentUI(Fragment fragment, String replyName, int resultCode) {
        Intent intent = new Intent(fragment.getActivity(), InputCommentActivity.class);
        intent.putExtra(InputCommentActivity.REPLY_NAME, replyName);
        fragment.startActivityForResult(intent, resultCode);
    }

    public static Intent getConfideNotifyUIIntent(Context context, int pushType) {
        Bundle arguments = new Bundle();
        arguments.putInt(ConfideNotifyFragment.EXTRA_PUSH_TYPE, pushType);
        return getFragmentInNewActivity(context, ConfideNotifyFragment.class, arguments);
    }

    public static void launchConfideNotifyUI(Context context, int type) {
        context.startActivity(getConfideNotifyUIIntent(context, type));
    }

    public static void launchMyOwnMixDynamicUI(Context context) {
        Bundle args = new Bundle();
        args.putInt(MixDynamicArgs.ARGS_SCENARIO_TYPE, DynamicScenario.MY_OWN.toInt());
        launchFragmentInNewActivity(context, MixDynamicAllFragment.class, args);
    }

    public static void launchDynamicNewMessageUI(Context context) {
        launchFragmentInNewActivity(context, NewMessageFragment.class, null);
    }

    public static Intent getSystemNotifyIntent(Context context, int type) {
        Bundle arguments = new Bundle();
        arguments.putInt(SystemPushUtils.SYSTEM_PUSH_TYPE, type);
        return getFragmentInNewActivity(context, SystemPushFragment.class, arguments);
    }

    public static void launchSystemNotifyUI(Context context, int type) {
        context.startActivity(getSystemNotifyIntent(context, type));
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NotificationService.NOTIFICATION_SYSTEM_NOTIFY);
    }

    public static void launchMoodUI(Context context) {
        launchFragmentInNewActivity(context, MoodFragment.class, null);
    }

    public static void launchMoodUserListUI(Fragment fragment, ArrayList<UserGroup> userGroups, ArrayList<MoodUser> moodUsers, int resultCode) {
        Intent intent = new Intent(fragment.getActivity(), MoodUserListFragment.class);
        intent.putExtra(MoodUtils.USERIDS, moodUsers);
        intent.putExtra(MoodUtils.USER_GROUP, userGroups);
        fragment.startActivityForResult(intent, resultCode);
    }

    public static void launchFaceBanknoteUI(Context context) {
        context.startActivity(new Intent(context, FaceBanknoteActivity.class));
    }

    public static void launchMusicListUI(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, MusicListUI.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launchImageGalleryUI(Context context, String[] images, int position) {
        Bundle args = new Bundle();
        args.putStringArray(ImageGalleryFragment.IMAGES, images);
        args.putInt(ImageGalleryFragment.POSITION, position);
        launchFragmentInNewActivity(context, ImageGalleryFragment.class, args);
    }

    public static void launchSelectPhotoImageUI(Activity activity, int requestCode,
                                                String title, String done, int count, ArrayList<String> imageUrls) {
        Intent intent = new Intent(activity, SelectPhotoImageActivity.class);
        intent.putExtra(MultiSelectImageActivity.ACTIONBAR_TITLE, title);
        intent.putExtra(MultiSelectImageActivity.SELECT_BUTTON_TEXT, done);
        intent.putExtra(MultiSelectImageActivity.SELECT_IMAGE_PAGER, count);
        intent.putExtra(MultiSelectImageActivity.IMAGE_URLS, imageUrls);
        activity.startActivityForResult(intent, requestCode);
    }
}
