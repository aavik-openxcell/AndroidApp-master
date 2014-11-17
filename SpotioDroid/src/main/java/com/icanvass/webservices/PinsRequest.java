package com.icanvass.webservices;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.icanvass.database.PINDAO;
import com.icanvass.helpers.SDDefine;
import com.icanvass.helpers.SPHelper;
import com.icanvass.objects.CustomValuesObject;
import com.icanvass.objects.LocationObject;
import com.icanvass.objects.PINObject;
import com.octo.android.robospice.request.SpiceRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by romek on 10.05.2014.
 */

public class PinsRequest extends SpiceRequest<JSONObject> {
    public static final String TAG = "PinsRequest";
    String mEmail;
    String mPassword;
    String mCompany;
    Context mCtx;

    public PinsRequest(Context ctx) {
        super(JSONObject.class);
        this.mCtx=ctx;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        mEmail = sharedPreferences.getString("EMAIL", null);
        mPassword = sharedPreferences.getString("PASSWORD", null);
        mCompany = sharedPreferences.getString("COMPANY", null);
    }

    @Override
    public JSONObject loadDataFromNetwork() throws Exception {
        if(mEmail==null) return null;
        //Log.i(TAG, "loadDataFromNetwork");
        int skip=0;
        int top=2500;
        while(true) {
            String refreshdate = SPHelper.getInstance().getDate("refresh");
            String urlstr = "http://services.spotio.com:888/PinService.svc/Pins?$format=json&$skip="+skip+"&$top="+top+"&$select=CustomValues,Id,Status,Location,UserName,Latitude,Longitude,CreationDate,UpdateDate&$orderby=CreationDate%20desc&$expand=CustomValues";
            if (refreshdate != null) {
                urlstr += "&$filter=CreationDate%20ge%20datetime%27" + refreshdate + "%27%20or%20UpdateDate%20ge%20datetime%27" + refreshdate + "%27";
            }
            Log.e(TAG,"pin url=>"+urlstr);
            Uri.Builder uriBuilder = Uri.parse(urlstr).buildUpon();

            String url = uriBuilder.build().toString();
            Date date = new Date();
            long start = date.getTime();
            Log.i(TAG, "start: " + date.toString());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }

            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                    .openConnection();

            byte[] auth = (mCompany + "||" + mEmail + ":" + mPassword).getBytes();
            String basic = Base64.encodeToString(auth, Base64.NO_WRAP);
            urlConnection.setRequestProperty("Authorization", "Basic " + basic);

            String result;
            try {
                result = IOUtils.toString(urlConnection.getInputStream());
            } catch (Exception e) {
                Log.e(TAG, IOUtils.toString(urlConnection.getErrorStream()));
                return null;
            }

            Log.e(TAG, url);
//            Log.d(TAG, "Basic " + basic);
//            Log.i(TAG, "stop: " + new Date().toString() + ", duration: " + (new Date().getTime() - start) + " ms");
            urlConnection.disconnect();


            JSONArray pins = pinsFromResult(result);
            Log.e(TAG, "pinsFromResult: " + pins.toString());
//            Log.i(TAG, "json: " + new Date().toString() + ", duration: " + (new Date().getTime() - start) + " ms");
            if(pins != null && pins.length() > 0) {
                addPins(pins);
                notifyAboutPins();
                skip += top;
            }else{
                break;
            }
        }
        SPHelper.getInstance().saveDate("refresh", SDDefine.serverFormat.format(new Date()));
        notifyAboutEnd();
        return new JSONObject();
    }

    private void notifyAboutPins() {
        Intent intent = new Intent();
        intent.setAction("com.spotio.Pins");
        mCtx.sendBroadcast(intent);
    }
    private void notifyAboutEnd() {
        Intent intent = new Intent();
        intent.setAction("com.spotio.PinsEnd");
        mCtx.sendBroadcast(intent);
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

    private void insertPin(JSONObject p) {


//        PINDAO.getInstance().insert(pinFromJSON(p));
    }

    private int addPins(JSONArray pins) {

//        for(int i=0; i<pins.length(); ++i) {
//            insertPin(pins.optJSONObject(i));
//        }
        PINDAO.getInstance().insert(pins);
        Log.e(TAG,"total pins lenght->"+pins.length());
        return pins.length();
    }

    private JSONArray pinsFromResult(String result) throws Exception {
        JSONObject response = new JSONObject(result);

        //SPHelper.getInstance().saveDate("refresh", SDDefine.serverFormat.format(new Date()));
        JSONArray pins=response.optJSONArray("value");



        return pins;
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     *
     * @return
     */
    public static String createCacheKey() {
        return "pins";
    }
}