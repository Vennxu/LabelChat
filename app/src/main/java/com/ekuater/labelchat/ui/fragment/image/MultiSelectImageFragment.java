package com.ekuater.labelchat.ui.fragment.image;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.activity.ImageViewSelectActivity;
import com.ekuater.labelchat.util.L;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LinYong
 */
public class MultiSelectImageFragment extends Fragment {

    private static final String TAG = MultiSelectImageFragment.class.getSimpleName();
    private static final String FUNC_TAKE_PHOTO = "take_photo";
    private static final int REQUEST_TAKE_PHOTO = 1001;
    private static final int REQUEST_SELECT_PHOTO = 1002;

    public static MultiSelectImageFragment newInstance(String title, String menuText, ArrayList<String> imageUrls, int maxSelectCount,
                                                       ImageSelectListener listener) {
        MultiSelectImageFragment instance = new MultiSelectImageFragment();
        instance.mTitle = title;
        instance.mMaxSelectCount = maxSelectCount;
        instance.mMenuText = menuText;
        instance.mListener = listener;
        instance.mImageUrls = imageUrls;
        return instance;
    }

    private String mTitle;
    private int mMaxSelectCount;
    private ImageSelectListener mListener;
    private ProgressBar mProgressBar;
    private Uri mTempPhotoUri;
    private String mMenuText;
    private boolean mLoading = false;
    private ItemAdapter mAdapter;
    private int mPosition;
    private View mView;
    private ArrayList<String> mImageUrls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mMaxSelectCount = Math.max(mMaxSelectCount, 1);
    }

    private TextView title;
    private TextView addBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_multi_select, container, false);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        title = (TextView) view.findViewById(R.id.title);
        addBtn = (TextView) view.findViewById(R.id.right_title);
        addBtn.setVisibility(View.VISIBLE);
        addBtn.setEnabled(false);
        if (mMenuText != null) {
            addBtn.setText(mMenuText);
        } else {
            addBtn.setText(getResources().getString(R.string.labelstory_input_finish));
        }
        addBtn.setTextColor(getResources().getColor(R.color.colorLightDark));
        GridView gridView = (GridView) view.findViewById(R.id.grid);
        updateSelectDoneView(mImageUrls == null ? 0:mImageUrls.size());
        mAdapter = new ItemAdapter(mMaxSelectCount, mImageUrls, new AdapterListener() {
            @Override
            public void onItemToggled(int selectedCount) {
                updateSelectDoneView(selectedCount);
            }
        });

        LoadTask loadTask = new LoadTask(getActivity(), new LoadTask.LoadListener() {
            @Override
            public void onPreLoad() {
                mLoading = true;
                updateProgressBar();
            }

            @Override
            public void onPostLoad(List<ImageItem> items) {
                mAdapter.setItems(addFunctionItems(items));
                mLoading = false;
                updateProgressBar();
            }
        });
        loadTask.load();
        gridView.setAdapter(mAdapter);
        gridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageItem item = mAdapter.getItem(position);

                switch (item.mType) {
                    case ImageItem.TYPE_FUNCTION:
                        if (FUNC_TAKE_PHOTO.equals(item.mExtra)) {
                            onTakePhoto();
                        }
                        break;
                    default:
                        View selectView = view.findViewById(R.id.select);
                        mView = (View) selectView.getTag();
                        previewImage(mAdapter.getItem(position).mImagePath, position);
                        break;
                }
            }
        });
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        updateProgressBar();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMultiSelectDone();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        L.v(TAG, "onActivityResult(), requestCode=%1$d,resultCode=%2$d,data=%3$s",
                requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                onPhotoTaken(resultCode);
                break;
            case REQUEST_SELECT_PHOTO:
                if (data != null) {
                    boolean isSelect = data.getBooleanExtra(ImageViewSelectActivity.IS_CHECK, false);
                    mAdapter.previewItemSelected(mPosition, mView, isSelect);
                }
                break;
            default:
                break;
        }
    }

    private void onMultiSelectDone() {
        ArrayList<String> imageUrl = mAdapter.getImageUrl();
        notifyMultiSelect(imageUrl.toArray(new String[imageUrl.size()]));
    }

    private void updateSelectDoneView(int selectedCount) {
        title.setText(mTitle + getString(R.string.total_select_count,
                selectedCount, mMaxSelectCount));
        addBtn.setEnabled(selectedCount > 0);
        addBtn.setTextColor(selectedCount > 0 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.colorLightDark));
    }

    private void updateProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(mLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void onTakePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTempPhotoUri = Uri.fromFile(EnvConfig.genTempFile("jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempPhotoUri);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void onPhotoTaken(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            notifySingleSelect(mTempPhotoUri.getPath(), true);
        } else {
            notifySelectFailure();
        }
    }

    private void previewImage(String imagePath, int position) {
        try {
            mPosition = position;
            UILauncher.launchImageViewSelectUI(this, REQUEST_SELECT_PHOTO,
                    Uri.fromFile(new File(imagePath)));
        } catch (Exception e) {
            L.w(TAG, e);
        }
    }

    private List<ImageItem> addFunctionItems(List<ImageItem> items) {
        items.add(0, new ImageItem(R.drawable.ic_camera_selector, FUNC_TAKE_PHOTO));
        return items;
    }

    private void notifySelectListener(SelectListenerNotifier notifier) {
        notifier.notify(mListener);
    }

    private void notifySelectFailure() {
        notifySelectListener(new SelectFailedNotifier());
    }

    private void notifyMultiSelect(String[] imagePaths) {
        notifySelectListener(new MultiSelectNotifier(imagePaths));
    }

    private void notifySingleSelect(String imagePath, boolean isTemp) {
        notifySelectListener(new SingleSelectNotifier(imagePath, isTemp));
    }

    private class SelectFailedNotifier implements SelectListenerNotifier {

        @Override
        public void notify(ImageSelectListener listener) {
            if (listener != null) {
                listener.onSelectFailure();
            }
        }
    }

    private class MultiSelectNotifier implements SelectListenerNotifier {

        private final String[] mImagePaths;

        public MultiSelectNotifier(String[] imagePaths) {
            mImagePaths = imagePaths;
        }

        @Override
        public void notify(ImageSelectListener listener) {
            if (listener != null) {
                listener.onMultiSelectSuccess(mImagePaths);
            }
        }
    }

    private class SingleSelectNotifier implements SelectListenerNotifier {

        private final String mImagePath;
        private final boolean mIsTemp;

        public SingleSelectNotifier(String imagePath, boolean isTemp) {
            mImagePath = imagePath;
            mIsTemp = isTemp;
        }

        @Override
        public void notify(ImageSelectListener listener) {
            if (listener != null) {
                listener.onSelectSuccess(mImagePath, mIsTemp);
            }
        }
    }

    private interface AdapterListener {
        public void onItemToggled(int selectedCount);
    }

    private interface AdapterListenerNotifier {
        public void notify(AdapterListener listener);
    }

    private class ItemAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final ThumbnailCache mCache;
        private final AdapterListener mListener;
        private final int mMaxSelectCount;

        private List<ImageItem> mItemList;
        //        private SparseBooleanArray mSelectState;
        private ArrayList<String> mImageUrls;

        private View.OnClickListener mCheckClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View selectView = v.findViewById(R.id.select);
                View view = (View) selectView.getTag();
                int position = (Integer) selectView.getTag(R.id.select);
                toggleItemSelected(position, view);
            }
        };

        public ItemAdapter(int maxSelectCount, ArrayList<String> imageUrls, AdapterListener listener) {
            mInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mCache = ThumbnailCache.getInstance();
            mListener = listener;
            mMaxSelectCount = maxSelectCount;
            mImageUrls = imageUrls == null ? new ArrayList<String>() : imageUrls;
        }

        public void setItems(List<ImageItem> items) {
            mItemList = items;
            notifyDataSetChanged();
        }

        public void toggleItemSelected(int position, View view) {
            final boolean selected = !isSelected(position);
            if (selected && getSelectedCount() >= mMaxSelectCount) {
                return;
            }
            if (selected) {
                mImageUrls.add(mItemList.get(position).mImagePath);
            } else {
                mImageUrls.remove(mItemList.get(position).mImagePath);
            }
            updateViewSelected(view, selected);
            notifyItemToggled();
        }

