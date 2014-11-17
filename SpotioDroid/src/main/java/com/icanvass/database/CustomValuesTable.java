package com.icanvass.database;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class CustomValuesTable extends SDTable {

    public static final String TABLE_NAME = "customValues";

    public enum collumns {
        Id, PinId, DefinitionId, StringValue, DecimalValue, DateTimeValue, IntValue, City, Address
    }

    public static String createStatement() {
        String listCollumns[] = new String[10];
        listCollumns[0] = collumns.Id.name() + INTEGER + NOT_NULL + COMMA;
        listCollumns[1] = collumns.PinId.name() + TEXT + COMMA;
        listCollumns[2] = collumns.DefinitionId.name() + INTEGER + COMMA;
        listCollumns[3] = collumns.StringValue.name() + TEXT + COMMA;
        listCollumns[4] = collumns.DecimalValue.name() + INTEGER + COMMA;
        listCollumns[5] = collumns.DateTimeValue.name() + TEXT + COMMA;
        listCollumns[6] = collumns.City.name() + TEXT + COMMA;
        listCollumns[7] = collumns.Address.name() + TEXT + COMMA;
        listCollumns[8] = collumns.IntValue.name() + INTEGER + COMMA;
        listCollumns[9] = PRIMARY_KEY +" ("+collumns.Id.name() + COMMA + collumns.PinId.name() + ")";
        String collumns = "";
        for (int i = 0; i < listCollumns.length; i++) {
            collumns += listCollumns[i];
        }
        return String.format(CREATE_STATMENT, TABLE_NAME, collumns);
    }

    public static String createIndexesStatement() {
        return "CREATE INDEX customValues_index ON customValues (PinId)";
    }

    public static String dropIndexesStatement() {
        return "Drop INDEX customValues_index";
    }
}
