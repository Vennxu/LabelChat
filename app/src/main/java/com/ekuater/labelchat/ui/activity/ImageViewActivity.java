package com.ekuater.labelchat.ui.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.L;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageViewActivity extends BackIconActivity {

    private static final String TAG = ImageViewActivity.class.getSimpleName();

    private PhotoView mPhotoView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        mPhotoView = (PhotoView) findViewById(R.id.photo_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        PhotoViewListener listener = new PhotoViewListener();
        mPhotoView.setOnViewTapListener(listener);
        mPhotoView.setOnPhotoTapListener(listener);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Intent intent = getIntent();
        Uri photoUri = intent.getData();

        if (photoUri != null) {
            new LoadTask(metrics.widthPixels, metrics.heightPixels).load(photoUri);
        } else {
            mPhotoView.setImageResource(R.drawable.image_load_fail);
        }
    }

    private class PhotoViewListener implements PhotoViewAttacher.OnViewTapListener,
            PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            finish();
        }

        @Override
        public void onViewTap(View view, float x, float y) {
            finish();
        }
    }

    private class LoadTask extends AsyncTask<Uri, Void, Bitmap> {

        private final int mMaxWidth;
        private final int mMaxHeight;

        public LoadTask(int maxWidth, int maxHeight) {
            mMaxWidth = maxWidth;
            mMaxHeight = maxHeight;
        }

        public void load(Uri imageUri) {
            executeOnExecutor(THREAD_POOL_EXECUTOR, imageUri);
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            final Uri uri = params[0];
            InputStream in = null;
            Bitmap bitmap = null;

            try {
                in = openInputStream(uri);

                if (in != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(in, null, options);
                    in.close();
                    in = null;

                    float realWidth = options.outWidth;
                    float realHeight = options.outHeight;

                    if (realHeight > 0 && realWidth > 0) {
                        int scale = (int) (Math.max(realWidth / mMaxWidth,
                                realHeight / mMaxHeight) + 0.5F);
                        in = openInputStream(uri);
                        options.inSampleSize = (scale >= 1) ? scale : 1;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeStream(in, null, options);
                        in.close();
                        in = null;
                    }
                }
            } catch (Exception e) {
                L.w(TAG, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        L.w(TAG, e);
                    }
                }
            }

            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mProgressBar.setVisibility(View.GONE);
            if (bitmap != null) {
                mPhotoView.setImageBitmap(bitmap);
            } else {
                mPhotoView.setImageResource(R.drawable.image_load_fail);
            }
        }
    }

    private class OpenResourceIdResult {
        public Resources r;
        public int id;
    }

    private OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        Resources r;
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        } else {
            try {
                r = getPackageManager().getResourcesForApplication(authority);
            } catch (PackageManager.NameNotFoundException ex) {
                throw new FileNotFoundException("No package found for authority: " + uri);
            }
        }
        List<String> path = uri.getPathSegments();
        if (path == null) {
            throw new FileNotFoundException("No path: " + uri);
        }
        int len = path.size();
        int id;
        if (len == 1) {
            try {
                id = Integer.parseInt(path.get(0));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
            }
        } else if (len == 2) {
            id = r.getIdentifier(path.get(1), path.get(0), authority);
        } else {
            throw new FileNotFoundException("More than two path segments: " + uri);
        }
        if (id == 0) {
            throw new FileNotFoundException("No resource found for: " + uri);
        }
        OpenResourceIdResult res = new OpenResourceIdResult();
        res.r = r;
        res.id = id;
        return res;
    }

    private InputStream openInputStream(Uri uri) throws Exception {
        final String scheme = uri.getScheme();
        final InputStream in;

        switch (scheme) {
            case ContentResolver.SCHEME_ANDROID_RESOURCE: {
                OpenResourceIdResult r = getResourceId(uri);
                in = r.r.openRawResource(r.id);
                break;
            }
            case ContentResolver.SCHEME_CONTENT:
            case ContentResolver.SCHEME_FILE:
                in = getContentResolver().openInputStream(uri);
                break;
            default:
                in = new FileInputStream(uri.toString());
                break;
        }

        return in;
    }
}
