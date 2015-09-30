
package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LocationInfo;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.datastruct.TmpGroup;
import com.ekuater.labelchat.delegate.AccountManager;
import com.ekuater.labelchat.delegate.TmpGroupManager;
import com.ekuater.labelchat.settings.SettingHelper;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.adapter.GroupInformationGridViewAdapter;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.GroupInformationGridView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupInformationFragment extends Fragment {
    public static final String GROUP_INFORMATION = "group_information_id";
    private GroupInformationGridView mGroupInformationGridView;
    private GroupInformationGridViewAdapter mGroupInformationGridViewAdapter;
    private ArrayList<Stranger> mStrangerList;
    private Button mExitGroup;
    private TmpGroupManager mTmpGroupManager;
    private String mGroupId;
    private TmpGroup mGroup;
    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private FragmentActivity mContext;
    private LocationInfo mLocationInfo;
    public static final int QUERY_GROUP_INFO_SUCCESS=1;
    public static final int QUERY_GROUP_INFO_FAILD=2;
    public static final int DISMISS_GROUP_SUCCESS=3;
    public static final int DISMISS_GROUP_FAILD=4;
    public static final int QUIT_GROUP_SUCCESS=5;
    public static final int QUIT_GROUP_FAILD=6;


    private TmpGroupManager.IListener mDismissGroupListener = new TmpGroupManager.AbsListener() {

        @Override
        public void onDismissGroupRequestResult(int result, String groupId) {
            super.onDismissGroupRequestResult(result, groupId);
            Message msg=Message.obtain();
            if (result== ConstantCode.TMP_GROUP_OPERATION_SUCCESS){
                msg.what=DISMISS_GROUP_SUCCESS;
            }else{
                msg.what=DISMISS_GROUP_FAILD;
            }
            mHandler.sendMessage(msg);
        }

        @Override
        public void onQuitGroupResult(int result, String groupId) {
            super.onQuitGroupResult(result, groupId);
            Message msg=Message.obtain();
            if (result== ConstantCode.TMP_GROUP_OPERATION_SUCCESS){
                msg.what=QUIT_GROUP_SUCCESS;
            }else{
                msg.what=QUIT_GROUP_FAILD;
            }
            mHandler.sendMessage(msg);
        }

        @Override
        public void onQueryGroupInfoResult(int result, String groupId, TmpGroup group) {
            super.onQueryGroupInfoResult(result, groupId, group);
                Message msg=Message.obtain();
                msg.what=QUERY_GROUP_INFO_SUCCESS;
                mGroup=group;
                mHandler.sendMessage(msg);
        }
    };

    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case QUERY_GROUP_INFO_SUCCESS:
                    if (mGroup!=null) {
//                        mGroupInformationGridViewAdapter = new GroupInformationGridViewAdapter(getActivity(),
//                                mGroup.getMembers());
//                        mGroupInformationGridView.setAdapter(mGroupInformationGridViewAdapter);
                    }
                    mProgressBar.setVisibility(View.GONE);
                    mScrollView.setVisibility(View.VISIBLE);
                    break;
                case QUERY_GROUP_INFO_FAILD:

                    break;
                case QUIT_GROUP_SUCCESS:
                    ShowToast.makeText(mContext, R.drawable.emoji_sad, getString(R.string.exitsuccesegroup)).show();

                    UILauncher.launchMainUI(mContext);

                    break;
                case QUIT_GROUP_FAILD:
                    ShowToast.makeText(mContext, R.drawable.emoji_cry, getString(R.string.exitfaildgroup)).show();
                    break;
                case DISMISS_GROUP_SUCCESS:
                    ShowToast.makeText(mContext, R.drawable.emoji_sad, getString(R.string.dismisssuccesegroup)).show();

                    UILauncher.launchMainUI(mContext);

                    break;
                case DISMISS_GROUP_FAILD:
                    ShowToast.makeText(mContext, R.drawable.emoji_cry, getString(R.string.dismissfaildgroup)).show();

                    break;

            }

        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getActivity();
        mLocationInfo = AccountManager.getInstance(mContext).getLocation();
        mTmpGroupManager = TmpGroupManager.getInstance(getActivity());
        mTmpGroupManager.registerListener(mDismissGroupListener);
        parseArguments();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.groupInformation);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_information, container, false);
        init(view);
        mGroupInformationGridView.setOnItemClickListener(mOnItemClickListener);
        if (mGroup!=null) {
            Stranger[] strangers=mGroup.getMembers();
            List<Stranger> strangerList= Arrays.asList(strangers);
            ArrayList<Stranger> strangerArrayList=new ArrayList<Stranger>();
            strangerArrayList.addAll(strangerList);
            Stranger stranger=null;
            Stranger acountStranger=null;
            for (int i=0;i<strangerArrayList.size();i++){
                Stranger strangerL=strangerArrayList.get(i);
                if (strangerL.getUserId().equals(mGroup.getCreateUserId())){
                    stranger=strangerArrayList.get(i);
                }
                if (strangerL.getUserId().equals(AccountManager.getInstance(mContext).getUserId())){
                    acountStranger=strangerArrayList.get(i);
                }

            }
            strangerArrayList.remove(stranger);
//            strangerArrayList.remove(acountStranger);
            Collections.sort(strangerArrayList, new Comparator<Stranger>() {
                @Override
                public int compare(Stranger lhs, Stranger rhs) {

                    LocationInfo locationInfo = AccountManager.getInstance(mContext).getLocation();
                    double lhs1;
                    double lhs2;
                    if (lhs.getLocation()!=null&&rhs.getLocation()!=null) {
                         lhs1 = LocationInfo.distance(locationInfo, lhs.getLocation());
                         lhs2 = LocationInfo.distance(locationInfo, rhs.getLocation());
                    }else{
                         lhs1=0.02;
                         lhs2=0.02;
                    }
                    BigDecimal data1 = new BigDecimal(lhs1);
                    BigDecimal data2 = new BigDecimal(lhs2);
                    return data1.compareTo(data2);
                }
            });
            strangerArrayList.add(0, stranger);
            mGroupInformationGridViewAdapter = new GroupInformationGridViewAdapter(getActivity(),
                    strangerArrayList);
            mGroupInformationGridView.setAdapter(mGroupInformationGridViewAdapter);
        }
        mProgressBar.setVisibility(View.GONE);
        mScrollView.setVisibility(View.VISIBLE);
        return view;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Object object = parent.getItemAtPosition(position);
            if (object instanceof Stranger) {
                Stranger stranger = (Stranger) object;
                UILauncher.launchStrangerDetailUI(view.getContext(), stranger);
            }
        }
    };
    private View.OnClickListener mOnclickListener=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (mGroup.getCreateUserId().equals(SettingHelper.getInstance(getActivity()).getAccountUserId())){
                showResentConfirm(R.string.isdismissGroup);
            }else{
                showResentConfirm(R.string.isexitGroup);
            }

        }
    };
    private void showResentConfirm(int message) {
        ConfirmDialogFragment.UiConfig uiConfig
                = new ConfirmDialogFragment.UiConfig(
                getActivity().getString(message),
        null);
        ConfirmDialogFragment.IConfirmListener confirmListener
                = new ResendConfirmListener();
        ConfirmDialogFragment fragment = ConfirmDialogFragment.newInstance(uiConfig,
                confirmListener);
        fragment.show(getActivity().getSupportFragmentManager(), "ResendConfirm");

    }
    private void init(View view) {
        mGroupInformationGridView = (GroupInformationGridView) view
                .findViewById(R.id.group_friend_info_list);
        mExitGroup = (Button) view.findViewById(R.id.exit_group);
        mExitGroup.setOnClickListener(mOnclickListener);
        if (mGroup.getCreateUserId().equals(SettingHelper.getInstance(getActivity()).getAccountUserId())){
            mExitGroup.setText(R.string.dismissGroup);
        }
        mScrollView=(ScrollView)view.findViewById(R.id.scrollView1);
        mProgressBar=(ProgressBar)view.findViewById(R.id.groupinfo_progress);

    }
    private class ResendConfirmListener extends ConfirmDialogFragment.AbsConfirmListener {
        @Override
        public void onConfirm() {
            if (mGroup.getCreateUserId().equals(SettingHelper.getInstance(getActivity()).getAccountUserId())){
                mTmpGroupManager.dismissGroupRequest(mGroupId,null);
            }else{
                mTmpGroupManager.quitGroup(mGroupId);
            }
        }
    }
    private void parseArguments() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mGroupId = bundle.getString(GROUP_INFORMATION);
        }
        mGroup= mTmpGroupManager.queryGroup(mGroupId);

    }
}
