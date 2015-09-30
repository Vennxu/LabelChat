package com.ekuater.labelchat.ui.widget.emoji;

import android.content.Context;
import android.text.Spannable;
import android.text.style.DynamicDrawableSpan;

import com.ekuater.labelchat.ui.widget.emoji.util.EmojiFace;
import com.ekuater.labelchat.ui.widget.emoji.util.StaticImageSpan;

/**
 * @author LinYong
 */
/*package*/ final class EmotifyHelper {

    public static void emotify(Context context, Spannable spannable, int size) {
        final Context appContext = context.getApplicationContext();
        final EmojiFace emojiFace = EmojiFace.getInstance(appContext);
        final String startTag = emojiFace.getStartTag();
        final String endTag = emojiFace.getEndTag();
        final int startTagLen = startTag.length();
        final int endTagLen = endTag.length();

        int length = spannable.length();
        int tagStart = -1;
        boolean inTag = false;

        if (length <= 0) {
            return;
        }

        for (int position = 0; position < length; ++position) {
            String tmp = spannable.subSequence(position, position + startTagLen).toString();

            if (tmp.equals(startTag)) {
                tagStart = position;
                inTag = true;
                continue;
            }

            if (inTag) {
                tmp = spannable.subSequence(position, position + endTagLen).toString();

                // Have we reached end of the tag?
                if (tmp.equals(endTag)) {
                    final int tagEnd = position + endTagLen;
                    final int faceStart = tagStart + startTagLen;
                    final int faceEnd = tagEnd - endTagLen;

                    if (faceEnd > faceStart) {
                        String face = spannable.subSequence(faceStart, faceEnd).toString();
                        DynamicDrawableSpan imageSpan = getStaticImageSpan(appContext,
                                emojiFace, face, size);

                        if (imageSpan != null) {
                            spannable.setSpan(imageSpan, tagStart,
                                    tagEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    inTag = false;
                    tagStart = -1;
                }
            }
        }
    }

    private static DynamicDrawableSpan getStaticImageSpan(Context context, EmojiFace emojiFace,
                                                          String face, int size) {
        final int id = emojiFace.getStaticFaceId(face);
        return (id > 0) ? new StaticImageSpan(context, id, size) : null;
    }
}
