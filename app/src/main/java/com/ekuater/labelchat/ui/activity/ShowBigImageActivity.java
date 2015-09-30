package com.ekuater.labelchat.ui.activity;


import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.ui.activity.base.BackIconActivity;
import com.ekuater.labelchat.util.L;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


public class ShowBigImageActivity extends BackIconActivity implements View.OnTouchListener,View.OnClickListener {

    private static final String TAG = "PhotoViewer";
    public static final String EXTRA_IMAGE_RES_ID = "extra_image_res_id";
    public static final String IS_CHECK = "is_check";
    public Matrix matrix = new Matrix();
    public Matrix savedMatrix = new Matrix();
    public DisplayMetrics dm;
    public ImageView imgView,mLoadingImg;
    public Bitmap bitmap, bitmap1;

    float minScaleR = 1f;
    static final float MAX_SCALE = 10f;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    public PointF prev = new PointF();
    public PointF mid = new PointF();
    float dist = 1f;
    private ProgressBar mProgressBar;
    int count = 0, firClick = 0, secClick = 0;
    private Uri photoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_view_select);
        // 获取图片资源
        imgView = (ImageView) findViewById(R.id.photo_view);// 获取控件
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        View closeView = findViewById(R.id.close);
        View yesView= findViewById(R.id.yes);

        closeView.setOnClickListener(this);
        yesView.setOnClickListener(this);
         dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Intent intent = getIntent();
        int imageResId = intent.getIntExtra(EXTRA_IMAGE_RES_ID, -1);
        photoUri = intent.getData();
        if (imageResId > 0) {
            mProgressBar.setVisibility(View.GONE);
            imgView.setImageResource(imageResId);
        } else if (photoUri != null) {
            new LoadTask(dm.widthPixels, dm.heightPixels).load(photoUri);
        } else {
            imgView.setImageResource(R.drawable.image_load_fail);
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        boolean isCheck=false;
        switch (v.getId()) {
            case R.id.close:
                isCheck=false;
                break;
            case R.id.yes:
                isCheck=true;
                break;
            default:
                break;
        }
        intent.putExtra(IS_CHECK,isCheck);
        setResult(RESULT_OK,intent);
        finish();
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
                        bitmap1 = BitmapFactory.decodeStream(in, null, options);
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

            return bitmap1;
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mProgressBar.setVisibility(View.GONE);
            if (bitmap != null) {
                imgView.setImageBitmap(bitmap1);
                center();
//              anim.stop();
                imgView.setImageMatrix(matrix);
            } else {
                imgView.setImageResource(R.drawable.image_load_fail);
            }
        }
    }
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 主点按下
            case MotionEvent.ACTION_DOWN:

                count++;
                if (count == 1) {
                    firClick = (int) System.currentTimeMillis();

                } else if (count == 2) {
                    secClick = (int) System.currentTimeMillis();
                    if (secClick - firClick < 500) {
                        finish();
                    }
                    count = 0;
                    firClick = 0;
                    secClick = 0;
                }

                savedMatrix.set(matrix);
                prev.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            // 副点按下
            case MotionEvent.ACTION_POINTER_DOWN:
                dist = spacing(event);
                // 如果连续两点距离大于10，则判定为多点模式
                if (spacing(event) > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                // savedMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - prev.x, event.getY()
                            - prev.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float tScale = newDist / dist;
                        matrix.postScale(tScale, tScale, mid.x, mid.y);
                    }
                }
                break;
        }
        imgView.setImageMatrix(matrix);
        CheckView();
        return true;
    }

    private void CheckView() {
        float p[] = new float[9];
        matrix.getValues(p);
        if (mode == ZOOM) {
            if (p[0] < minScaleR) {
                // Log.d("", "当前缩放级别:"+p[0]+",最小缩放级别:"+minScaleR);
                matrix.setScale(minScaleR, minScaleR);
            }
            if (p[0] > MAX_SCALE) {
                // Log.d("", "当前缩放级别:"+p[0]+",最大缩放级别:"+MAX_SCALE);
                matrix.set(savedMatrix);
            }
        }
        center();
    }


    private void center() {
        center(true, true);
    }


    protected void center(boolean horizontal, boolean vertical) {

        Matrix m = new Matrix();
        m.set(matrix);
        RectF rect = new RectF(0, 0, bitmap1.getWidth(), bitmap1.getHeight());
        m.mapRect(rect);

        float height = rect.height();
        float width = rect.width();

        float deltaX = 0, deltaY = 0;

        if (vertical) {
            // 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
            int screenHeight = dm.heightPixels;
            if (height < screenHeight) {
                deltaY = (screenHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < screenHeight) {
                deltaY = imgView.getHeight() - rect.bottom;
            }
        }

        if (horizontal) {
            int screenWidth = dm.widthPixels;
            if (width < screenWidth) {
                deltaX = (screenWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < screenWidth) {
                deltaX = screenWidth - rect.right;
            }
        }
        matrix.postTranslate(deltaX, deltaY);
    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private InputStream openInputStream(Uri uri) throws Exception {
        final String scheme = uri.getScheme();
        final InputStream in;

        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            OpenResourceIdResult r = getResourceId(uri);
            in = r.r.openRawResource(r.id);
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
            in = getContentResolver().openInputStream(uri);
        } else {
            in = new FileInputStream(uri.toString());
        }

        return in;
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

}
