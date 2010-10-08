package net.sourcewalker.syncdemo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NumbersDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "numbers.sql";
    private static final int DATABASE_VERSION = 1;

    public NumbersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Numbers.SCHEMA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.delete(Numbers.TABLE, null, null);
        onCreate(db);
    }

}
