package com.ekuater.labelchat.ui.fragment.labelstory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.LabelStory;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.ui.UILauncher;
import com.ekuater.labelchat.ui.util.MiscUtils;
import com.ekuater.labelchat.ui.util.ShowToast;
import com.ekuater.labelchat.ui.widget.emoji.EmojiTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Leo on 2015/4/20.
 *
 * @author LinYong
 */
public class TxtStoryRender implements StoryContentRender, View.OnClickListener {

    private Context mContext;
    private EmojiTextView mDetailContent;
    private ImageView mDetailImage;
    private ImageView mView;
    private LinearLayout mImageList;

    private LabelStory mBoundStory;
    private Display display;

    public TxtStoryRender(Context context) {
        mContext = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    @Override
    public void onCreate() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.txt_story_content, container, false);
        mDetailContent = (EmojiTextView) view.findViewById(R.id.descript_content);
        mDetailImage = (ImageView) view.findViewById(R.id.descript_image);
        mImageList = (LinearLayout) view.findViewById(R.id.descript_image_list);
        mDetailImage.setOnClickListener(this);
        mDetailImage.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuInflater inflater = new MenuInflater(mContext);
                inflater.inflate(R.menu.save_picture_menu, menu);
                menu.findItem(R.id.save).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        saveStoryImage();
                        return true;
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void bindContentData(LabelStory story) {
        mBoundStory = story;
        bindStory();
    }

    @Override
    public void onDestroyView() {
    }

    private void bindStory() {
        String content = mBoundStory.getContent();
        if (TextUtils.isEmpty(content)) {
            mDetailContent.setVisibility(View.GONE);
        } else {
            mDetailContent.setVisibility(View.VISIBLE);
            mDetailContent.setText(content);
        }
        loadContentImage();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.descript_image:
                if (mBoundStory != null && mBoundStory.getImages() != null
                        && mBoundStory.getImages().length > 0) {
                    if(mBoundStory.getImages().length > 1) {
                        if (mDetailImage.getTag() != null) {
                            UILauncher.launchImageGalleryUI(mContext, mBoundStory.getImages(), Integer.parseInt(mDetailImage.getTag().toString()));
                        }
                    }else{
                        UILauncher.launchImageGalleryUI(mContext, mBoundStory.getImages(), 0);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void saveStoryImage() {
        Drawable drawable = mDetailImage.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                    bitmap, null, null);
            saveImage(bitmap);
            ShowToast.makeText(mContext, R.drawable.emoji_smile,
                    mContext.getResources().getString(R.string.saved)).show();
        }
    }

    private void saveImage(Bitmap bitmap) {
        File saveFilePath = new File(Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM);
        if (!saveFilePath.exists()) {
            try {
                if (saveFilePath.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(saveFilePath);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadContentImage() {
        String[] images = mBoundStory.getImages();
        if (images != null && images.length > 0) {
            String image = images[0];
            mDetailImage.setVisibility(View.VISIBLE);
            mDetailImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            MiscUtils.showLabelStoryImageThumb(AvatarManager.getInstance(mContext),
                    image, mDetailImage, R.drawable.pic_loading);
            mDetailImage.setTag(0);
            if (images.length > 1) {
                mImageList.setVisibility(View.VISIBLE);
                loadingImageList(images);
            }
        } else {
            mDetailImage.setVisibility(View.GONE);
        }
    }

    private void loadingImageList(String thumbImage[]) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int margin = MiscUtils.dp2px(mContext, 10);
        int weight = (display.getWidth() - (margin * 8)) / 5;
        for (int i = 0; i < thumbImage.length; i++) {
            final int position = i;
            final String imageUrl = thumbImage[position];
            View view = inflater.inflate(R.layout.descript_image_item, mImageList, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_item);
            ImageView imageBg = (ImageView) view.findViewById(R.id.image_bg);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(weight, weight);
            if (i != 0) {
                params.setMargins(margin, 0, 0, 0);
            }else{
                mView = imageBg;
                imageBg.setBackgroundResource(R.drawable.dynamic_imge_bg);
            }
            imageView.setLayoutParams(params);
            imageBg.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MiscUtils.showLabelStoryImageThumb(AvatarManager.getInstance(mContext),
                            imageUrl,mDetailImage,
                            R.drawable.pic_loading);
                    mDetailImage.setTag(position);
                    mView.setBackgroundResource(0);
                    mView = (ImageView) v;
                    mView.setBackgroundResource(R.drawable.dynamic_imge_bg);
                }
            });
            MiscUtils.showLabelStoryImageThumb(AvatarManager.getInstance(mContext),
                    thumbImage[i], imageView,
                    R.drawable.pic_loading);
            mImageList.addView(view);
        }
    }
}
