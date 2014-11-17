package com.icanvass.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.icanvass.objects.FilterObj;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Justin CAO on 6/3/2014.
 */
public class SPHelper {
    private static SPHelper instance;
    private SharedPreferences sharedPreferences;

    public SPHelper(Context context) {
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new SPHelper(context);
        }
    }

    public static synchronized SPHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    SPHelper.class.getSimpleName()
                            + " is not initialized, call initializeInstance(..) method first."
            );
        }

        return instance;
    }

    public void saveCache(String key, ArrayList<JSONObject> value) {
//        cacheJson tempJson = new cacheJson();
//        tempJson.value = value;
        String json = new JSONArray(value).toString();
        sharedPreferences.edit().putString(key, json).commit();
    }

    public ArrayList<JSONObject> getCache(String key) {
        String cached = sharedPreferences.getString(key, null);
        try {
            ArrayList<JSONObject> listdata = new ArrayList<JSONObject>();
            JSONArray jArray = new JSONArray(cached);
            if (jArray != null) {
                for (int i=0;i<jArray.length();i++){
                    listdata.add(jArray.getJSONObject(i));
                }
            }
            return listdata;
        } catch (Exception e) {
            return null;
        }
    }

    public String getDate(String key) {
        String cached = sharedPreferences.getString(key, null);
        return cached;
    }

    public void saveDate(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    public void clearCache(String key) {
        sharedPreferences.edit().remove(key).commit();
    }

    private class cacheJson {
        public ArrayList<JSONObject> value;
    }

    public void clearFilter() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SDDefine.FILTER_STATUSES);
        editor.remove(SDDefine.FILTER_DATE_FROM);
        editor.remove(SDDefine.FILTER_DATE_TO);
        editor.remove(SDDefine.FILTER_USERS);
        editor.commit();
    }

    public void saveFilter(FilterObj filterObj) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SDDefine.FILTER_STATUSES, filterObj.getStatuses());
        editor.putString(SDDefine.FILTER_DATE_FROM, filterObj.getDateFrom());
        editor.putString(SDDefine.FILTER_DATE_TO, filterObj.getDateTo());
        editor.putString(SDDefine.FILTER_USERS, filterObj.getUseres());
        editor.commit();
    }

    public FilterObj getFilter() {
        String statuses = sharedPreferences.getString(SDDefine.FILTER_STATUSES, null);
        String dateFrom = sharedPreferences.getString(SDDefine.FILTER_DATE_FROM, null);
        String dateTo = sharedPreferences.getString(SDDefine.FILTER_DATE_TO, null);
        String useres = sharedPreferences.getString(SDDefine.FILTER_USERS, null);
        return new FilterObj(statuses, dateFrom, dateTo, useres);
    }

    public SharedPreferences getSharer() {
        return sharedPreferences;
    }
}
