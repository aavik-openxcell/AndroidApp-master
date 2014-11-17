package com.icanvass.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.icanvass.helpers.SDDefine;

public class SDSQLiteHelper extends NMSQLiteHelper {

    private static SDSQLiteHelper instance;

    public SDSQLiteHelper(Context context) {
        super(context, SDDefine.dbName,
                SDDefine.dbDefaultVersion);
    }

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new SDSQLiteHelper(context);
        }
    }

    public static synchronized SDSQLiteHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    SDSQLiteHelper.class.getSimpleName()
                            + " is not initialized, call initializeInstance(..) method first."
            );
        }

        return instance;
    }

    @Override
    protected void initDatabase(SQLiteDatabase database) {
        database.execSQL(PINTable.createStatement());
        database.execSQL(PINTable.createIndexesStatement());
        database.execSQL(ClientDataTable.createStatement());
        database.execSQL(CustomValuesTable.createStatement());
        database.execSQL(CustomValuesTable.createIndexesStatement());
        database.execSQL(LocationTable.createStatement());
    }

    @Override
    protected void updateDatabase(SQLiteDatabase database, int oldVersion,
                                  int newVersion) {
        database.execSQL(PINTable.dropStatement(PINTable.TABLE_NAME));
        database.execSQL(PINTable.dropIndexesStatement());
        database.execSQL(ClientDataTable.dropStatement(ClientDataTable.TABLE_NAME));
        database.execSQL(CustomValuesTable.dropStatement(CustomValuesTable.TABLE_NAME));
        database.execSQL(CustomValuesTable.dropIndexesStatement());
        database.execSQL(LocationTable.dropStatement(LocationTable.TABLE_NAME));
        onCreate(database);
    }

    public void clearAll() {
        execSQL(PINTable.emptyStatement(PINTable.TABLE_NAME), new Object[]{});
        execSQL(ClientDataTable.emptyStatement(ClientDataTable.TABLE_NAME), new Object[]{});
        execSQL(CustomValuesTable.emptyStatement(CustomValuesTable.TABLE_NAME), new Object[]{});
        execSQL(LocationTable.emptyStatement(LocationTable.TABLE_NAME), new Object[]{});
    }

}
