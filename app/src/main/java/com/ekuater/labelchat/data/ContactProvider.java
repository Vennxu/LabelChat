
package com.ekuater.labelchat.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.ekuater.labelchat.data.DataConstants.Contact;

/**
 * @author LinYong
 */
public class ContactProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "contacts";
    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;
    private static final String DEFAULT_SORT_ORDER = Contact._ID + " ASC";

    static {
        URI_MATCHER.addURI(Contact.AUTHORITY, Contact.PATH, CONTACTS);
        URI_MATCHER.addURI(Contact.AUTHORITY, Contact.PATH + "/#", CONTACT_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create database table
            db.execSQL("CREATE TABLE " + TABLE_NAME
                    + " ("
                    + Contact._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + Contact.USER_ID + " TEXT NOT NULL UNIQUE,"
                    + Contact.LABEL_CODE + " TEXT NOT NULL UNIQUE,"
                    + Contact.NICKNAME + " TEXT,"
                    + Contact.REMARKS_NAME + " TEXT,"
                    + Contact.MOBILE + " TEXT,"
                    + Contact.SEX + " INTEGER,"
                    + Contact.BIRTHDAY + " INTEGER,"
                    + Contact.AGE + " INTEGER,"
                    + Contact.CONSTELLATION + " INTEGER,"
                    + Contact.PROVINCE + " TEXT,"
                    + Contact.CITY + " TEXT,"
                    + Contact.SCHOOL + " TEXT,"
                    + Contact.SIGNATURE + " TEXT,"
                    + Contact.AVATAR + " TEXT,"
                    + Contact.AVATAR_THUMB + " TEXT,"
                    + Contact.LABELS + " TEXT,"
                    + Contact.APPEARANCE_FACE + " TEXT,"
                    + Contact.THEME + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + Contact.THEME + " TEXT;");
                oldVersion++;
            }
        }
    }

    private Context mContext;
    private SQLiteOpenHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new DatabaseHelper(mContext);

        return true;
    }

    @Override
    public String getType(Uri uri) {
        String type;

        switch (URI_MATCHER.match(uri)) {
            case CONTACTS:
                type = Contact.CONTENT_TYPE;
                break;
            case CONTACT_ID:
                type = Contact.CONTENT_ITEM_TYPE;
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }

        return type;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        switch (URI_MATCHER.match(uri)) {
            case CONTACTS:
                qBuilder.setTables(TABLE_NAME);
                break;
            case CONTACT_ID:
                qBuilder.setTables(TABLE_NAME);
                qBuilder.appendWhere(Contact._ID + "=");
                qBuilder.appendWhere(uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c;
        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        c = qBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);

        if (c != null) {
            c.setNotificationUri(mContext.getContentResolver(), uri);
        }

        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (URI_MATCHER.match(uri) != CONTACTS) {
            throw new IllegalArgumentException("Unknown Uri " + uri);
        }

        ContentValues newValues = (values != null) ? new ContentValues(values)
                : new ContentValues();

        for (String column : Contact.REQUIRED_COLUMNS) {
            if (!newValues.containsKey(column)) {
                throw new IllegalArgumentException("Missing column: " + column);
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, Contact.USER_ID, newValues);

        Uri noteUri = ContentUris.withAppendedId(Contact.CONTENT_URI, rowId);
        mContext.getContentResolver().notifyChange(noteUri, null);

        return noteUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case CONTACTS:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACT_ID:
                String segment = uri.getPathSegments().get(1);
                String whereClause;

                if (TextUtils.isEmpty(selection)) {
                    whereClause = Contact._ID + "=" + segment;
                } else {
                    whereClause = Contact._ID + "=" + segment + " AND (" + selection + ")";
                }

                count = db.delete(TABLE_NAME, whereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }

        mContext.getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case CONTACTS:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case CONTACT_ID:
                String segment = uri.getPathSegments().get(1);
                String whereClause;

                if (TextUtils.isEmpty(selection)) {
                    whereClause = Contact._ID + "=" + segment;
                } else {
                    whereClause = Contact._ID + "=" + segment + " AND (" + selection + ")";
                }

                count = db.update(TABLE_NAME, values, whereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }

        mContext.getContentResolver().notifyChange(uri, null);

        return count;
    }
}
