package com.icanvass.webservices;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.octo.android.robospice.request.SpiceRequest;
import com.icanvass.helpers.JsonHelper;
import com.icanvass.helpers.SPHelper;
import com.icanvass.objects.ListUsers;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by romek on 10.05.2014.
 */

public class UsersRequest extends SpiceRequest<JSONObject> {

    String mEmail;
    String mPassword;
    String mCompany;

    public UsersRequest(Context ctx) {
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
                "http://services.spotio.com:888/PinService.svc/UserProfileBasics()?$top=100&$expand=UserRoles&$format=json").buildUpon();

        String url = uriBuilder.build().toString();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                .openConnection();

        byte[] auth = (mCompany + "||" + mEmail + ":" + mPassword).getBytes();
        String basic = Base64.encodeToString(auth, Base64.NO_WRAP);
        urlConnection.setRequestProperty("Authorization", "Basic " + basic);

        String result=null;
        try {
            result = IOUtils.toString(urlConnection.getInputStream());
        } catch (Exception e) {
            Log.e("UsersRequest", IOUtils.toString(urlConnection.getErrorStream()));
            return null;
        }
        urlConnection.disconnect();

        JSONObject o = new JSONObject(result);
        getPermissions(o.optJSONArray("value"));

        ListUsers listUsers = JsonHelper.fromJson(o, ListUsers.class);
        ListUsers.save(listUsers);

        return o;
    }

    private void getPermissions(JSONArray users) {

        String roleid=null;
        for(int k=0;k<users.length();k++) {
            JSONObject u=users.optJSONObject(k);
            if(u.optString("UserName").equals(mEmail)) {
                JSONArray roles = u.optJSONArray("UserRoles");
                roleid=roles.optJSONObject(0).optString("RoleId");
                break;
            }
        }

        if(roleid==null) return;

        Uri.Builder uriBuilder = Uri.parse(
                "http://services.spotio.com:888/PinService.svc/PermissionItemBasics()?$filter=RoleId%20eq%20"+roleid+"&$top=100&$format=json").buildUpon();

        String url = uriBuilder.build().toString();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] auth = (mCompany + "||" + mEmail + ":" + mPassword).getBytes();
        String basic = Base64.encodeToString(auth, Base64.NO_WRAP);
        urlConnection.setRequestProperty("Authorization", "Basic " + basic);

        String result=null;
        try {
            result = IOUtils.toString(urlConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject o= null;
        try {
            o = new JSONObject(result);
            JSONArray a=o.optJSONArray("value");
            for(int i=0;i<a.length();++i) {
                JSONObject p=a.optJSONObject(i);
                if(p.optString("PermissionCode").equals("AllowedToShareReports")) {
                    SPHelper.getInstance().getSharer().edit().putString("sharing", "1").commit();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     * @return
     */
    public String createCacheKey() {
        return null;
    }
}