//        private void removeUrl(String url){
//
//        }

        public void previewItemSelected(int position, View view, boolean isCheck) {
            if (getSelectedCount() >= mMaxSelectCount) {
                return;
            }
            if (isCheck) {
                mImageUrls.add(mItemList.get(position).mImagePath);
            } else {
                mImageUrls.remove(mItemList.get(position).mImagePath);
            }
            updateViewSelected(view, isCheck);
            notifyItemToggled();
        }

        public int getSelectedCount() {
            return (mImageUrls == null) ? 0 : mImageUrls.size();
        }

        public ArrayList<String> getImageUrl() {
            return mImageUrls;
        }
//        public ImageItem[] getSelectedItems() {
//            List<ImageItem> items = new ArrayList<>();
//            int count = getSelectedCount();
//            if (count > 0 && mItemList != null) {
//                for (int i = 0; i < count; i++) {
//                    items.add(mItemList.get(mImageUrls.keyAt(i)));
//                }
//            }
//            return items.toArray(new ImageItem[count]);
//        }

        private void notifyListener(AdapterListenerNotifier notifier) {
            notifier.notify(mListener);
        }

        private void notifyItemToggled() {
            notifyListener(new ItemToggledNotifier(getSelectedCount()));
        }

        private boolean isSelected(int position) {
            for (int i = 0; i < mImageUrls.size(); i++) {
                if (mImageUrls.get(i).equals(mItemList.get(position).mImagePath)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getCount() {
            return (mItemList == null) ? 0 : mItemList.size();
        }

        @Override
        public ImageItem getItem(int position) {
            return (mItemList == null) ? null : mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView(parent);
            }
            bindView(position, convertView);
            return convertView;
        }

        private View newView(ViewGroup parent) {
            View view = mInflater.inflate(R.layout.image_mulit_select_item, parent, false);
            View selectArea = view.findViewById(R.id.select_area);
            selectArea.setOnClickListener(mCheckClickListener);
            return view;
        }

        private void bindView(int position, View view) {
            ImageItem imageItem = getItem(position);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.select);

            switch (imageItem.mType) {
                case ImageItem.TYPE_FUNCTION:
                    imageView.setTag(null);
                    checkBox.setVisibility(View.GONE);
                    imageView.setImageResource(imageItem.mIconId);
                    break;
                default:
                    imageView.setTag(imageItem.mImagePath);
                    checkBox.setVisibility(View.VISIBLE);
                    mCache.loadThumbnail(imageItem.mThumbnailPath, imageItem.mImagePath,
                            new ImageShowCallback(imageView, imageItem.mImagePath));
                    break;
            }

            checkBox.setTag(view);
            checkBox.setTag(R.id.select, position);
            updateViewSelected(view, isSelected(position));
        }

        private void updateViewSelected(View view, boolean selected) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.select);
            View shadowView = view.findViewById(R.id.shadow);
            checkBox.setChecked(selected);
            shadowView.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        private class ImageShowCallback implements ThumbnailCache.LoadCallback {

            private final ImageView mImageView;
            private final String mSourcePath;

            public ImageShowCallback(ImageView imageView, String sourcePath) {
                mImageView = imageView;
                mSourcePath = sourcePath;
            }

            @Override
            public void onThumbnailLoaded(Bitmap thumbnail) {
                if (mImageView != null && mSourcePath.equals(mImageView.getTag())) {
                    if (thumbnail != null) {
                        mImageView.setImageBitmap(thumbnail);
                    } else {
                        mImageView.setImageResource(0);
                    }
                }
            }
        }

        private class ItemToggledNotifier implements AdapterListenerNotifier {

            private final int mSelectedCount;

            public ItemToggledNotifier(int selectedCount) {
                mSelectedCount = selectedCount;
            }

            @Override
            public void notify(AdapterListener listener) {
                if (listener != null) {
                    listener.onItemToggled(mSelectedCount);
                }
            }
        }
    }
}
