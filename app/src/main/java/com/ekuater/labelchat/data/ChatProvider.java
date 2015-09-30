
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

import com.ekuater.labelchat.data.DataConstants.Chat;
import com.ekuater.labelchat.datastruct.ChatMessage;

/**
 * Chat message provider
 *
 * @author LinYong
 */
public class ChatProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String DATABASE_NAME = "chats.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "chats";
    private static final int MESSAGES = 1;
    private static final int MESSAGE_ID = 2;
    private static final String DEFAULT_SORT_ORDER = Chat._ID + " ASC";

    static {
        URI_MATCHER.addURI(Chat.AUTHORITY, Chat.PATH, MESSAGES);
        URI_MATCHER.addURI(Chat.AUTHORITY, Chat.PATH + "/#", MESSAGE_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create chat message history database table
            db.execSQL("CREATE TABLE " + TABLE_NAME
                    + " ("
                    + Chat._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + Chat.TARGET_ID + " TEXT NOT NULL,"
                    + Chat.MESSAGE_ID + " TEXT NOT NULL,"
                    + Chat.DIRECTION + " INTEGER NOT NULL,"
                    + Chat.DATETIME + " INTEGER NOT NULL,"
                    + Chat.STATE + " INTEGER NOT NULL,"
                    + Chat.TYPE + " INTEGER NOT NULL,"
                    + Chat.CONTENT + " TEXT,"
                    + Chat.PREVIEW + " TEXT,"
                    + Chat.SENDER_ID + " TEXT,"
                    + Chat.CONVERSATION + " INTEGER NOT NULL DEFAULT "
                    + ChatMessage.CONVERSATION_PRIVATE
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + Chat.SENDER_ID + " TEXT;");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + Chat.CONVERSATION
                        + " INTEGER NOT NULL DEFAULT " + ChatMessage.CONVERSATION_PRIVATE + ";");
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
            case MESSAGES:
                type = Chat.CONTENT_TYPE;
                break;
            case MESSAGE_ID:
                type = Chat.CONTENT_ITEM_TYPE;
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
            case MESSAGES:
                qBuilder.setTables(TABLE_NAME);
                break;
            case MESSAGE_ID:
                qBuilder.setTables(TABLE_NAME);
                qBuilder.appendWhere(Chat._ID + "=");
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
        if (URI_MATCHER.match(uri) != MESSAGES) {
            throw new IllegalArgumentException("Unknown Uri " + uri);
        }

        ContentValues newValues = (values != null) ? new ContentValues(values)
                : new ContentValues();

        for (String column : Chat.REQUIRED_COLUMNS) {
            if (!newValues.containsKey(column)) {
                throw new IllegalArgumentException("Missing column: " + column);
            }
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(TABLE_NAME, Chat.DATETIME, newValues);

        Uri noteUri = ContentUris.withAppendedId(Chat.CONTENT_URI, rowId);
        mContext.getContentResolver().notifyChange(noteUri, null);

        return noteUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case MESSAGES:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case MESSAGE_ID:
                String segment = uri.getPathSegments().get(1);
                String whereClause;

                if (TextUtils.isEmpty(selection)) {
                    whereClause = Chat._ID + "=" + segment;
                } else {
                    whereClause = Chat._ID + "=" + segment + " AND (" + selection + ")";
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
            case MESSAGES:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case MESSAGE_ID:
                String segment = uri.getPathSegments().get(1);
                String whereClause;

                if (TextUtils.isEmpty(selection)) {
                    whereClause = Chat._ID + "=" + segment;
                } else {
                    whereClause = Chat._ID + "=" + segment + " AND (" + selection + ")";
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
