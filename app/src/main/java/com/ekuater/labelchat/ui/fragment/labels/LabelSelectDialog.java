package com.ekuater.labelchat.ui.fragment.labels;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.labelstory.LabelStoryCategoryCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.fragment.labelstory.LabelStoryCategoryFragment;
import com.ekuater.labelchat.ui.util.FileUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.FlowLayout;

import org.json.JSONException;


/**
 * @author LinYong
 */
public class LabelSelectDialog extends DialogFragment
        implements AdapterView.OnItemClickListener {

    private static final String TAG = LabelSelectDialog.class.getSimpleName();

    public interface IListener {
        public void onItemSelected(int position, LabelStoryCategory catetory);
        public void onCancelSelected();
    }

    public static final class UiConfig {

        public String title;
        public LabelStoryCategory[] textItems;
        public int height;
        public IListener listener;

        public UiConfig() {
            title = null;
            textItems = null;
            height = -1;
            listener = null;
        }
    }


    public static LabelSelectDialog newInstance(UiConfig config) {
        LabelSelectDialog instance = new LabelSelectDialog();
        instance.applyConfig(config);
        return instance;
    }
    private static final int REQUEST_CATEGORY_SUCCESE = 101;
    private static final int REQUEST_CATEGORY_FAILED = 102;
    private String mTitle;
    private LabelStoryCategory[] mTextItems;
    private IListener mListener;
    private int mHeight;
    private LabelStoryManager mLabelStoryManager;
    private ProgressBar mProgressBar;
    private FlowLayout flowLayout;
    private Activity activity;

    public LabelSelectDialog() {
        setStyle(STYLE_NO_TITLE, 0);
    }

    public void applyConfig(UiConfig config) {
        mTitle = config.title;
        mHeight = config.height;
        mListener = config.listener;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            postHandler(msg);
        }
    };
    private void postHandler(Message message){

        switch (message.what){
            case REQUEST_CATEGORY_SUCCESE:
                stopAnimotion();
                keepCategory(message.obj.toString());
                LabelStoryCategory[] catetory = (LabelStoryCategory[])message.obj;
                if (catetory != null && catetory.length > 0){
                    mTextItems = catetory;
                    initDate();
                }
                break;
            case REQUEST_CATEGORY_FAILED:
                stopAnimotion();
                if (activity == null){
                    return;
                }
                ShowToast.makeText(activity, R.drawable.emoji_cry, getString(R.string.request_failure)).show();
                break;
            default:
                break;
        }
    }
    public void keepCategory(String fileString){
        String category = FileUtils.readFileData(LabelStoryCategoryFragment.CATEGORY_TXT,activity);
        if (!TextUtils.isEmpty(category)){
            FileUtils.deletFileData(LabelStoryCategoryFragment.CATEGORY_TXT);
        }
        FileUtils.writeFileData(LabelStoryCategoryFragment.CATEGORY_TXT,fileString,activity);
    }
    public void getTextCatetory()  {
        String str= FileUtils.readFileData(LabelStoryCategoryFragment.CATEGORY_TXT, getActivity());
        if ( str != null && !" ".equals( str ) ){
            Log.d("str", str);
            try{
                LabelStoryCategoryCommand.CommandResponse commandResponse=new LabelStoryCategoryCommand.CommandResponse(str);
                mTextItems = commandResponse.getCatetory();
                initDate();
            } catch (JSONException e) {
                getCategory();
                return;
            }
        }else{
            getCategory();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mLabelStoryManager = LabelStoryManager.getInstance(activity);
    }

    private void initDate() {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        if (mTextItems != null) {
                for (int i = 0; i < mTextItems.length; i++) {
                    final int position = i;
                    final LabelStoryCategory labelStoryCategory = mTextItems[position];
                    TextView name = (TextView) layoutInflater.inflate(R.layout.select_item, flowLayout, false);
                    name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismiss();
                            mListener.onItemSelected(position,labelStoryCategory);
                        }
                    });
                    name.setBackgroundResource(R.drawable.ic_label_other_normal);
                    name.setText(labelStoryCategory.getmCategoryName());
                    flowLayout.addView(name);
                }
            }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_select_dialog, container, false);
        TextView titleView = (TextView) view.findViewById(R.id.title_header);
        TextView titleCancel = (TextView) view.findViewById(R.id.title_cancel);
        mProgressBar = (ProgressBar) view.findViewById(R.id.select_progressbar);
        titleView.setText(mTitle);
        flowLayout = (FlowLayout) view.findViewById(R.id.select_content);
        flowLayout.setHorizontalGap(20);
        flowLayout.setVerticalGap(20);
        getTextCatetory();
        titleCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancelSelected();
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mHeight > 0) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.height = mHeight;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            mListener.onItemSelected(position, mTextItems[position]);
        }
        dismiss();
    }
    private void getCategory(){
        startAnimotion();
        LabelStoryManager.LabelStoryCategoryQueryObserver observer = new LabelStoryManager.LabelStoryCategoryQueryObserver() {
            @Override
            public void onQueryResult(int result, LabelStoryCategory[] catetories, boolean remaining) {
                if (result == ConstantCode.EXECUTE_RESULT_SUCCESS){
                    Message message = Message.obtain(handler,REQUEST_CATEGORY_SUCCESE,catetories);
                    handler.sendMessage(message);
                }else{
                    Message message = Message.obtain(handler,REQUEST_CATEGORY_FAILED,catetories);
                    handler.sendMessage(message);
                }
            }
        };
        mLabelStoryManager.categoryListLabelStory(observer);
    }

    private void startAnimotion(){
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void stopAnimotion(){
        mProgressBar.setVisibility(View.GONE);
    }

    private class ItemAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private int mItemLayout;

        public ItemAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mItemLayout = R.layout.select_label_text_center_item;

        }

        @Override
        public int getCount() {
            return mTextItems != null ? mTextItems.length : 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = newView(parent);
            }
            bindView(position, view);
            return view;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(mItemLayout, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.text);
            holder.iconView = (ImageView) view.findViewById(R.id.icon);

            view.setTag(holder);
            return view;
        }

        private void bindView(int position, View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.textView.setText(mTextItems[position].getmCategoryName());

        }

        private class ViewHolder {
            public TextView textView;
            public ImageView iconView;
        }
    }
}
