package com.icanvass.database;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class PINTable extends SDTable {

    public static final String TABLE_NAME = "pins";

    public enum collumns {
        _id, FastId, RelatedJobId, DateTimeInputted, UserLocation, CreationDate,
        UserName, UpdateUserName, Notes, Status, CreationUserName, UpdateDate,
        UserCurrentLatitude, UserCurrentLongitude, Latitude, Longitude, StreetName, HouseNumber;
    }

    public static String createStatement() {
        String listCollumns[] = new String[18];
        listCollumns[0] = collumns._id.name() + TEXT + PRIMARY_KEY + NOT_NULL + COMMA;
        listCollumns[1] = collumns.FastId.name() + INTEGER + COMMA;
        listCollumns[2] = collumns.DateTimeInputted.name() + TEXT + COMMA;
        listCollumns[3] = collumns.UserLocation.name() + TEXT + COMMA;
        listCollumns[4] = collumns.CreationDate.name() + TEXT + COMMA;
        listCollumns[5] = collumns.UserName.name() + TEXT + COMMA;
        listCollumns[6] = collumns.UpdateUserName.name() + TEXT + COMMA;
        listCollumns[7] = collumns.Notes.name() + TEXT + COMMA;
        listCollumns[8] = collumns.Status.name() + TEXT + COMMA;
        listCollumns[9] = collumns.CreationUserName.name() + TEXT + COMMA;
        listCollumns[10] = collumns.UpdateDate.name() + TEXT + COMMA;
        listCollumns[11] = collumns.UserCurrentLatitude.name() + REAL + COMMA;
        listCollumns[12] = collumns.UserCurrentLongitude.name() + REAL + COMMA;
        listCollumns[13] = collumns.RelatedJobId.name() + TEXT + COMMA;
        listCollumns[14] = collumns.Latitude.name() + REAL + COMMA;
        listCollumns[15] = collumns.Longitude.name() + REAL + COMMA;
        listCollumns[16] = collumns.StreetName.name() + TEXT + COMMA;
        listCollumns[17] = collumns.HouseNumber.name() + TEXT;


        String collumns = "";
        for (int i = 0; i < listCollumns.length; i++) {
            collumns += listCollumns[i];
        }
        return String.format(CREATE_STATMENT, TABLE_NAME, collumns);
    }

    public static String createIndexesStatement() {
        return "CREATE INDEX pins_index ON pins (_id)";
    }

    public static String dropIndexesStatement() {
        return "Drop INDEX pins_index";
    }
}
