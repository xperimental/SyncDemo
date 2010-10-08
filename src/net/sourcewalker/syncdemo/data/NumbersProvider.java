package net.sourcewalker.syncdemo.data;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class NumbersProvider extends ContentProvider {

    private static final UriMatcher matcher;
    private static final HashMap<String, String> projectionMap;

    private static final String TAG = "NumbersProvider";
    private static final int MATCH_LIST = 1;
    private static final int MATCH_ITEM = 2;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Numbers.AUTHORITY, "/", MATCH_LIST);
        matcher.addURI(Numbers.AUTHORITY, "#", MATCH_ITEM);

        projectionMap = new HashMap<String, String>();
        for (String col : Numbers.DEFAULT_PROJECTION) {
            projectionMap.put(col, col);
        }
    }

    private NumbersDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new NumbersDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result;
        switch (matcher.match(uri)) {
        case MATCH_LIST:
            result = db.delete(Numbers.TABLE, null, null);
            break;
        case MATCH_ITEM:
            result = db.delete(Numbers.TABLE, Numbers._ID + " == ?",
                    new String[] { uri.getLastPathSegment() });
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");
        switch (matcher.match(uri)) {
        case MATCH_ITEM:
            return Numbers.CONTENT_TYPE_ITEM;
        case MATCH_LIST:
            return Numbers.CONTENT_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert");
        switch (matcher.match(uri)) {
        case MATCH_LIST:
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.insert(Numbers.TABLE, Numbers.STATUS, values);
            Uri numberUri = Uri.withAppendedPath(Numbers.CONTENT_URI, values
                    .getAsString(Numbers._ID));
            getContext().getContentResolver().notifyChange(numberUri, null);
            return numberUri;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query");
        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables(Numbers.TABLE);
        query.setProjectionMap(projectionMap);
        if (sortOrder == null) {
            sortOrder = Numbers.DEFAULT_SORT_ORDER;
        }
        switch (matcher.match(uri)) {
        case MATCH_LIST:
            break;
        case MATCH_ITEM:
            query.appendWhere(Numbers._ID + " == " + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = query.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        Log.d(TAG, "update");
        switch (matcher.match(uri)) {
        case MATCH_ITEM:
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int result = db.update(Numbers.TABLE, values,
                    Numbers._ID + " == ?", new String[] { uri
                            .getLastPathSegment() });
            getContext().getContentResolver().notifyChange(uri, null);
            return result;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

}
