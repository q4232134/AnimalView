package com.jiaozhu.ahibernate.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
    private static int connectNum = 0;

    public MyDBHelper(Context context, String databaseName,
                      SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public void onCreate(SQLiteDatabase db) {
        TableHelper.createTable(db, DaoManager.getInstance().getTableNameList().toArray(new String[0]));
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


}