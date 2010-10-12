package net.sourcewalker.syncdemo.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Numbers implements BaseColumns {

    public static final String AUTHORITY = "net.sourcewalker.syncdemo";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/numbers");

    public static final Uri CONTENT_URI_ALL = Uri.withAppendedPath(CONTENT_URI,
            "all");

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
            + AUTHORITY;

    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/"
            + AUTHORITY;

    public static final String TABLE = "numbers";

    public static final String STATUS = "status";

    public static final String STATUS_LOCAL = "local";
    public static final String STATUS_REMOTE = "remote";
    public static final String STATUS_DELETED = "deleted";

    public static final String[] DEFAULT_PROJECTION = new String[] { _ID,
            STATUS };

    public static final String DEFAULT_SORT_ORDER = Numbers._ID + " ASC";

    public static final String SCHEMA = "CREATE TABLE " + TABLE + " (" + _ID
            + " INTEGER PRIMARY KEY, " + STATUS + " TEXT )";

}
