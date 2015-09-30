package com.ekuater.labelchat.ui.fragment.labelstory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ekuater.labelchat.R;
import com.ekuater.labelchat.delegate.AvatarManager;
import com.ekuater.labelchat.delegate.ShortUrlImageLoadListener;
import com.ekuater.labelchat.delegate.imageloader.LoadFailType;

import java.util.Timer;
import java.util.TimerTask;

public class ShowBigImageActivity extends Activity implements OnTouchListener, Handler.Callback {

    public static final String EXTRA_BITMAP = "extra_bitmap";

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private DisplayMetrics dm;
    private ImageView imgView;
    private Bitmap bitmap, bitmap1;

    /**
     * 最小缩放比例
     */
    private float minScaleR = 1f;
    /**
     * 最大缩放比例
     */
    private static final float MAX_SCALE = 10f;

    /**
     * 初始状态
     */
    private static final int NONE = 0;
    /**
     * 拖动
     */
    private static final int DRAG = 1;
    /**
     * 缩放
     */
    private static final int ZOOM = 2;

    /**
     * 当前模式
     */
    private int mode = NONE;

    private PointF prev = new PointF();
    private PointF mid = new PointF();
    private float dist = 1f;
    private int count = 0, firClick = 0, secClick = 0;
    private String mUrl;
    private static final int MSG_HANDLE_THROW_PHOTO_RESULT = 102;
    private static final int MSG_TIMER = 103;
    private ProgressBar mProgressBar;

    private final Handler mHandler = new Handler(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_acivity);
        // 获取图片资源
        imgView = (ImageView) findViewById(R.id.imag1);// 获取控件
        mProgressBar = (ProgressBar) findViewById(R.id.showprogress);
        mProgressBar.setVisibility(View.VISIBLE);
        parseArguments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;

        switch (msg.what) {
            case MSG_HANDLE_THROW_PHOTO_RESULT:
                mProgressBar.setVisibility(View.GONE);
                handlerLoadThrowPhoto(msg.obj);
                break;
            case MSG_TIMER:
                timer.cancel();
                if (!isFinishing()) {
                    KeepPhotoDialog keepPhotoDialog = new KeepPhotoDialog(this,
                            R.style.Dialog, bitmap);
                    keepPhotoDialog.show();
                }
                break;
            default:
                handled = false;
                break;
        }
        return handled;
    }

    private void parseArguments() {
        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        if (args != null) {
            String url = args.getString(LabelStoryUtils.SHOW_PHOTO_URL);
            if (!TextUtils.isEmpty(url)) {
                mUrl = args.getString(LabelStoryUtils.SHOW_PHOTO_URL);
                Log.d("show", mUrl);
                loadContentImage();
            } else {
                Bitmap bitmap = args.getParcelable(EXTRA_BITMAP);
                loadThrowPhoto(bitmap);
            }
        }
    }

    private ShortUrlImageLoadListener mLoadListener = new ShortUrlImageLoadListener() {
        @Override
        public void onLoadFailed(String shortUrl, LoadFailType loadFailType) {
//            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadComplete(String shortUrl, Bitmap loadedImage) {
            if (loadedImage != null) {
                Log.d("show", shortUrl);
            }
            loadThrowPhoto(loadedImage);
//            mProgressBar.setVisibility(View.GONE);
        }
    };

    public void loadContentImage() {
        Bitmap bitmap = null;
        if (mUrl != null) {
            bitmap = AvatarManager.getInstance(this).getLabelStoryImage(
                    mUrl, mLoadListener);
        }
        if (bitmap != null) {
            loadThrowPhoto(bitmap);
        }
    }

    private void loadThrowPhoto(Bitmap bitmap) {
        Message message = mHandler.obtainMessage(MSG_HANDLE_THROW_PHOTO_RESULT, bitmap);
        mHandler.sendMessage(message);
    }

    private void handlerLoadThrowPhoto(Object object) {
        bitmap = (Bitmap) object;
        if (bitmap != null) {
            imgView.setImageBitmap(bitmap);
            imgView.setOnTouchListener(ShowBigImageActivity.this);// 设置触屏监听
            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);// 获取分辨率
            // 获取这个图片的宽和高
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 定义预转换成的图片的宽度和高度
            int newWidth = dm.widthPixels;
            // 计算缩放率，新尺寸除原始尺寸
            float scaleWidth = ((float) newWidth) / width;

            // 创建操作图片用的matrix对象
            Matrix matrix1 = new Matrix();
            // 缩放图片动作
            matrix1.postScale(scaleWidth, scaleWidth);

            // 创建新的图片
            bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix1,
                    true);
            imgView.setImageBitmap(bitmap1);// 填充控件
            // minZoom();
            center();
            imgView.setImageMatrix(matrix);
        } else {
            //下载失败，设置默认图片
            imgView.setBackgroundResource(R.drawable.image_load_fail);
        }
    }

    private float firstX, firstY;
    private float endX, endY;

    /**
     * 触屏监听
     */
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 主点按下
            case MotionEvent.ACTION_DOWN:
                firstX = event.getX();
                firstY = event.getY();
                Log.d("eventDown", event.getX() + "XY" + event.getY());
                count++;
                if (count == 1) {
                    firClick = (int) System.currentTimeMillis();
                } else if (count == 2) {
                    secClick = (int) System.currentTimeMillis();//双击事件
                }

                savedMatrix.set(matrix);
                prev.set(event.getX(), event.getY());
                mode = DRAG;
                if (mode == DRAG) {
                    Log.d("mode", "---");
                    timeTask();
                }
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
                timer.cancel();
                break;
            case MotionEvent.ACTION_UP:
                secClick = (int) System.currentTimeMillis();
                if (secClick - firClick < 100) {
                    finish();
                } else {
                    timer.cancel();
                }
                firstY = 0;
                firstX = 0;
                endX = 0;
                endY = 0;
                count = 0;
                firClick = 0;
                secClick = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                // savedMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    endX = event.getX();
                    endY = event.getY();
                    Log.d("eventUp", event.getX() + "XY" + event.getY());
                    if (endY - firstY > 3 || endX - firstX > 3) {
                        Log.d("eventUp", "cancel");
                        timer.cancel();
                    }
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

    Timer timer = null;

    public void timeTask() {
        TimerTask task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = MSG_TIMER;
                mHandler.sendMessage(message);
            }
        };
        timer = new Timer(true);
        timer.schedule(task, 1000, 1000);
    }

    /**
     * 限制最大最小缩放比例，自动居中
     */
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

    /**
     * 最小缩放比例，最大为100%
     */
    private void minZoom() {
        minScaleR = Math.min(
                (float) dm.widthPixels / (float) bitmap1.getWidth(),
                (float) dm.heightPixels / (float) bitmap1.getHeight());
        if (minScaleR < 1.0) {
            matrix.postScale(minScaleR, minScaleR);
        }
    }

    private void center() {
        center(true, true);
    }

    /**
     * 横向、纵向居中
     */
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

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 两点的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}