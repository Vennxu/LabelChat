package com.ekuater.labelchat.ui.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.contact.NewUserCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.Stranger;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ContactsManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.fragment.labelstory.NoScrollListview;
import com.ekuater.labelchat.ui.util.FileUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.ui.widget.CircleImageView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenxiang on 2015/3/3.
 */
public class LookFrendsFragment extends Fragment {

    private static final String LOOK_LABEL_SEARCH_FRIEND = "label_search_friend";
    private static final String LOOK_FRIEND_AROUND = "friend_around";
    public static final String FILE_NEW_USER_NAME = "file_new_user_name.txt";
    private static final int HANDLER_NEW_USER=101;
    private static final int MSG_HANDLE_EXACT_SEARCH_RESULT = 102;

    private NoScrollListview mListView;
    private GridView mNewUserList;
    private EditText mSearchEditText;
    private ProgressBar mProgressBar,mSearchProgress;
    private TextView mPostButton;

    private LookFriendsAdapter mLookFriendsAdapter;
    private NewUserAdapter mNewUserAdapter;
    private ContactsManager mContactsManager;
    private AvatarManager mAvatarManager;
    private Activity mActivity;
    private int mLabelCodeMinLength;
    private int mLabelCodeMaxLength;
    private int mMobileLength;


    private final DiscoverListItem.FunctionItemListener mFunctionItemListener
            = new DiscoverListItem.FunctionItemListener() {
        @Override
        public void onClick(String function) {
            launchFunctionItemUI(function);
        }
    };

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleResult(msg);
        }
    };
    private void handleResult(Message msg){
        switch (msg.what){
            case HANDLER_NEW_USER:
                newUserHandler(msg.arg1,msg.obj);
                break;
            case MSG_HANDLE_EXACT_SEARCH_RESULT:
                handleExactSearchResult(msg.arg1, (Stranger) msg.obj);
                break;
            default:
                break;
        }
    }
    private void handleExactSearchResult(int result, Stranger stranger) {
        final Activity activity = getActivity();

//        L.v(TAG, "handleExactSearchResult(), result=" + result);

//        stopSearchAnimation();
        mSearchProgress.setVisibility(View.GONE);
        mPostButton.setVisibility(View.VISIBLE);
        switch (result) {
            case ContactsManager.QUERY_RESULT_SUCCESS:
                if (stranger != null) {
                    if (mContactsManager.getUserContactByUserId(stranger.getUserId()) != null) {
                        UILauncher.launchFriendDetailUI(activity, stranger.getUserId());
                        mActivity.finish();
                    } else {
                        UILauncher.launchStrangerDetailUI(activity, stranger);
                    }
                } else {
                    ShowToast.makeText(activity, R.drawable.emoji_sad, getString(R.string.number_search_no_user)).show();
                }
                break;
            case ContactsManager.QUERY_RESULT_ILLEGAL_ARGUMENTS:
                ShowToast.makeText(activity, R.drawable.emoji_smile, getString(R.string.input_mobile_or_label_code_prompt,
                        mMobileLength, mLabelCodeMinLength)).show();

                break;
            default:
                ShowToast.makeText(activity, R.drawable.emoji_sad, getString(R.string.number_search_no_user)).show();

                break;
        }
    }
    private void newUserHandler(int result,Object object){
        if (result== ConstantCode.EXECUTE_RESULT_SUCCESS) {
            mProgressBar.setVisibility(View.GONE);
            mNewUserList.setVisibility(View.VISIBLE);
            Stranger[] strangers = (Stranger[]) object;
            mNewUserAdapter.updateNewUser(strangers);
        }
    }
    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            final String text = editable.toString();
            final int length = text.length();
            int deleteCount;
            boolean searchEnable;

            if (text.startsWith("1")) {  // if mobile number search
                deleteCount = length - mMobileLength;
                searchEnable = (length >= mMobileLength);
            } else {  // else label code search
                deleteCount = length - mLabelCodeMaxLength;
                searchEnable = (length >= mLabelCodeMinLength);
            }

            if (deleteCount > 0) {
                int start = mSearchEditText.getSelectionStart() - deleteCount;
                int end = mSearchEditText.getSelectionEnd();
                mSearchEditText.removeTextChangedListener(this);
                editable.delete(start, end);
                mSearchEditText.setSelection(start);
                mSearchEditText.addTextChangedListener(this);
            }

            mPostButton.setEnabled(searchEnable);
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity=getActivity();
        final Resources res = mActivity.getResources();
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.hide();
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
        mContactsManager=ContactsManager.getInstance(getActivity());
        mAvatarManager=AvatarManager.getInstance(getActivity());
        mLookFriendsAdapter=new LookFriendsAdapter(getActivity());
        mNewUserAdapter = new NewUserAdapter();
        mLookFriendsAdapter.updateItems(newMethodItemList());
        mMobileLength = res.getInteger(R.integer.mobile_length);
        mLabelCodeMinLength = res.getInteger(R.integer.label_code_min_length);
        mLabelCodeMaxLength = res.getInteger(R.integer.label_code_max_length);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_look_frends, container, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        title.setText(R.string.function_look_frends);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mListView = (NoScrollListview) view.findViewById(R.id.look_frends_list);
        mSearchEditText = (EditText) view.findViewById(R.id.contact_edit);
        mNewUserList = (GridView) view.findViewById(R.id.new_user_list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.new_user_progress);
        mPostButton = (TextView) view.findViewById(R.id.contact_conform_post);
        mSearchProgress = (ProgressBar) view.findViewById(R.id.contact_progress);
        mPostButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mSearchProgress.setVisibility(View.VISIBLE);
                mPostButton.setVisibility(View.GONE);
                onExactSearch(mSearchEditText.getText().toString());
            }
        });
        mSearchEditText.addTextChangedListener(mTextWatcher);
        mNewUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UILauncher.launchStrangerDetailUI(view.getContext(), mNewUserAdapter.getItem(i));
            }
        });
        mNewUserList.setSelector(new ColorDrawable(android.R.color.transparent));
        mListView.setOnItemClickListener(mLookFriendsAdapter);
        mListView.setAdapter(mLookFriendsAdapter);
        mNewUserList.setAdapter(mNewUserAdapter);
        if (getTextStranger()!=null){
            mProgressBar.setVisibility(View.GONE);
            mNewUserList.setVisibility(View.VISIBLE);
            mNewUserAdapter.updateNewUser(getTextStranger());
        }else{
            mProgressBar.setVisibility(View.VISIBLE);
            mNewUserList.setVisibility(View.GONE);
        }
        getNewUser();
        return view;
    }

    private void getNewUser(){
        ContactsManager.NewUserObserver observer = new ContactsManager.NewUserObserver() {
            @Override
            public void onQueryResult(int result, Stranger[] strangers, boolean remaining) {
                Message message = Message.obtain(handler,HANDLER_NEW_USER,result,0,strangers);
                handler.sendMessage(message);
            }
        };
        mContactsManager.queryNewUser(getActivity(),observer);
    }

    private List<DiscoverListItem.Item> newMethodItemList() {
        List<DiscoverListItem.Item> items = new ArrayList<DiscoverListItem.Item>();
        items.add(new DiscoverListItem.FunctionItem(
                getString(R.string.exact_search),
                R.drawable.ic_label_search_friend,
                LOOK_LABEL_SEARCH_FRIEND,
                mFunctionItemListener));
        items.add(new DiscoverListItem.FunctionItem(
                getString(R.string.people_around),
                R.drawable.ic_people_around,
                LOOK_FRIEND_AROUND,
                mFunctionItemListener));
        return items;
    }
    public Stranger[] getTextStranger()  {
        Stranger[] strangers=null;
        String str= FileUtils.readFileData(FILE_NEW_USER_NAME,mActivity);
        if ( str != null && !" ".equals( str ) ){
            Log.d("str",str);
            try{
                NewUserCommand.CommandResponse commandResponse=new NewUserCommand.CommandResponse(str);
                strangers=commandResponse.getNewUsers();
            } catch (JSONException e) {
                return null;
            }

        }
        return strangers;
    }
    private void launchFunctionItemUI(String function) {
        if (LOOK_FRIEND_AROUND.equals(function)) {
            UILauncher.launchPeopleAroundUI(getActivity());
        } else if (LOOK_LABEL_SEARCH_FRIEND.equals(function)) {
            UILauncher.launchExactSearchFriendUI(getActivity());
        }
    }
    private static class LookFriendsAdapter extends BaseAdapter
            implements AdapterView.OnItemClickListener {

        private final LayoutInflater mInflater;
        private List<DiscoverListItem.Item> mItemList;

        public LookFriendsAdapter(Context context) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mItemList = new ArrayList<DiscoverListItem.Item>();
        }

        public synchronized void updateItems(List<DiscoverListItem.Item> items) {
            if (items != null) {
                mItemList = items;
            } else {
                mItemList.clear();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public DiscoverListItem.Item getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return DiscoverListItem.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getViewType();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DiscoverListItem.Item item = getItem(position);

            if (view == null) {
                view = item.newView(mInflater, parent);
            }
            item.bindView(view);

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            getItem(position).onClick();
        }
    }

    public class NewUserAdapter extends BaseAdapter{

        private Stranger[] mStrangers=null;
        private LayoutInflater inflater;

        public NewUserAdapter(){
            inflater = LayoutInflater.from(mActivity);
        }

        public void updateNewUser(Stranger[] strangers){
            if (mStrangers==null) {
                mStrangers = strangers;
            }else{
                mStrangers=null;
                mStrangers=strangers;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mStrangers==null?0:mStrangers.length;
        }

        @Override
        public Stranger getItem(int position) {
            return mStrangers[position];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view==null){
                view=inflater.inflate(R.layout.look_frends_item_new,viewGroup,false);
            }
           Stranger stranger=getItem(i);
           CircleImageView imageView= (CircleImageView) ViewHolder.get(view,R.id.item_tx);
           TextView textView= (TextView) ViewHolder.get(view,R.id.item_name);

            MiscUtils.showAvatarThumb(mAvatarManager,stranger.getAvatarThumb(),imageView,R.drawable.contact_single);
            if (stranger.getNickname().length() > 0 && stranger.getNickname() != null) {
                textView.setText(stranger.getNickname());
            } else {
                textView.setText(stranger.getLabelCode());
            }
            textView.setText(stranger.getNickname());
            return view;
        }
    }
    private void onExactSearch(String searchWord) {
//        L.v(TAG, "onExactSearch(), searchWord=" + searchWord);

//        startSearchAnimation();
        mContactsManager.exactSearchUser(searchWord,
                new ContactsManager.UserQueryObserver() {
                    @Override
                    public void onQueryResult(int result, Stranger stranger) {
//                        L.v(TAG, "onQueryResult(), result=" + result);
                        Message message = handler.obtainMessage(MSG_HANDLE_EXACT_SEARCH_RESULT,
                                result, 0, stranger);
                        handler.sendMessage(message);
                    }
                });
    }
}
