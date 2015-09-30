package com.ekuater.labelchat.banknote;

import android.graphics.Bitmap;

/**
 * Created by Leo on 2015/5/11.
 *
 * @author LinYong
 */
public class ComposeResult {

    public ComposeError error;
    public Bitmap faceBanknote;

    public ComposeResult(ComposeError error, Bitmap faceBanknote) {
        this.error = error;
        this.faceBanknote = faceBanknote;
    }
}
