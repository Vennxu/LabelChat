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
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ekuater.labelchat.EnvConfig;
import com.ekuater.labelchat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author FanChong
 */
public class SelectThrowPhotoFragment extends Fragment {

    private static final String TAG = SelectThrowPhotoFragment.class.getSimpleName();

    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final String FUNC_TAKE_PHOTO = "func_take_photo";


    public static SelectThrowPhotoFragment newInstance(String title, int maxSelectCount,
                                                       ImageSelectListener listener) {
        SelectThrowPhotoFragment instance = new SelectThrowPhotoFragment();
        instance.mTitle = title;
        instance.mMaxSelectCount = maxSelectCount;
        instance.mListener = listener;
        return instance;
    }

    private Gallery mGallery;
    private GridView mGridView;
    private TextView mTextView;
    private ThumbnailPhotoAdapter mThumbnailPhotoAdapter;
    private PhotoAdapters mPhotoAdapters;
    private View mSelectDoneBtn;

    private String mTitle;
    private ImageSelectListener mListener;
    private Uri mTempPhotoUri;
    private int mMaxSelectCount;
    private int mPosition = 0;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    private View.OnClickListener mDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onMultiSelectDone();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();

        if (actionBar != null) {
            if (TextUtils.isEmpty(mTitle)) {
                mTitle = getString(R.string.exact_select_image);
            }
            actionBar.setTitle(mTitle);

            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            View view = LayoutInflater.from(activity).inflate(
                    R.layout.throw_select_image_done, null);

            mSelectDoneBtn = view.findViewById(R.id.done);
            mSelectDoneBtn.setOnClickListener(mDoneClickListener);

            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            actionBar.setCustomView(view, lp);

        }

