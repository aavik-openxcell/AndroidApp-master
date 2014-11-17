package com.icanvass.objects;

import java.util.ArrayList;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class PINObject {
    public String Id;
    public int FastId;
    public String RelatedJobId;
    public String DateTimeInputted;
    public String UserLocation;
    public String CreationDate;
    public String UserName;
    public String UpdateUserName;
    public String Notes;
    public String Status;
    public String CreationUserName;
    public String UpdateDate;
    public double UserCurrentLatitude;
    public double UserCurrentLongitude;
    public double Latitude;
    public double Longitude;

    public String StreetName;
    public String HouseNumber;

    public ArrayList<CustomValuesObject> CustomValues;
    public ClientDataObject ClientData;
    public LocationObject Location;
}
