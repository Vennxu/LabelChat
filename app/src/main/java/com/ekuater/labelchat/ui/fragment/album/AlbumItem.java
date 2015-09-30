package com.ekuater.labelchat.ui.fragment.album;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.datastruct.AlbumPhoto;
import com.ekuater.labelchat.delegate.AlbumManager;

/**
 * Created by Leo on 2015/3/21.
 *
 * @author LinYong
 */
public class AlbumItem {

    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_UPLOAD = 1;
    public static final int TYPE_COUNT = 2;

    public static int getTypeCount() {
        return TYPE_COUNT;
    }

    public interface Item {

        public View newView(LayoutInflater inflater, ViewGroup parent);

        public void bindView(View view);

        public int getType();

        public void onClick();
    }

    public interface UploadItemListener {

        public void onClick();
    }

    public static class UploadItem implements Item {

        private final UploadItemListener listener;

        public UploadItem(UploadItemListener listener) {
            this.listener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.album_photo_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            ImageView imageView = (ImageView) view.findViewById(R.id.photo);
            imageView.setImageResource(R.drawable.ic_album_upload_photo);
        }

        @Override
        public int getType() {
            return TYPE_UPLOAD;
        }

        @Override
        public void onClick() {
            if (this.listener != null) {
                this.listener.onClick();
            }
        }
    }

    public interface PhotoItemListener {

        public void onClick(AlbumPhoto photo);
    }

    public static class PhotoItem implements Item {

        private AlbumPhoto photo;
        private AlbumManager manager;
        private PhotoItemListener listener;

        public PhotoItem(AlbumPhoto photo, AlbumManager manager, PhotoItemListener listener) {
            this.photo = photo;
            this.manager = manager;
            this.listener = listener;
        }

        @Override
        public View newView(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.album_photo_item, parent, false);
        }

        @Override
        public void bindView(View view) {
            ImageView imageView = (ImageView) view.findViewById(R.id.photo);
            this.manager.displayPhotoThumb(photo.getPhotoThumb(), imageView,
                    R.drawable.pic_loading);
        }

        @Override
        public int getType() {
            return TYPE_PHOTO;
        }

        @Override
        public void onClick() {
            if (this.listener != null) {
                this.listener.onClick(getPhoto());
            }
        }

        public AlbumPhoto getPhoto() {
            return this.photo;
        }
    }
}
