package com.icanvass.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.VisibleRegion;
import com.icanvass.adapters.ILocationReceivedListener;
import com.icanvass.objects.CustomValuesObject;
import com.icanvass.objects.LocationObject;
import com.icanvass.objects.PINObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class PINDAO extends SDDAO<PINObject> {
    private static PINDAO instance;
    Context c;

    public PINDAO(Context context) {

        super(context);
        c = context;
    }

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new PINDAO(context);
        }
    }

    public static synchronized PINDAO getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    PINDAO.class.getSimpleName()
                            + " is not initialized, call initializeInstance(..) method first."
            );
        }
        return instance;
    }

    @Override
    public ArrayList<PINObject> getAll() {
        ArrayList<PINObject> pins = new ArrayList<PINObject>();
        String sql = "SELECT * FROM " + PINTable.TABLE_NAME;
        Cursor c = getDbHelper().rawQuery(sql, null);
        if (c != null){
            while (c.moveToNext()){
                pins.add(parser(c));
            }
            c.close();
        }

        return pins;
    }

    @Override
    public PINObject get(String id) {
        PINObject object = null;
        String sql = "SELECT * FROM " + PINTable.TABLE_NAME + " WHERE _id = '" + id + "'";
        Cursor cursor = getDbHelper().rawQuery(sql, null);//new String[]{id});
        while (cursor.moveToNext()) {
            object = parser(cursor);
        }
        cursor.close();
        getDbHelper().closeReadableDatabase();
        return object;
    }

    @Override
    public long insert(SQLiteDatabase database,PINObject pinObject) {
//        ClientDataObject clientDataObject = pinObject.ClientData;
//        LocationObject locationObject = pinObject.Location;
//        ArrayList<CustomValuesObject> customValuesObject = pinObject.CustomValues;
//
////        ClientDataDAO.getInstance().insert(clientDataObject);
//        LocationDAO.getInstance().insert(locationObject);
//        CustomValuesDAO.getInstance().delete(pinObject.Id);
//        for (CustomValuesObject object : customValuesObject)
//            CustomValuesDAO.getInstance().insert(object);
//
//        ContentValues values = new ContentValues();
//        values.put(PINTable.collumns._id.name(), pinObject.Id);
//        values.put(PINTable.collumns.FastId.name(), pinObject.FastId);
//        values.put(PINTable.collumns.RelatedJobId.name(), pinObject.RelatedJobId);
//        values.put(PINTable.collumns.DateTimeInputted.name(), pinObject.DateTimeInputted);
//        values.put(PINTable.collumns.UserLocation.name(), pinObject.UserLocation);
//        values.put(PINTable.collumns.CreationDate.name(), pinObject.CreationDate);
//        values.put(PINTable.collumns.UserName.name(), pinObject.UserName);
//        values.put(PINTable.collumns.UpdateUserName.name(), pinObject.UpdateUserName);
//        values.put(PINTable.collumns.Notes.name(), pinObject.Notes);
//        values.put(PINTable.collumns.Status.name(), pinObject.Status);
//        values.put(PINTable.collumns.CreationUserName.name(), pinObject.CreationUserName);
//        values.put(PINTable.collumns.UpdateDate.name(), pinObject.UpdateDate);
//        values.put(PINTable.collumns.UserCurrentLatitude.name(), pinObject.UserCurrentLatitude);
//        values.put(PINTable.collumns.UserCurrentLongitude.name(), pinObject.UserCurrentLongitude);
//        values.put(PINTable.collumns.Latitude.name(), pinObject.Latitude);
//        values.put(PINTable.collumns.Longitude.name(), pinObject.Longitude);
//        values.put(PINTable.collumns.StreetName.name(), pinObject.StreetName);
//        values.put(PINTable.collumns.HouseNumber.name(), pinObject.HouseNumber);
//        return getDbHelper().insert(PINTable.TABLE_NAME, null, values);
        return -1;
    }

    public static PINObject pinFromJSON(JSONObject p) {
        PINObject pin = new PINObject();
        /*
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
        public String UserCurrentLatitude;
        public String UserCurrentLongitude;
        public String Latitude;
        public String Longitude;

        public ArrayList<CustomValuesObject> CustomValues;
        public ClientDataObject ClientData;
        public LocationObject Location;
         */
        pin.Id=p.optString("Id");
        pin.CreationDate=p.optString("CreationDate");
        pin.UserName=p.optString("UserName");
        pin.Status=p.optString("Status");
        String ud=p.optString("UpdateDate");
        if(ud.equals("")||ud.equals("null")){
            ud=pin.CreationDate;
        }
        pin.UpdateDate=ud;
        pin.UserCurrentLatitude=p.optDouble("UserCurrentLatitude");
        pin.UserCurrentLongitude=p.optDouble("UserCurrentLongitude");
        pin.Latitude=p.optDouble("Latitude");
        pin.Longitude=p.optDouble("Longitude");

        JSONArray cvs=p.optJSONArray("CustomValues");
        if(cvs!=null) {
            ArrayList<CustomValuesObject> cvList = new ArrayList<CustomValuesObject>();
            for(int j=0; j<cvs.length(); ++j) {
                CustomValuesObject cv=new CustomValuesObject();
                        /*
                        public String Id, pinId, DefinitionId, StringValue, DecimalValue, DateTimeValue, IntValue
                         */
                JSONObject c=cvs.optJSONObject(j);
                cv.Id=c.optInt("Id");
                cv.PinId=c.optString("PinId");
                cv.DefinitionId=c.optInt("DefinitionId");
                cv.StringValue=c.optString("StringValue");
                cv.DecimalValue=c.optString("DecimalValue");
                cv.DateTimeValue=c.optString("DateTimeValue");
                cv.IntValue=c.optString("IntValue");
                cvList.add(cv);
            }
            pin.CustomValues=cvList;
        }

        JSONObject loc=p.optJSONObject("Location");
        LocationObject location=new LocationObject();
                /*
                public String Id;
                public String pinId;
                public int HouseNumber;
                public String Street;
                public String Zip;
                public String State;
                public String Unit;
                public String City;
                public String Address;
                 */
        //location.Id=pin.Id;
        location.PinId=pin.Id;
        location.HouseNumber=loc.optInt("HouseNumber");
        location.Street=loc.optString("Street");
        location.Zip=loc.optString("Zip");
        location.Street=loc.optString("Street");
        location.State=loc.optString("State");
        location.Unit=loc.optString("Unit");
        location.City=loc.optString("City");
        location.Address=loc.optString("Address");
        location.Country=loc.optString("Country");
        pin.Location=location;

        pin.StreetName=location.Street;
        pin.HouseNumber=""+location.HouseNumber;

        return pin;
    }

    public ContentValues getContenValues(PINObject pinObject) {

//        ClientDataObject clientDataObject = pinObject.ClientData;


        ContentValues values = new ContentValues();
        values.put(PINTable.collumns._id.name(), pinObject.Id);
        values.put(PINTable.collumns.FastId.name(), pinObject.FastId);
        values.put(PINTable.collumns.RelatedJobId.name(), pinObject.RelatedJobId);
        values.put(PINTable.collumns.DateTimeInputted.name(), pinObject.DateTimeInputted);
        values.put(PINTable.collumns.UserLocation.name(), pinObject.UserLocation);
        values.put(PINTable.collumns.CreationDate.name(), pinObject.CreationDate);
        values.put(PINTable.collumns.UserName.name(), pinObject.UserName);
        values.put(PINTable.collumns.UpdateUserName.name(), pinObject.UpdateUserName);
        values.put(PINTable.collumns.Notes.name(), pinObject.Notes);
        values.put(PINTable.collumns.Status.name(), pinObject.Status);
        values.put(PINTable.collumns.CreationUserName.name(), pinObject.CreationUserName);
        values.put(PINTable.collumns.UpdateDate.name(), pinObject.UpdateDate);
        values.put(PINTable.collumns.UserCurrentLatitude.name(), pinObject.UserCurrentLatitude);
        values.put(PINTable.collumns.UserCurrentLongitude.name(), pinObject.UserCurrentLongitude);
        values.put(PINTable.collumns.Latitude.name(), pinObject.Latitude);
        values.put(PINTable.collumns.Longitude.name(), pinObject.Longitude);
        values.put(PINTable.collumns.StreetName.name(), pinObject.StreetName);
        values.put(PINTable.collumns.HouseNumber.name(), pinObject.HouseNumber);

        return values;
    }

    public long insert(JSONArray pins) {
        long start = new Date().getTime();
        SQLiteDatabase database = getDbHelper().openWritableDatabase();
        database.beginTransaction();
        long id = -1;
        try {
            SQLiteStatement locationsStatement = database.compileStatement(
                    "INSERT or REPLACE into locations (PinId, HouseNumber, Street, Zip, State, Unit, City, Address, Country) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

            SQLiteStatement customValuesStatement = database.compileStatement(
                    "INSERT into customValues (Id, PinId, DefinitionId, StringValue, DecimalValue, DateTimeValue, IntValue) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)");
            SQLiteStatement customValuesDeleteStatement = database.compileStatement(
                    "DELETE from customValues WHERE PinId = ?");


            SQLiteStatement pinsStatement = database.compileStatement(
                    "INSERT or REPLACE into pins (_id, DateTimeInputted, UserLocation, CreationDate, UserName, " +
                            "Status, UpdateDate, UserCurrentLatitude, UserCurrentLongitude, Latitude, Longitude, " +
                            "StreetName, HouseNumber) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            File gpxfile = new File(root, "SpotioTxt");
            FileWriter writer = null;
            try {
                 writer =  new FileWriter(gpxfile);
            }catch (IOException e) {
                e.printStackTrace();

            }
            for(int i=0; i<pins.length(); ++i) {
                PINObject pinObject=pinFromJSON(pins.optJSONObject(i));

                LocationObject locationObject = pinObject.Location;
                ArrayList<CustomValuesObject> customValuesObject = pinObject.CustomValues;

//                locationsStatement.clearBindings();
                locationsStatement.bindString(1, locationObject.PinId);
                locationsStatement.bindString(2, "" + locationObject.HouseNumber);
                locationsStatement.bindString(3, locationObject.Street);
                locationsStatement.bindString(4, locationObject.Zip);
                locationsStatement.bindString(5, locationObject.State);
                locationsStatement.bindString(6, locationObject.Unit);
                locationsStatement.bindString(7, locationObject.City);
                locationsStatement.bindString(8, locationObject.Address);
                locationsStatement.bindString(9, locationObject.Country);
                locationsStatement.execute();

//                CustomValuesDAO.getInstance().delete(database,pinObject.Id);
                customValuesDeleteStatement.bindString(1,pinObject.Id);
                customValuesDeleteStatement.execute();

                for (CustomValuesObject object : customValuesObject) {
                    customValuesStatement.bindString(1,""+object.Id);
                    customValuesStatement.bindString(2,object.PinId);
                    customValuesStatement.bindString(3,""+object.DefinitionId);
                    customValuesStatement.bindString(4,object.StringValue);
                    customValuesStatement.bindString(5,""+object.DecimalValue);
                    customValuesStatement.bindString(6,object.DateTimeValue);
                    customValuesStatement.bindString(7,""+object.IntValue);
                    customValuesStatement.execute();
                }

                pinsStatement.bindString(1,pinObject.Id);
                pinsStatement.bindString(2,"");
                pinsStatement.bindString(3,"");
                pinsStatement.bindString(4,pinObject.CreationDate);
                pinsStatement.bindString(5,pinObject.UserName);
                pinsStatement.bindString(6,pinObject.Status);
                pinsStatement.bindString(7,pinObject.UpdateDate);
                pinsStatement.bindString(8,""+0);
                pinsStatement.bindString(9,""+0);
                pinsStatement.bindString(10,""+pinObject.Latitude);
                pinsStatement.bindString(11,""+pinObject.Longitude);
                pinsStatement.bindString(12,pinObject.StreetName);
                pinsStatement.bindString(13,pinObject.HouseNumber);
                pinsStatement.execute();

                String text = "pin id=>" +pinObject.Id +",lat=>" +pinObject.Latitude+",long=>"+pinObject.Longitude;

                try
                {

                    if (!root.exists()) {
                        root.mkdirs();
                    }

                    Log.e("PinsRequest" , "=>" +text);
                    writer.append(text+"\n");


                }
                catch (IOException e) {
                    e.printStackTrace();

                }

            }

            try
            {

                writer.flush();
                writer.close();


            }
            catch (IOException e) {
                e.printStackTrace();

            }


            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        getDbHelper().closeWritableDatabase();

        Log.i("mPinsRequest", "duration: " + (new Date().getTime() - start) + " ms");
        return id;

    }

   /* public void generateNoteOnSD(String sBody){
        try
        {

            if (!root.exists()) {
                root.mkdirs();
            }

            if(writer==null){
                writer = new FileWriter(gpxfile);
            }

            Log.e("PinsRequest" , "=>" +sBody);
            writer.append(sBody);
            writer.flush();
            writer.close();
//            Toast.makeText(c, "Saved", Toast.LENGTH_SHORT).show();
        }
         catch (IOException e) {
            e.printStackTrace();
//             importError = e.getMessage();
//             iError();
        }
    }*/

    @Override
    public long update(PINObject pinObject) {
        return 0;
    }

    @Override
    public void delete(SQLiteDatabase database,String id) {

    }

    public String getId(Cursor cursor){
        return cursor.getString( cursor.getColumnIndex( PINTable.collumns._id.name() ) );
    }

    /**
     * Parsing only fields that needed to fill item in List Tab
     * @param cursor cursor
     * @return PINObject with fields
     */
    public PINObject parseFields(Cursor cursor, int position, ILocationReceivedListener listener){
        PINObject pinObject = null;

        if (cursor != null) {
            pinObject = new PINObject();
            pinObject.Id                    = cursor.getString(cursor.getColumnIndex(PINTable.collumns._id.name()));
            pinObject.Status                = cursor.getString(cursor.getColumnIndex(PINTable.collumns.Status.name()));
            pinObject.UpdateDate            = cursor.getString(cursor.getColumnIndex(PINTable.collumns.UpdateDate.name()));
            new GetLocationTask(pinObject, position, listener).execute();
        }

        return pinObject;
    }

    private class GetLocationTask extends AsyncTask<Void, Void, LocationObject>{

        private PINObject mPINObject;
        private ILocationReceivedListener mLocationReceivedListener;
        private int mPosition;

        private GetLocationTask(PINObject obj, int position, ILocationReceivedListener listener) {
            mPINObject = obj;
            mPosition = position;
            mLocationReceivedListener = listener;
        }

        @Override
        protected LocationObject doInBackground(Void... params) {
            return LocationDAO.getInstance().get(mPINObject.Id);
        }

        @Override
        protected void onPostExecute(LocationObject locationObject) {
            mPINObject.Location = locationObject;
            mLocationReceivedListener.onLocationReceived(locationObject, mPosition);
        }
    }


    public static MapsMarkerCursorIds matchMapMarkerCursorIds(Cursor cursor){
        return new MapsMarkerCursorIds(cursor);
    }

    public static Cursor getMapMarkersCursor (String where, VisibleRegion region){

        Log.e("MapFragment","PINDAO,where=>"+ where);
//        Log.e("MapFragment","PINDAO,pintable value=>"+ (""+(region.nearLeft.latitude-0.01)+","+""+(region.farRight.latitude+0.01)+","+""+(region.nearLeft.longitude-0.01)+","+""+(region.farRight.longitude+0.01)));
        Log.e("MapFragment","PINDAO,pintable value=>"+ (""+(region.nearLeft.latitude)+","+""+(region.farRight.latitude)+","+""+(region.nearLeft.longitude)+","+""+(region.farRight.longitude)));

        return PINDAO.getInstance().getDbHelper().rawSelect(PINTable.TABLE_NAME, new String[]{
                        PINTable.collumns._id.name(), PINTable.collumns.Status.name(), PINTable.collumns.Latitude.name(), PINTable.collumns.Longitude.name()
                },
                where,
//                new String[] {""+(region.nearLeft.latitude-0.01),""+(region.farRight.latitude+0.01),""+(region.nearLeft.longitude-0.01),""+(region.farRight.longitude+0.01)},
                new String[] {""+(region.nearLeft.latitude),""+(region.farRight.latitude),""+(region.nearLeft.longitude),""+(region.farRight.longitude)},
                null,null,null);
    }

    public static class MapsMarkerCursorIds {
        int id;
        int status;
        int latitude;
        int longtitude;

        public MapsMarkerCursorIds(Cursor cursor){
            id = cursor.getColumnIndex(PINTable.collumns._id.name());
            status = cursor.getColumnIndex(PINTable.collumns.Status.name());
            latitude = cursor.getColumnIndex(PINTable.collumns.Latitude.name());
            longtitude = cursor.getColumnIndex(PINTable.collumns.Longitude.name());
        }
    }

    public PINObject getMapMarkerInfo(Cursor cursor, MapsMarkerCursorIds fields){
        PINObject mapMarkerObject = null;
        if (cursor != null) {
            mapMarkerObject = new PINObject();
            mapMarkerObject.Id = cursor.getString(fields.id);
            mapMarkerObject.Status = cursor.getString(fields.status);
            mapMarkerObject.Latitude = cursor.getDouble(fields.latitude);
            mapMarkerObject.Longitude = cursor.getDouble(fields.longtitude);
        }
        return mapMarkerObject;
    }

    @Override
    public PINObject parser(Cursor cursor) {
        PINObject pinObject = null;

        if (cursor != null) {
            pinObject = new PINObject();
            pinObject.Id                    = cursor.getString(cursor.getColumnIndex(PINTable.collumns._id.name()));
            pinObject.FastId                = cursor.getInt(cursor.getColumnIndex(PINTable.collumns.FastId.name()));
            pinObject.RelatedJobId          = cursor.getString(cursor.getColumnIndex(PINTable.collumns.RelatedJobId.name()));
            pinObject.DateTimeInputted      = cursor.getString(cursor.getColumnIndex(PINTable.collumns.DateTimeInputted.name()));
            pinObject.UserLocation          = cursor.getString(cursor.getColumnIndex(PINTable.collumns.UserLocation.name()));
            pinObject.CreationDate          = cursor.getString(cursor.getColumnIndex(PINTable.collumns.CreationDate.name()));
            pinObject.UserName              = cursor.getString(cursor.getColumnIndex(PINTable.collumns.UserName.name()));
            pinObject.UpdateUserName        = cursor.getString(cursor.getColumnIndex(PINTable.collumns.UpdateUserName.name()));
            pinObject.Notes                 = cursor.getString(cursor.getColumnIndex(PINTable.collumns.Notes.name()));
            pinObject.Status                = cursor.getString(cursor.getColumnIndex(PINTable.collumns.Status.name()));
            pinObject.CreationUserName      = cursor.getString(cursor.getColumnIndex(PINTable.collumns.CreationUserName.name()));
            pinObject.UpdateDate            = cursor.getString(cursor.getColumnIndex(PINTable.collumns.UpdateDate.name()));
            pinObject.UserCurrentLatitude   = cursor.getDouble(cursor.getColumnIndex(PINTable.collumns.UserCurrentLatitude.name()));
            pinObject.UserCurrentLongitude  = cursor.getDouble(cursor.getColumnIndex(PINTable.collumns.UserCurrentLongitude.name()));
            pinObject.Latitude              = cursor.getDouble(cursor.getColumnIndex(PINTable.collumns.Latitude.name()));
            pinObject.Longitude             = cursor.getDouble(cursor.getColumnIndex(PINTable.collumns.Longitude.name()));

            pinObject.CustomValues          = CustomValuesDAO.getInstance().getList(pinObject.Id);
            pinObject.ClientData            = ClientDataDAO.getInstance().get(pinObject.Id);
            pinObject.Location              = LocationDAO.getInstance().get(pinObject.Id);
        }
        return pinObject;
    }
}
