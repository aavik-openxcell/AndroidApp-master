package com.icanvass.database;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class LocationTable extends SDTable {

    public static final String TABLE_NAME = "locations";

    public enum collumns {
        Id, PinId, HouseNumber, Street, Zip, State, Unit, City, Address, Country
    }

    public static String createStatement() {
        String listCollumns[] = new String[10];
        listCollumns[0] = collumns.Id.name() + INTEGER + PRIMARY_KEY + AUTOINCREMENT + COMMA;
        listCollumns[1] = collumns.PinId.name() + TEXT + COMMA;
        listCollumns[2] = collumns.HouseNumber.name() + INTEGER + COMMA;
        listCollumns[3] = collumns.Street.name() + TEXT + COMMA;
        listCollumns[4] = collumns.Zip.name() + TEXT + COMMA;
        listCollumns[5] = collumns.State.name() + TEXT + COMMA;
        listCollumns[6] = collumns.Unit.name() + TEXT + COMMA;
        listCollumns[7] = collumns.City.name() + TEXT + COMMA;
        listCollumns[8] = collumns.Address.name() + TEXT + COMMA;
        listCollumns[9] = collumns.Country.name() + TEXT;
        String collumns = "";
        for (int i = 0; i < listCollumns.length; i++) {
            collumns += listCollumns[i];
        }
        return String.format(CREATE_STATMENT, TABLE_NAME, collumns);
    }
}
