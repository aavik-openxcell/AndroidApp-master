package com.icanvass.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public abstract class NMDAO<T> {

	public abstract ArrayList<T> getAll();

	public abstract T get(String id);

	public abstract long insert(SQLiteDatabase database,T t);

	public abstract long update(T t);

	public abstract void delete(SQLiteDatabase database,String id);

	public abstract T parser(Cursor cursor);

}
