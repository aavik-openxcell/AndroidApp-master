package com.icanvass.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.icanvass.objects.LocationObject;

import java.util.ArrayList;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class LocationDAO extends SDDAO<LocationObject> {
    private static LocationDAO instance;

    public LocationDAO(Context context) {
        super(context);
    }

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new LocationDAO(context);
        }
    }

    public static synchronized LocationDAO getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    LocationDAO.class.getSimpleName()
                            + " is not initialized, call initializeInstance(..) method first."
            );
        }
        return instance;
    }

    @Override
    public ArrayList<LocationObject> getAll() {
        return null;
    }

    @Override
    public LocationObject get(String id) {
        LocationObject object = null;
        String sql = "SELECT * FROM " + LocationTable.TABLE_NAME + " WHERE PinId = '" + id + "'";
        Cursor cursor = getDbHelper().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            object = parser(cursor);
        }
        cursor.close();
        getDbHelper().closeReadableDatabase();
        return object;
    }

    @Override
    public long insert(SQLiteDatabase database,LocationObject locationObject) {
//        ContentValues values = new ContentValues();
//        //values.put(LocationTable.collumns.Id.name(), locationObject.Id);
//        values.put(LocationTable.collumns.PinId.name(), locationObject.PinId);
//        values.put(LocationTable.collumns.HouseNumber.name(), locationObject.HouseNumber);
//        values.put(LocationTable.collumns.Street.name(), locationObject.Street);
//        values.put(LocationTable.collumns.Zip.name(), locationObject.Zip);
//        values.put(LocationTable.collumns.State.name(), locationObject.State);
//        values.put(LocationTable.collumns.Unit.name(), locationObject.Unit);
//        values.put(LocationTable.collumns.City.name(), locationObject.City);
//        values.put(LocationTable.collumns.Address.name(), locationObject.Address);
//        values.put(LocationTable.collumns.Country.name(), locationObject.Country);

        SQLiteStatement stmt = database.compileStatement(
                "INSERT or REPLACE into location (PinId, HouseNumber, Street, Zip, State, Unit, City, Address, Country)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        stmt.bindString(1, locationObject.PinId);
        stmt.bindString(2, ""+locationObject.HouseNumber);
        stmt.bindString(3, locationObject.Street);
        stmt.bindString(4, locationObject.Zip);
        stmt.bindString(5, locationObject.State);
        stmt.bindString(6, locationObject.Unit);
        stmt.bindString(7, locationObject.City);
        stmt.bindString(8, locationObject.Address);
        stmt.bindString(9, locationObject.Country);
        stmt.execute();

        return -1;//getDbHelper().insert(database, LocationTable.TABLE_NAME, null, values);
    }

    @Override
    public long update(LocationObject locationObject) {
        return 0;
    }

    @Override
    public void delete(SQLiteDatabase database,String id) {

    }

    @Override
    public LocationObject parser(Cursor cursor) {
        LocationObject l = null;

        if (cursor != null) {
            l = new LocationObject();
            l.Id = cursor.getInt(cursor.getColumnIndex(LocationTable.collumns.Id.name()));
            l.PinId = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.PinId.name()));
            l.HouseNumber = cursor.getInt(cursor.getColumnIndex(LocationTable.collumns.HouseNumber.name()));
            l.Street = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.Street.name()));
            l.Zip = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.Zip.name()));
            l.State = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.State.name()));
            l.Unit = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.Unit.name()));
            l.City = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.City.name()));
            l.Address = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.Address.name()));
            l.Country = cursor.getString(cursor.getColumnIndex(LocationTable.collumns.Country.name()));
        }
        return l;
    }
}
