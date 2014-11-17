package com.icanvass.webservices;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Justin CAO on 6/5/2014.
 */
public class SaveRequest extends SpiceRequest<String> {

    private JSONObject mParams;
    String mEmail;
    String mPassword;
    String mCompany;

    public SaveRequest(JSONObject params, Context ctx) {
        super(String.class);
        mParams = params;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
        mEmail = sharedPreferences.getString("EMAIL", null);
        mPassword = sharedPreferences.getString("PASSWORD", null);
        mCompany = sharedPreferences.getString("COMPANY", null);
    }

    @Override
    public String loadDataFromNetwork() throws Exception {

        // With Uri.Builder class we can build our url is a safe manner
        Uri.Builder uriBuilder = Uri.parse(
                "http://services.spotio.com:888/PinService.svc/Pins?$format=json").buildUpon();

        String url = uriBuilder.build().toString();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                .openConnection();

        byte[] auth = (mCompany + "||" + mEmail + ":" + mPassword).getBytes();
        String basic = Base64.encodeToString(auth, Base64.NO_WRAP);
        urlConnection.setRequestProperty("Authorization", "Basic " + basic);

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        urlConnection.connect();
//Create JSONObject here


// Send POST output.
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
        bw.write(mParams.toString());
        bw.flush();
        bw.close();

        String result=null;
        try {
            result = IOUtils.toString(urlConnection.getInputStream());
        }catch (Exception e) {
            Log.e("add", IOUtils.toString(urlConnection.getErrorStream()));
        }
        urlConnection.disconnect();

        return result;
    }

}
