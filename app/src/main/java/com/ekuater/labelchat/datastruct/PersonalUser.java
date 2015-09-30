package com.ekuater.labelchat.datastruct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/4/28.
 */
public class PersonalUser implements Parcelable {

    public static final int CONTACT = 1;
    public static final int STRANGER = 2;

    private int type;
    private UserContact userContact;

    public PersonalUser(int type, UserContact userContact) {
        this.type = type;
        this.userContact = userContact;
    }

    public PersonalUser(Parcel source) {
        this.type = source.readInt();
        this.userContact = source.readParcelable(UserContact.class.getClassLoader());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UserContact getUserContact() {
        return userContact;
    }

    public void setUserContact(UserContact userContact) {
        this.userContact = userContact;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeParcelable(userContact, flags);
    }

    public static final Parcelable.Creator<PersonalUser> CREATOR = new Parcelable.Creator<PersonalUser>() {
        @Override
        public PersonalUser createFromParcel(Parcel source) {
            return new PersonalUser(source);
        }

        @Override
        public PersonalUser[] newArray(int size) {
            return new PersonalUser[size];
        }
    };
}
