package com.ekuater.labelchat.ui.fragment.friends;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.UserContact;
import com.ekuater.labelchat.datastruct.UserLabel;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.MiscUtils;

/**
 * Created by Leo on 2015/1/15.
 *
 * @author LinYong
 */
/*package*/ class UserDetailHelper {

    public static class Detail {

        private String labelCode;
        private String nickname;
        private int gender;
        private int age;
        private String province;
        private String city;
        private int constellation;
        private String avatar;
        private String avatarThumb;
        private UserLabel[] labels;
        private LocationInfo location;
        private boolean isFriend;

        public Detail(UserContact contact) {
            labelCode = contact.getLabelCode();
            nickname = contact.getNickname();
            gender = contact.getSex();
            age = contact.getAge();
            province = contact.getProvince();
            city = contact.getCity();
            constellation = contact.getConstellation();
            avatar = contact.getAvatar();
            avatarThumb = contact.getAvatarThumb();
            labels = contact.getLabels();
            location = null;
            isFriend = true;
        }

        public Detail(Stranger stranger, boolean isFriend) {
            labelCode = stranger.getLabelCode();
            nickname = stranger.getNickname();
            gender = stranger.getSex();
            age = stranger.getAge();
            province = stranger.getProvince();
            city = stranger.getCity();
            constellation = stranger.getConstellation();
            avatar = stranger.getAvatar();
            avatarThumb = stranger.getAvatarThumb();
            labels = stranger.getLabels();
            location = stranger.getLocation();
            this.isFriend = isFriend;
        }

        public String getLabelCode() {
            return labelCode;
        }

        public String getNickname() {
            return nickname;
        }

        public int getGender() {
            return gender;
        }

        public int getAge() {
            return age;
        }

        public String getProvince() {
            return province;
        }

        public String getCity() {
            return city;
        }

        public int getConstellation() {
            return constellation;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getAvatarThumb() {
            return avatarThumb;
        }

        public UserLabel[] getLabels() {
            return labels;
        }

        public LocationInfo getLocation() {
            return location;
        }

        public boolean isFriend() {
            return isFriend;
        }
    }

    public interface Listener {

        public void onLabelClick(UserLabel label);

        public void onLabelLongClick(UserLabel label);

        public void onLabelRecommend();
    }

    public static class AbsListener implements Listener {

        @Override
        public void onLabelClick(UserLabel label) {
        }

        @Override
        public void onLabelLongClick(UserLabel label) {
        }

        @Override
        public void onLabelRecommend() {
        }
    }

    private interface ListenerNotifier {
        public void notify(Listener listener);
    }

    private Context mContext;
    private Listener mListener;
    private Detail mDetail;
    private View mParentView;
    private UserPraiseLabelAdapter mLabelAdapter;

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.avatar_image:
                    UILauncher.launchShowFriendAvatarImage(mContext, mDetail.getAvatar());
                    break;
                default:
                    break;
            }
        }
    };

    private LinearLayout mDetailLayout;
    private View mInfoArea;
    private View mOperationArea;
    private ListView mLabelListView;
    private boolean mLimitDetailLayoutHeight = false;

    public UserDetailHelper(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
        mLabelAdapter = new UserPraiseLabelAdapter(context,
                new UserPraiseLabelAdapter.AdapterListener() {
                    @Override
                    public void onLabelClick(UserLabel label) {
                        notifyLabelClick(label);
                    }

                    @Override
                    public void onLabelLongClick(UserLabel label) {
                        notifyLabelLongClick(label);
                    }

                    @Override
                    public void onLabelRecommend() {
                        notifyLabelRecommend();
                    }
                });
    }

    public void setDetail(Detail detail) {
        mDetail = detail;
        mLabelAdapter.updateUserLabels(mDetail.getLabels());
        /*if (mDetail.isFriend()) {
            mLabelAdapter.addLabelRecommendItem();
        }*/
        mLabelAdapter.updateItemDone();
    }

    public void setParentView(View parentView) {
        mParentView = parentView;
        mDetailLayout = (LinearLayout) mParentView.findViewById(R.id.detail_area);
        mInfoArea = mParentView.findViewById(R.id.info_area);
        mOperationArea = mParentView.findViewById(R.id.operation_area);
        mLabelListView = (ListView) mParentView.findViewById(R.id.label_list);
        mDetailLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                limitDetailLayoutHeight();
            }
        });
        mLabelListView.setAdapter(mLabelAdapter);
        mLabelListView.setOnItemClickListener(mLabelAdapter);
        mLabelListView.setOnItemLongClickListener(mLabelAdapter);
    }

    public void bindInfo() {
        if (mDetail == null || mParentView == null) {
            return;
        }

        Resources resources = mContext.getResources();
        AvatarManager avatarManager = AvatarManager.getInstance(mContext);
        LocationInfo myLocation = AccountManager.getInstance(mContext).getLocation();
        ImageView avatarImage = (ImageView) mParentView.findViewById(R.id.avatar_image);
        TextView nicknameView = (TextView) mParentView.findViewById(R.id.nickname);
        ImageView sexImage = (ImageView) mParentView.findViewById(R.id.gender);
        TextView labelCodeView = (TextView) mParentView.findViewById(R.id.label_code);
        TextView cityView = (TextView) mParentView.findViewById(R.id.city);
        TextView constellationView = (TextView) mParentView.findViewById(R.id.constellation);
        TextView ageView = (TextView) mParentView.findViewById(R.id.age);
        TextView distanceView = (TextView) mParentView.findViewById(R.id.distance);

        nicknameView.setText(mDetail.getNickname());
        sexImage.setImageResource(ConstantCode.getSexImageResource(mDetail.getGender()));
        labelCodeView.setText(mDetail.getLabelCode());
        final String city = getCity(mDetail);
        cityView.setText(city);
        cityView.setVisibility(TextUtils.isEmpty(city) ? View.GONE : View.VISIBLE);
        constellationView.setText(UserContact.getConstellationString(
                resources, mDetail.getConstellation()));
        constellationView.setVisibility((mDetail.getConstellation() >= 0)
                ? View.VISIBLE : View.GONE);
        ageView.setText(UserContact.getAgeString(resources, mDetail.getAge()));
        ageView.setVisibility((mDetail.getAge() >= 0) ? View.VISIBLE : View.GONE);
        MiscUtils.showAvatarThumb(avatarManager, mDetail.getAvatarThumb(), avatarImage);
        avatarImage.setOnClickListener(mClickListener);

        LocationInfo location = mDetail.getLocation();
        if (!mDetail.isFriend() && myLocation != null && location != null) {
            distanceView.setText(MiscUtils.getDistanceString(mContext,
                    myLocation.getDistance(location)));
            distanceView.setVisibility(View.VISIBLE);
        } else {
            distanceView.setVisibility(View.GONE);
        }
    }

    private String getCity(Detail detail) {
        final String province = detail.getProvince();
        final String city = detail.getCity();
        String cityAddress = "";

        if (!TextUtils.isEmpty(province)) {
            cityAddress += province + "  ";
        }
        if (!TextUtils.isEmpty(city)) {
            cityAddress += (city.equals(province)) ? "" : city;
        }

        return cityAddress.trim();
    }

    public void bindLabels() {
        mLabelAdapter.notifyDataSetChanged();
        needLimitDetailLayoutHeight();
    }

    private void needLimitDetailLayoutHeight() {
        mLimitDetailLayoutHeight = true;
        mDetailLayout.requestLayout();
    }

    private void limitDetailLayoutHeight() {
        final View view = mParentView;

        if (!mLimitDetailLayoutHeight || view == null) {
            return;
        }

        mLimitDetailLayoutHeight = false;

        final ViewGroup.MarginLayoutParams detailLp = (ViewGroup.MarginLayoutParams)
                mDetailLayout.getLayoutParams();
        final int detailMaxHeight = view.getMeasuredHeight()
                - mOperationArea.getMeasuredHeight();
        final int infoAreaHeight = mInfoArea.getMeasuredHeight();

        if (infoAreaHeight >= detailMaxHeight) {
            detailLp.height = detailMaxHeight;
        } else {
            final ViewGroup.MarginLayoutParams listLp = (ViewGroup.MarginLayoutParams)
                    mLabelListView.getLayoutParams();
            final int maxLabelListHeight = detailMaxHeight - listLp.bottomMargin
                    - listLp.topMargin;
            final int minLabelListHeight = infoAreaHeight - listLp.bottomMargin
                    - listLp.topMargin;
            detailLp.height = measureListViewHeight(mLabelListView, minLabelListHeight,
                    maxLabelListHeight) + listLp.bottomMargin + listLp.topMargin;
        }

        mDetailLayout.getParent().requestLayout();
    }

    private int measureListViewHeight(ListView listView, int minHeight, int maxHeight) {
        final ListAdapter adapter = listView.getAdapter();
        final int dividerHeight = listView.getDividerHeight();
        int height = 0;

        for (int i = 0, len = adapter.getCount(); i < len; i++) {
            View itemView = adapter.getView(i, null, listView);
            itemView.measure(0, 0);
            height += itemView.getMeasuredHeight();
            height += (i > 0) ? dividerHeight : 0;

            if (height >= maxHeight) {
                height = maxHeight;
                break;
            }
        }

        return Math.max(minHeight, height);
    }

    private void notifyListener(ListenerNotifier notifier) {
        if (mListener != null) {
            notifier.notify(mListener);
        }
    }

    private void notifyLabelClick(UserLabel label) {
        notifyListener(new LabelClickNotifier(label));
    }

    private void notifyLabelLongClick(UserLabel label) {
        notifyListener(new LabelLongClickNotifier(label));
    }

    private void notifyLabelRecommend() {
        notifyListener(new LabelRecommendNotifier());
    }

    private static class LabelClickNotifier implements ListenerNotifier {

        private final UserLabel mLabel;

        public LabelClickNotifier(UserLabel label) {
            mLabel = label;
        }

        @Override
        public void notify(Listener listener) {
            listener.onLabelClick(mLabel);
        }
    }

    private static class LabelLongClickNotifier implements ListenerNotifier {

        private final UserLabel mLabel;

        public LabelLongClickNotifier(UserLabel label) {
            mLabel = label;
        }

        @Override
        public void notify(Listener listener) {
            listener.onLabelLongClick(mLabel);
        }
    }

    private static class LabelRecommendNotifier implements ListenerNotifier {

        @Override
        public void notify(Listener listener) {
            listener.onLabelRecommend();
        }
    }
}
