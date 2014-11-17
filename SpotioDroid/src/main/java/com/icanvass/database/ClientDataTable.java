package com.icanvass.database;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class ClientDataTable extends SDTable {

    public static final String TABLE_NAME = "clients";

    public enum collumns {
        Id, pinId, AppointmentEventId, FirstName, LastName, Phone, Appointment, Email
    }

    public static String createStatement() {
        String listCollumns[] = new String[8];
        listCollumns[0] = collumns.Id.name() + INTEGER + PRIMARY_KEY + AUTOINCREMENT + COMMA;
        listCollumns[1] = collumns.pinId.name() + TEXT + COMMA;
        listCollumns[2] = collumns.AppointmentEventId.name() + TEXT + COMMA;
        listCollumns[3] = collumns.FirstName.name() + TEXT + COMMA;
        listCollumns[4] = collumns.LastName.name() + TEXT + COMMA;
        listCollumns[5] = collumns.Phone.name() + TEXT + COMMA;
        listCollumns[6] = collumns.Email.name() + TEXT + COMMA;
        listCollumns[7] = collumns.Appointment.name() + TEXT;
        String collumns = "";
        for (int i = 0; i < listCollumns.length; i++) {
            collumns += listCollumns[i];
        }
        return String.format(CREATE_STATMENT, TABLE_NAME, collumns);
    }
}
