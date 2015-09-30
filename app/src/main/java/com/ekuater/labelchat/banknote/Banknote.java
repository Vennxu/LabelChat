package com.ekuater.labelchat.banknote;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Leo on 2015/5/10.
 *
 * @author LinYong
 */
public class Banknote {

    private String image;
    private Point facePosition;
    private Point faceSize;
    private int baseColor;

    public Banknote() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Point getFacePosition() {
        return facePosition;
    }

    public void setFacePosition(Point facePosition) {
        this.facePosition = facePosition;
    }

    public Point getFaceSize() {
        return faceSize;
    }

    public void setFaceSize(Point faceSize) {
        this.faceSize = faceSize;
    }

    public int getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(int baseColor) {
        this.baseColor = baseColor;
    }

    public Bitmap decodeImage(AssetManager am) {
        InputStream is = null;
        try {
            is = am.open("face_banknote/" + image);
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