        mMaxSelectCount = Math.max(mMaxSelectCount, 1);
        mThumbnailPhotoAdapter = new ThumbnailPhotoAdapter(activity, mMaxSelectCount,
                new SelectListener() {

                    @Override
                    public void onImageSelected(String imagePath, boolean selected) {
                        handleImageSelected(imagePath, selected);

                    }
                });
        mPhotoAdapters = new PhotoAdapters(activity, new AdapterListener() {
            @Override
            public void onImageRemoved(String path) {
                handleImageRemoved(path);
            }
        });
        LoadTask loadTask = new LoadTask(getActivity(), new LoadTask.LoadListener() {
            @Override
            public void onPreLoad() {

            }

            @Override
            public void onPostLoad(List<ImageItem> items) {
                mThumbnailPhotoAdapter.setItems(addFunctionItems(items
                ));
            }
        });
        loadTask.load();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_throw_photo_list, container, false);
        initView(view);
        selectDoneView(0, 0);
        mGallery.setAdapter(mPhotoAdapters);
        mGridView.setAdapter(mThumbnailPhotoAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageItem item = mThumbnailPhotoAdapter.getItem(position);
                switch (item.mType) {
                    case ImageItem.TYPE_FUNCTION:
                        if (FUNC_TAKE_PHOTO.equals(item.mExtra)) {
                            onTakePhoto();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        mGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectDoneView(position, parent.getCount());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;

    }

    private void initView(View view) {
        mGallery = (Gallery) view.findViewById(R.id.photo_preview);
        mGridView = (GridView) view.findViewById(R.id.photo_list);
        mTextView = (TextView) view.findViewById(R.id.image_position);
    }

    private void handleImageSelected(String imagePath, boolean selected) {
        if (selected) {
            mPhotoAdapters.addImage(imagePath);
        } else {
            mPhotoAdapters.removeImage(imagePath);
        }
        mGallery.setSelection(mPhotoAdapters.getCount()-1);
        selectDoneView(mGallery.getSelectedItemPosition(), mPhotoAdapters.getCount());
    }

    private void handleImageRemoved(String path) {
        mThumbnailPhotoAdapter.unSelectImage(path);
        selectDoneView(mGallery.getSelectedItemPosition(), mPhotoAdapters.getCount());
    }

    private void onMultiSelectDone() {
        ImageItem[] items = mThumbnailPhotoAdapter.getSelectedItems();
        String[] paths = new String[items.length];

        for (int i = 0; i < items.length; ++i) {
            paths[i] = items[i].mImagePath;
        }
        notifyMultiSelect(paths);
    }


    private void selectDoneView(int position, int selectedCount) {
        mTextView.setText((selectedCount > 0 ? position + 1 : selectedCount) + "/" + selectedCount);
        mSelectDoneBtn.setEnabled(selectedCount > 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                onPhotoTaken(resultCode);
                break;
            default:
                break;
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

    private List<ImageItem> addFunctionItems(List<ImageItem> items) {
        items.add(0, new ImageItem(R.drawable.ic_camera_selector, FUNC_TAKE_PHOTO));
        return items;
    }

    private void notifySelectListener(com.ekuater.labelchat.ui.fragment.image.SelectListenerNotifier notifier) {
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

    private class SelectFailedNotifier implements com.ekuater.labelchat.ui.fragment.image.SelectListenerNotifier {

        @Override
        public void notify(ImageSelectListener listener) {
            if (listener != null) {
                listener.onSelectFailure();
            }
        }
    }

    private class MultiSelectNotifier implements com.ekuater.labelchat.ui.fragment.image.SelectListenerNotifier {

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

    private class SingleSelectNotifier implements com.ekuater.labelchat.ui.fragment.image.SelectListenerNotifier {

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
        public void onImageRemoved(String path);
    }

    private interface AdapterListenerNotifier {
        public void notify(AdapterListener listener);
    }

    private interface SelectListener {
        public void onImageSelected(String imagePath, boolean selected);
    }

    private interface SelectListenerNotifier {
        public void notify(SelectListener listener);
    }

    private static class ThumbnailPhotoAdapter extends BaseAdapter {

        private List<ImageItem> mItemList;
        private LayoutInflater mInflater;
        private SelectListener mListener;
        private ThumbnailCache mCache;
        private int mMaxSelectCount;
        private SparseBooleanArray mSelectState;

        public ThumbnailPhotoAdapter(Context context, int maxSelectCount, SelectListener listener) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mCache = ThumbnailCache.getInstance();
            mListener = listener;
            mMaxSelectCount = maxSelectCount;
        }

        private View.OnClickListener mCheckClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View selectView = v.findViewById(R.id.select);
                View view = (View) selectView.getTag();
                int position = (Integer) selectView.getTag(R.id.select);
                toggleItemSelected(position, view);
            }
        };

        public void setItems(List<ImageItem> items) {
            mItemList = items;
            mSelectState = new SparseBooleanArray();
            notifyDataSetChanged();
        }

        public int getSelectedCount() {
            return (mSelectState == null) ? 0 : mSelectState.size();
        }

        public void unSelectImage(String path) {
            if (!TextUtils.isEmpty(path)) {
                for (int i = 0; i < mItemList.size(); ++i) {
                    if (path.equals(mItemList.get(i).mImagePath)) {
                        mSelectState.delete(i);
                        notifyDataSetChanged();
                        break;
                    }
                }
            }
        }

        private void toggleItemSelected(int position, View view) {
            boolean selected = !isSelected(position);

            if (selected && getSelectedCount() >= mMaxSelectCount) {
                return;
            }

            if (selected) {
                mSelectState.put(position, true);
            } else {
                mSelectState.delete(position);
            }
            updateViewSelected(view, selected);
            notifyItemToggled(position, selected);
        }

        public ImageItem[] getSelectedItems() {
            List<ImageItem> items = new ArrayList<ImageItem>();
            int count = getSelectedCount();

            if (count > 0 && mItemList != null) {
                for (int i = 0; i < count; i++) {
                    items.add(mItemList.get(mSelectState.keyAt(i)));
                }
            }

            return items.toArray(new ImageItem[count]);
        }

        public boolean isSelected(int position) {
            return (mSelectState != null) && mSelectState.get(position, false);
        }

        private void notifyListener(SelectListenerNotifier notifier) {
            notifier.notify(mListener);
        }

        private void notifyItemToggled(int position, boolean selected) {
            ImageItem imageItem = getItem(position);
            notifyListener(new ImageSelectedNotifier(imageItem.mImagePath, selected));
        }

        private void updateViewSelected(View view, boolean selected) {
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.select);
            View shadowView = view.findViewById(R.id.shadow);
            checkBox.setChecked(selected);
            shadowView.setVisibility(selected ? View.VISIBLE : View.GONE);
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
            View view = mInflater.inflate(R.layout.fragment_throw_photo_item, parent, false);
            View selectArea = view.findViewById(R.id.select_area);
            selectArea.setOnClickListener(mCheckClickListener);
            return view;
        }

        private void bindView(int position, View view) {
            ImageItem imageItem = getItem(position);
            ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail_image);
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

        private static class ImageSelectedNotifier implements SelectListenerNotifier {

            private String mImagePath;
            private boolean mSelected;

            public ImageSelectedNotifier(String imagePath, boolean selected) {
                mImagePath = imagePath;
                mSelected = selected;
            }

            @Override
            public void notify(SelectListener listener) {
                listener.onImageSelected(mImagePath, mSelected);
            }
        }
    }

    private static class ImageShowCallback implements ThumbnailCache.LoadCallback {

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

    private static class PhotoAdapters extends BaseAdapter {

        private List<ImageItem> mItemList;
        private LayoutInflater mInflater;
        private AdapterListener mListener;
        private ThrowPhotoThumbnailCache mCache;

        public PhotoAdapters(Context context, AdapterListener listener) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mCache = ThrowPhotoThumbnailCache.getInstance();
            mItemList = new ArrayList<ImageItem>();
            mListener = listener;
        }

        public void addImage(String path) {
            mItemList.add(new ImageItem(null, path));
            notifyDataSetChanged();
        }

        public void removeImage(String imagePath) {
            ImageItem tmpItem = null;
            for (ImageItem image : mItemList) {
                if (image.mImagePath.equals(imagePath)) {
                    tmpItem = image;
                    break;
                }
            }
            remove(tmpItem);
        }

        private void remove(ImageItem item) {
            mItemList.remove(item);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mItemList != null) ? mItemList.size() : 0;
        }

        @Override
        public ImageItem getItem(int position) {
            return (mItemList != null) ? mItemList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void notifyListener(AdapterListenerNotifier notifier) {
            notifier.notify(mListener);
        }

        private void notifyItemRemoved(ImageItem item) {
            notifyListener(new ImageRemovedNotifier(item.mImagePath));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            ImageItem imageItem = getItem(position);

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_throw_photo_item2, parent, false);
                holder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail_image);
                holder.closeView = (ImageView) convertView.findViewById(R.id.close_avatar_image);
                holder.closeView.setTag(holder);
                holder.closeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object object = v.getTag();
                        if (object instanceof ViewHolder) {
                            ViewHolder holder = (ViewHolder) object;
                            remove(holder.imageItem);
                            notifyItemRemoved(holder.imageItem);
                        }
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.imageItem = imageItem;
            mCache.loadThumbnail(imageItem.mThumbnailPath, imageItem.mImagePath,
                    new PreViewImageShowCallback(holder.imageView, imageItem.mImagePath));
            return convertView;
        }

        private static class ViewHolder {

            public ImageView imageView;
            public ImageView closeView;
            public ImageItem imageItem;
        }

        private static class ImageRemovedNotifier implements AdapterListenerNotifier {

            private final String mImagePath;

            public ImageRemovedNotifier(String imagePath) {
                mImagePath = imagePath;
            }

            @Override
            public void notify(AdapterListener listener) {
                if (listener != null) {
                    listener.onImageRemoved(mImagePath);
                }
            }
        }
    }

    private static class PreViewImageShowCallback implements ThrowPhotoThumbnailCache.LoadCallback {

        private final ImageView mImageView;
        private final String mSourcePath;

        public PreViewImageShowCallback(ImageView imageView, String sourcePath) {
            mImageView = imageView;
            mSourcePath = sourcePath;
            mImageView.setTag(sourcePath);
        }

        @Override
        public void onThumbnailLoaded(Bitmap thumbnail) {
            if (mSourcePath.equals(mImageView.getTag())) {
                if (thumbnail != null) {
                    mImageView.setImageBitmap(thumbnail);
                } else {
                    mImageView.setImageResource(0);
                }
            }
        }
    }
}
