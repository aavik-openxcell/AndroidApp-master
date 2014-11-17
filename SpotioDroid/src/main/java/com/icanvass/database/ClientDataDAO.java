package com.icanvass.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.icanvass.objects.ClientDataObject;

import java.util.ArrayList;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class ClientDataDAO extends SDDAO<ClientDataObject> {
    private static ClientDataDAO instance;

    public ClientDataDAO(Context context) {
        super(context);
    }

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new ClientDataDAO(context);
        }
    }

    public static synchronized ClientDataDAO getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    ClientDataDAO.class.getSimpleName()
                            + " is not initialized, call initializeInstance(..) method first."
            );
        }
        return instance;
    }

    @Override
    public ArrayList<ClientDataObject> getAll() {
        return null;
    }

    @Override
    public ClientDataObject get(String id) {
        ClientDataObject object = null;
        String sql = "SELECT * FROM " + ClientDataTable.TABLE_NAME + " WHERE pinId = '" + id + "'";
        Cursor cursor = getDbHelper().rawQuery(sql, null);//new String[]{id});
        while (cursor.moveToNext()) {
            object = parser(cursor);
        }
        cursor.close();
        getDbHelper().closeReadableDatabase();
        return object;
    }

    @Override
    public long insert(SQLiteDatabase database,ClientDataObject clientDataObject) {
        ContentValues values = new ContentValues();
        values.put(ClientDataTable.collumns.Id.name(), clientDataObject.Id);
        values.put(ClientDataTable.collumns.pinId.name(), clientDataObject.pinId);
        values.put(ClientDataTable.collumns.AppointmentEventId.name(), clientDataObject.AppointmentEventId);
        values.put(ClientDataTable.collumns.FirstName.name(), clientDataObject.FirstName);
        values.put(ClientDataTable.collumns.LastName.name(), clientDataObject.LastName);
        values.put(ClientDataTable.collumns.Phone.name(), clientDataObject.Phone);
        values.put(ClientDataTable.collumns.Appointment.name(), clientDataObject.Appointment);
        values.put(ClientDataTable.collumns.Email.name(), clientDataObject.Email);
        return getDbHelper().insert(database,ClientDataTable.TABLE_NAME, null, values);
    }

    @Override
    public long update(ClientDataObject clientDataObject) {
        return 0;
    }

    @Override
    public void delete(SQLiteDatabase database,String id) {

    }

    @Override
    public ClientDataObject parser(Cursor cursor) {
        return null;
    }
}

