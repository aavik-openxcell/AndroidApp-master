package com.icanvass.webservices;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.octo.android.robospice.request.SpiceRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by romek on 10.05.2014.
 */

public class FieldsRequest extends SpiceRequest<JSONObject> {

    String mEmail;
    String mPassword;
    String mCompany;

    public FieldsRequest(Context ctx) {
        super(JSONObject.class);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        mEmail = sharedPreferences.getString("EMAIL",null);
        mPassword = sharedPreferences.getString("PASSWORD",null);
        mCompany = sharedPreferences.getString("COMPANY",null);
    }

    @Override
    public JSONObject loadDataFromNetwork() throws Exception {
        if(mEmail==null) return null;
        Uri.Builder uriBuilder = Uri.parse(
                "http://services.spotio.com:888/PinService.svc/FieldDefinitions?$format=json").buildUpon();

        String url = uriBuilder.build().toString();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                .openConnection();

        byte[] auth = (mCompany + "||" + mEmail + ":" + mPassword).getBytes();
        String basic = Base64.encodeToString(auth, Base64.NO_WRAP);
        urlConnection.setRequestProperty("Authorization", "Basic " + basic);

        String result = IOUtils.toString(urlConnection.getInputStream());
        urlConnection.disconnect();

        return new JSONObject(result);
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     * @return
     */
    public String createCacheKey() {
        return "Fields";
    }
}