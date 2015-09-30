
package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

import com.ekuater.labelchat.util.UUIDGenerator;

/**
 * @author LinYong
 */
public class RequestCommand implements Parcelable {

    private static final String EMPTY_STRING = "";

    private String mSession;
    private String mUrl;
    private String mParam;
    private int mRequestMethod;

    public RequestCommand() {
        mSession = UUIDGenerator.generate();
        mUrl = EMPTY_STRING;
        mParam = EMPTY_STRING;
        mRequestMethod = ConstantCode.REQUEST_POST;
    }

    public String getSession() {
        return mSession;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = (url != null) ? url : EMPTY_STRING;
    }

    public String getParam() {
        return mParam;
    }

    public void setParam(String param) {
        mParam = (param != null) ? param : EMPTY_STRING;
    }

    public int getRequestMethod() {
        return mRequestMethod;
    }

    public void setRequestMethod(int method) {
        mRequestMethod = method;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSession);
        dest.writeString(mUrl);
        dest.writeString(mParam);
        dest.writeInt(mRequestMethod);
    }

    public static final Parcelable.Creator<RequestCommand> CREATOR = new Parcelable.Creator<RequestCommand>() {

        @Override
        public RequestCommand createFromParcel(Parcel source) {
            RequestCommand cmd = new RequestCommand();

            cmd.mSession = source.readString();
            cmd.mUrl = source.readString();
            cmd.mParam = source.readString();
            cmd.mRequestMethod = source.readInt();

            return cmd;
        }

        @Override
        public RequestCommand[] newArray(int size) {
            return new RequestCommand[size];
        }
    };
}
