package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/5/25.
 *
 * @author FanChong
 */
public class Music implements Parcelable {
    private String songId;
    private String singerId;
    private String songName;
    private String singerName;
    private String albumName;
    private String singerPic;
    private String musicUrl;
    private long duration;

    public Music() {
    }

    private Music(Parcel in) {
        this.songId = in.readString();
        this.singerId = in.readString();
        this.songName = in.readString();
        this.singerName = in.readString();
        this.albumName = in.readString();
        this.singerPic = in.readString();
        this.musicUrl = in.readString();
        this.duration = in.readLong();
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getSingerId() {
        return singerId;
    }

    public void setSingerId(String singerId) {
        this.singerId = singerId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getSingerPic() {
        return singerPic;
    }

    public void setSingerPic(String singerPic) {
        this.singerPic = singerPic;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.songId);
        dest.writeString(this.singerId);
        dest.writeString(this.songName);
        dest.writeString(this.singerName);
        dest.writeString(this.albumName);
        dest.writeString(this.singerPic);
        dest.writeString(this.musicUrl);
        dest.writeLong(this.duration);
    }

    public static final Parcelable.Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            return new Music(source);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };
}
