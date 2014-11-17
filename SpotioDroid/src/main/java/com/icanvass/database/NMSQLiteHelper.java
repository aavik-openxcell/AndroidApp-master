package com.icanvass.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class NMSQLiteHelper extends SQLiteOpenHelper {

	private AtomicInteger mWritableCounter = new AtomicInteger();
	private AtomicInteger mReadableCounter = new AtomicInteger();
	private SQLiteDatabase mWritableDatabase;
	private SQLiteDatabase mReadableDatabase;

	public NMSQLiteHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		initDatabase(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		updateDatabase(database, oldVersion, newVersion);
	}

	public synchronized SQLiteDatabase openWritableDatabase() {
		if (mWritableCounter.incrementAndGet() == 1) {
			// Opening new database for writing
			mWritableDatabase = getWritableDatabase();
		}
		return mWritableDatabase;
	}

	public synchronized SQLiteDatabase openReadableDatabase() {
		if (mReadableCounter.incrementAndGet() == 1) {
			// Opening new database for reading
			mReadableDatabase = getReadableDatabase();
		}
		return mReadableDatabase;
	}

	public synchronized void closeWritableDatabase() {
		if (mWritableCounter.decrementAndGet() == 0
				&& mReadableCounter.get() == 0) {
			// Closing database
			mWritableDatabase.close();
		}
	}

	public synchronized void closeReadableDatabase() {
		if (mReadableCounter.decrementAndGet() == 0
				&& mWritableCounter.get() == 0) {
			// Closing database
			mReadableDatabase.close();
		}
	}

	protected abstract void initDatabase(SQLiteDatabase database);

	protected abstract void updateDatabase(SQLiteDatabase database,
			int oldVersion, int newVersion);

	public Cursor rawSelect(String tableName, String[] tableColumns,
			String whereClase, String whereArgs[], String groupBy,
			String having, String orderBy) {
		Cursor cursor = null;
		SQLiteDatabase database = openReadableDatabase();
		database.beginTransaction();
		try {
			cursor = database.query(tableName, tableColumns, whereClase,
					whereArgs, groupBy, having, orderBy);
		} finally {
			database.endTransaction();
		}
		return cursor;
	}

	public Cursor rawQuery(String query, String[] selectionArgs) {
		Cursor cursor = null;
		SQLiteDatabase database = openReadableDatabase();
		database.beginTransaction();
		try {
			cursor = database.rawQuery(query, selectionArgs);
		} finally {
			database.endTransaction();
		}
		return cursor;
	}

	public ArrayList<ArrayList<String>> select(String tableName,
			String[] tableColumns, String whereClase, String whereArgs[],
			String groupBy, String having, String orderBy) {
		ArrayList<ArrayList<String>> retList = new ArrayList<ArrayList<String>>();
		ArrayList<String> list = new ArrayList<String>();

		SQLiteDatabase database = openReadableDatabase();
		database.beginTransaction();
		Cursor cursor = null;
		try {
			cursor = database.query(tableName, tableColumns, whereClase,
					whereArgs, groupBy, having, orderBy);
			if (cursor.moveToFirst()) {
				do {
					list = new ArrayList<String>();
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						list.add(cursor.getString(i));
					}
					retList.add(list);
				} while (cursor.moveToNext());
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			database.endTransaction();
			closeReadableDatabase();
		}

		return retList;

	}

	public ArrayList<ArrayList<String>> query(String query,
			String[] selectionArgs) {
		ArrayList<ArrayList<String>> retList = new ArrayList<ArrayList<String>>();
		ArrayList<String> list = new ArrayList<String>();
		SQLiteDatabase database = openReadableDatabase();
		database.beginTransaction();
		Cursor cursor = null;
		try {
			cursor = database.rawQuery(query, selectionArgs);
			if (cursor.moveToFirst()) {
				do {
					list = new ArrayList<String>();
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						list.add(cursor.getString(i));
					}
					retList.add(list);
				} while (cursor.moveToNext());
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			database.endTransaction();
			closeReadableDatabase();
		}

		return retList;
	}

	public long insert(SQLiteDatabase database, String tableName, String nullColumnHack,
			ContentValues initialValues) {

		long id = -1;
		try {
			id = database.insertWithOnConflict(tableName, nullColumnHack,
					initialValues, SQLiteDatabase.CONFLICT_REPLACE);
		} finally {
		}
		return id;
	}

	public boolean update(String tableName, ContentValues initialValues,
			String whereClause, String whereArgs[]) {

		SQLiteDatabase database = openWritableDatabase();
		database.beginTransaction();
		boolean success = false;
		try {
			success = database.update(tableName, initialValues, whereClause,
					whereArgs) > 0;
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		closeWritableDatabase();
		return success;
	}

	public int delete(SQLiteDatabase database,String tableName, String whereClause, String[] whereArgs) {

		int result = 0;
		try {
			result = database.delete(tableName, whereClause, whereArgs);
		} finally {
		}
		return result;
	}

	public void execSQL(String sql, Object[] bindArgs) {
		SQLiteDatabase database = openWritableDatabase();
		database.beginTransaction();
		try {
			database.execSQL(sql, bindArgs);
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}
		closeWritableDatabase();
	}

}
