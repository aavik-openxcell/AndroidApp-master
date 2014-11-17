package com.icanvass.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.icanvass.objects.CustomValuesObject;

import java.util.ArrayList;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class CustomValuesDAO extends SDDAO<CustomValuesObject> {
    private static CustomValuesDAO instance;

    public CustomValuesDAO(Context context) {
        super(context);
    }

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new CustomValuesDAO(context);
        }
    }

    public static synchronized CustomValuesDAO getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    LocationDAO.class.getSimpleName()
                            + " is not initialized, call initializeInstance(..) method first."
            );
        }
        return instance;
    }

    @Override
    public ArrayList<CustomValuesObject> getAll() {
        return null;
    }

    @Override
    public CustomValuesObject get(String id) {
        return null;
    }

    public ArrayList<CustomValuesObject> getList(String pinId) {
        ArrayList<CustomValuesObject> list = new ArrayList<CustomValuesObject>();
        String sql = "SELECT * FROM " + CustomValuesTable.TABLE_NAME + " WHERE pinId = '" + pinId + "'";
        Cursor cursor = getDbHelper().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            CustomValuesObject object = parser(cursor);
            list.add(object);
        }
        cursor.close();
        getDbHelper().closeReadableDatabase();
        return list;
    }

    @Override
    public long insert(SQLiteDatabase database, CustomValuesObject customValuesObject) {

        ContentValues values = new ContentValues();
        values.put(CustomValuesTable.collumns.Id.name(), customValuesObject.Id);
        values.put(CustomValuesTable.collumns.PinId.name(), customValuesObject.PinId);
        values.put(CustomValuesTable.collumns.DefinitionId.name(), customValuesObject.DefinitionId);
        values.put(CustomValuesTable.collumns.StringValue.name(), customValuesObject.StringValue);
        values.put(CustomValuesTable.collumns.DecimalValue.name(), customValuesObject.DecimalValue);
        values.put(CustomValuesTable.collumns.DateTimeValue.name(), customValuesObject.DateTimeValue);
        values.put(CustomValuesTable.collumns.IntValue.name(), customValuesObject.IntValue);
//        values.put(CustomValuesTable.collumns.City.name(), customValuesObject.City);
//        values.put(CustomValuesTable.collumns.Address.name(), customValuesObject.Address);


        return getDbHelper().insert(database, CustomValuesTable.TABLE_NAME, null, values);
    }

    @Override
    public long update(CustomValuesObject customValuesObject) {
        return 0;
    }

    @Override
    public void delete(SQLiteDatabase database,String pinId) {
        getDbHelper().delete(database,CustomValuesTable.TABLE_NAME, "PinId = '"+pinId+"'",null);
    }

    @Override
    public CustomValuesObject parser(Cursor cursor) {
        CustomValuesObject o = null;

        if (cursor != null) {
            o = new CustomValuesObject();
            o.Id = cursor.getInt(cursor.getColumnIndex(CustomValuesTable.collumns.Id.name()));
            o.IntValue = cursor.getString(cursor.getColumnIndex(CustomValuesTable.collumns.IntValue.name()));
            o.DefinitionId = cursor.getInt(cursor.getColumnIndex(CustomValuesTable.collumns.DefinitionId.name()));
            o.DecimalValue = cursor.getString(cursor.getColumnIndex(CustomValuesTable.collumns.DecimalValue.name()));

            o.PinId = cursor.getString(cursor.getColumnIndex(CustomValuesTable.collumns.PinId.name()));
            o.StringValue = cursor.getString(cursor.getColumnIndex(CustomValuesTable.collumns.StringValue.name()));
            o.DateTimeValue = cursor.getString(cursor.getColumnIndex(CustomValuesTable.collumns.DateTimeValue.name()));
        }
        return o;
    }
}
