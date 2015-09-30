package com.ekuater.labelchat.ui.fragment.labelstory;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.command.labelstory.LabelStoryCategoryCommand;
import com.ekuater.labelchat.datastruct.ConstantCode;
import com.ekuater.labelchat.datastruct.LabelStoryCategory;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.LabelStoryManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.FileUtils;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.util.ViewHolder;
import com.ekuater.labelchat.util.BmpUtils;

import org.json.JSONException;

/**
 * Created by Label on 2015/3/11.
 */
public class LabelStoryCategoryFragment extends Fragment {
    private static final int REQUEST_CATEGORY_SUCCESE = 101;
    private static final int REQUEST_CATEGORY_FAILED = 102;
    public static final String CATEGORY_TXT = "category_txt";

    private GridView mCategoryList;
    private ImageView mImageLoading;

    private LabelStoryManager mLabelStoryManager;
    private AvatarManager mAvatarManager;
    private CategoryAdapter mCategoryAdapter;
    private Activity activity;
    private boolean isLoading;
    private Display display;
    private int columnWidth;

    private int getImages(int index){
        int imageIds;
        switch (index){
            case 1:
                imageIds = R.drawable.ic_autodyne;
                break;
            case 2:
                imageIds = R.drawable.ic_alone;
                break;
            case 3:
                imageIds = R.drawable.ic_cooking;
                break;
            case 4:
                imageIds = R.drawable.ic_my_story;
                break;
            case 5:
                imageIds = R.drawable.ic_staring;
                break;
            case 6:
                imageIds = R.drawable.ic_travel;
                break;
            case 7:
                imageIds = R.drawable.ic_music;
                break;
            case 8:
                imageIds = R.drawable.ic_movie;
                break;
            case 9:
                imageIds = R.drawable.ic_reading_book;
                break;
            case 10:
                imageIds = R.drawable.ic_other;
                break;
            default:
                imageIds = R.drawable.get;
                break;
        }
        return imageIds;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            postHandler(msg);
        }
    };
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LabelStoryCategory category = (LabelStoryCategory) parent.getAdapter().getItem(position);
            if (category != null) {
                UILauncher.launchFragmentLabelStoryUI(getActivity(), category, null);
            }
        }
    };
    private void postHandler(Message message){

        switch (message.what){
            case REQUEST_CATEGORY_SUCCESE:
                stopAnimotion();
                keepCategory(message.obj.toString());
                LabelStoryCategory[] catetory = (LabelStoryCategory[])message.obj;
                if (catetory != null && catetory.length > 0){
                    mCategoryAdapter.updateCategoryes(catetory);
                }
                break;
            case REQUEST_CATEGORY_FAILED:
                stopAnimotion();
                if (activity == null){
                    return;
                }
                ShowToast.makeText(activity, R.drawable.emoji_cry,
                        activity.getResources().getString(R.string.request_failure)).show();
                break;
            default:
                break;
        }
    }
    public void keepCategory(String fileString){
       String category = FileUtils.readFileData(CATEGORY_TXT,activity);
        if (!TextUtils.isEmpty(category)){
            FileUtils.deletFileData(CATEGORY_TXT);
        }
        FileUtils.writeFileData(CATEGORY_TXT,fileString,activity);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mLabelStoryManager = LabelStoryManager.getInstance(activity);
        mAvatarManager = AvatarManager.getInstance(activity);
        mCategoryAdapter = new CategoryAdapter();
        isLoading = true;
        display = activity.getWindowManager().getDefaultDisplay();
    }
    public void getTextCatetory()  {
        LabelStoryCategory[] catetory=null;
        String str= FileUtils.readFileData(CATEGORY_TXT,activity);
        if ( str != null && !" ".equals( str ) ){
            Log.d("str",str);
            try{
                LabelStoryCategoryCommand.CommandResponse commandResponse=new LabelStoryCategoryCommand.CommandResponse(str);
                catetory = commandResponse.getCatetory();
                mCategoryAdapter.updateCategoryes(catetory);
            } catch (JSONException e) {
                getCategory();
                return;
            }
        }else{
            getCategory();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category,container,false);
        mCategoryList = (GridView)view.findViewById(R.id.label_story_category_list);
        columnWidth = (display.getWidth()- BmpUtils.dp2px(activity,30))/2;
        mCategoryList.setColumnWidth(columnWidth);
        mImageLoading = (ImageView)view.findViewById(R.id.label_story_category_list_loading);
        mCategoryList.setAdapter(mCategoryAdapter);
        mCategoryList.setOnItemClickListener(onItemClickListener);
        if (isLoading) {
            isLoading = false;
            getTextCatetory();
        }
        return view;
    }

    private void startAnimotion(){
        mCategoryList.setVisibility(View.GONE);
        mImageLoading.setVisibility(View.VISIBLE);
        Drawable drawable = mImageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.start();
    }

    private void stopAnimotion(){
        mCategoryList.setVisibility(View.VISIBLE);
        mImageLoading.setVisibility(View.GONE);
        Drawable drawable = mImageLoading.getDrawable();
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        animationDrawable.stop();
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

    private class CategoryAdapter extends BaseAdapter{

        private LayoutInflater inflater;
        private LabelStoryCategory[] catetories;
        public CategoryAdapter(){
            inflater = LayoutInflater.from(activity);
        }

        public void updateCategoryes(LabelStoryCategory[] catetorie){
            if (catetories == null){
                catetories = catetorie;
            }else{
                catetories = null;
                catetories = catetorie;
            }
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return catetories == null ? 0 : catetories.length;
        }

        @Override
        public LabelStoryCategory getItem(int position) {
            return catetories[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = inflater.inflate(R.layout.category_grid_item,parent,false);
            }
            TextView textView = (TextView) ViewHolder.get(convertView,R.id.category_item_text);
            RoundImageView imageView = (RoundImageView) ViewHolder.get(convertView,R.id.category_item_image);
            TextView textView_total = (TextView) ViewHolder.get(convertView, R.id.category_item_text_total);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(columnWidth,columnWidth/2);
            layoutParams.setMargins(0,20,0,0);
            imageView.setLayoutParams(layoutParams);
            MiscUtils.showCategoryAvatarThumb(mAvatarManager,getItem(position).getmImageUrl(),imageView,R.drawable.image_load_fail);
            textView.setText(getItem(position).getmCategoryName());
            textView_total.setText(getItem(position).getmDynamicTotal()+"");
            return convertView;
        }
    }
}
