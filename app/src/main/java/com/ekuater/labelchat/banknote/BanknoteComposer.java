package com.ekuater.labelchat.banknote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.FaceDetector;

/**
 * Created by Leo on 2015/5/11.
 *
 * @author LinYong
 */
public class BanknoteComposer {

    private static float[] colorFilterMatrix(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        float total = red + green + blue;

        float red_weight = red / total;
        float green_weight = green / total;
        float blue_weight = blue / total;
        float red_per = red / (float) 0xFF;
        float green_per = green / (float) 0xFF;
        float blue_per = blue / (float) 0xFF;

        return new float[]{
                red_weight * red_per, green_weight * red_per, blue_weight * red_per, 0, 0,
                red_weight * green_per, green_weight * green_per, blue_weight * green_per, 0, 0,
                red_weight * blue_per, green_weight * blue_per, blue_weight * blue_per, 0, 0,
                0, 0, 0, 1, 0,
        };
    }

    private final Context context;

    public BanknoteComposer(Context context) {
        this.context = context;
    }

    public ComposeResult compose(String photoPath, Banknote banknote) {
        Bitmap photoBitmap = BitmapFactory.decodeFile(photoPath);
        ComposeResult result = compose(photoBitmap, banknote);
        photoBitmap.recycle();
        return result;
    }

    public ComposeResult compose(Bitmap photoBitmap, Banknote banknote) {
        Bitmap faceDetectBmp = photoBitmap.copy(Bitmap.Config.RGB_565, true);
        int photoWidth = photoBitmap.getWidth();
        int photoHeight = photoBitmap.getHeight();
        FaceDetector detector = new FaceDetector(photoWidth, photoHeight, 1);
        FaceDetector.Face faces[] = new FaceDetector.Face[1];
        int faceCount = detector.findFaces(faceDetectBmp, faces);
        Point pos = banknote.getFacePosition();
        Point size = banknote.getFaceSize();

        faceDetectBmp.recycle();
        if (faceCount <= 0) {
            return new ComposeResult(ComposeError.NO_FACE, null);
        }

        FaceDetector.Face face = faces[0];
        PointF midPoint = new PointF();
        face.getMidPoint(midPoint);
        int faceWidth = Math.round(face.eyesDistance() * 2.6f);
        // int faceHeight = Math.round(faceWidth / 0.618f);
        int faceHeight = Math.round(faceWidth * (size.y / (float) size.x));
        int faceX = Math.round(midPoint.x - faceWidth / 2);
        int faceY = Math.round(midPoint.y - faceHeight * 4 / 9);

        Rect faceBound = new Rect(
                Math.max(0, faceX),
                Math.max(0, faceY),
                Math.min(photoWidth, faceX + faceWidth),
                Math.min(photoHeight, faceY + faceHeight));

        Bitmap faceBmp = Bitmap.createBitmap(faceBound.width(),
                faceBound.height(), Bitmap.Config.ARGB_8888);
        Rect dstBound = new Rect(0, 0, faceBmp.getWidth(), faceBmp.getHeight());
        Canvas canvas = new Canvas(faceBmp);
        Paint paint = new Paint();

        paint.reset();
        paint.setAntiAlias(true);
        canvas.drawBitmap(photoBitmap, faceBound, dstBound, paint);

        faceBmp = recycleOld(brightBitmap(faceBmp, 30), faceBmp);

        Bitmap banknoteBmp = banknote.decodeImage(context.getAssets());
        if (banknoteBmp == null) {
            faceBmp.recycle();
            return new ComposeResult(ComposeError.BANKNOTE_DECODE_ERROR, null);
        }

        Bitmap overlayBmp = Bitmap.createBitmap(banknoteBmp.getWidth(),
                banknoteBmp.getHeight(), banknoteBmp.getConfig());

        canvas = new Canvas(overlayBmp);
        faceBound.offset(-faceBound.left, -faceBound.top);
        dstBound.set(pos.x, pos.y, pos.x + size.x, pos.y + size.y);

        canvas.drawColor(Color.WHITE);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setMaskFilter(new BlurMaskFilter(Math.max(size.x, size.y) / 2.0f,
                BlurMaskFilter.Blur.SOLID));
        Matrix matrix = new Matrix();
        matrix.setRectToRect(new RectF(faceBound), new RectF(dstBound),
                Matrix.ScaleToFit.CENTER);
        canvas.drawBitmap(faceBmp, matrix, paint);

        paint.reset();
        paint.setAntiAlias(true);
        canvas.drawBitmap(banknoteBmp, 0, 0, paint);

        faceBmp.recycle();
        banknoteBmp.recycle();
        return new ComposeResult(ComposeError.SUCCESS, overlayBmp);
    }

    private Bitmap recycleOld(Bitmap newBmp, Bitmap oldBmp) {
        oldBmp.recycle();
        return newBmp;
    }

    private static int[] discolor(int[] pixels) {
        for (int i = 0; i < pixels.length; ++i) {
            int color = pixels[i];
            int a = Color.alpha(color);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            int grey = (int) (r * 0.299f + g * 0.587f + b * 0.114f);
            pixels[i] = Color.argb(a, grey, grey, grey);
        }

        return pixels;
    }

    private static int[] reverseColor(int[] pixels) {
        for (int i = 0; i < pixels.length; ++i) {
            int color = pixels[i];
            int a = (color & 0xff000000) >> 24;
            int r = 255 - ((color & 0x00ff0000) >> 16);
            int g = 255 - ((color & 0x0000ff00) >> 8);
            int b = 255 - ((color & 0x000000ff));
            pixels[i] = a << 24 | r << 16 | g << 8 | b;
        }

        return pixels;
    }

    private static int[] cameo(int[] pixels) {
        int preColor = pixels[0];
        int prepareColor = 0;

        for (int i = 0; i < pixels.length; ++i) {
            int currColor = pixels[i];
            int r = Color.red(currColor) - Color.red(prepareColor) + 128;
            int g = Color.green(currColor) - Color.red(prepareColor) + 128;
            int b = Color.green(currColor) - Color.blue(prepareColor) + 128;
            int a = Color.alpha(currColor);
            int newColor = (int) (r * 0.3 + g * 0.59 + b * 0.11);
            int modifyColor = Color.argb(a, newColor, newColor, newColor);

            pixels[i] = modifyColor;
            prepareColor = preColor;
            preColor = currColor;
        }

        return pixels;
    }

    private static Bitmap grayBitmap(Bitmap bitmap) {
        Bitmap newBmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), bitmap.getConfig());
        ColorMatrix matrix = new ColorMatrix();
        Canvas canvas = new Canvas(newBmp);
        Paint paint = new Paint();

        matrix.setSaturation(0.0f);
        paint.setAntiAlias(true);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return newBmp;
    }

    private static Bitmap brightBitmap(Bitmap bitmap, float brightness) {
        Bitmap newBmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), bitmap.getConfig());
        ColorMatrix matrix = new ColorMatrix(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        });
        Canvas canvas = new Canvas(newBmp);
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return newBmp;
    }

    private static Bitmap multiplyBitmap(Bitmap bitmap, int color) {
        Bitmap newBmp = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(newBmp);
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return newBmp;
    }
}
