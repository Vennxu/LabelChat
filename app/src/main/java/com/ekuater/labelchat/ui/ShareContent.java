package com.ekuater.labelchat.ui;

import android.graphics.Bitmap;

import com.ekuater.labelchat.util.TextUtil;

/**
 * Created by Leo on 2015/1/22.
 *
 * @author LinYong
 */
public class ShareContent {

    public enum Platform {

        QQ("QQ"),
        QZONE("QZone"),
        SINA_WEIBO("SinaWeibo"),
        WEIXIN("WeiXin"),
        WEIXIN_CIRCLE("WeiXinCircle");

        private final String platform;

        Platform(String platform) {
            this.platform = platform;
        }

        public String getPlatform() {
            return platform;
        }
    }

    public enum MediaType {
        IMAGE,
        VIDEO,
        MUSIC,
    }

    private final String title;
    private final String content;
    private final Bitmap icon;
    private final String url;
    private final String labelStoryId;

    private String mediaFileUrl;
    private MediaType mediaType;

    private Platform sharePlatform;

    public ShareContent() {
        this(null, null, null, null, null);
    }

    /**
     * @deprecated
     */
    public ShareContent(String title, String content,
                        Bitmap icon, String url) {
        this(title, content, icon, url, "");
    }

    public ShareContent(String title, String content,
                        Bitmap icon, String url,
                        String labelStoryId) {
        this.title = title;
        this.content = content;
        this.icon = icon;
        this.url = url;
        this.labelStoryId = labelStoryId;

        this.mediaFileUrl = null;
        this.mediaType = null;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }

    public String getLabelStoryId() {
        return labelStoryId;
    }

    public Platform getSharePlatform() {
        return sharePlatform;
    }

    public void setSharePlatform(Platform sharePlatform) {
        this.sharePlatform = sharePlatform;
    }

    public void setShareMedia(String mediaFileUrl, MediaType mediaType) {
        this.mediaFileUrl = mediaFileUrl;
        this.mediaType = mediaType;
    }

    public String getMediaFile() {
        return mediaFileUrl;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public boolean hasShareMedia() {
        return mediaType != null && !TextUtil.isEmpty(mediaFileUrl);
    }
}
