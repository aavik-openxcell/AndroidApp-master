package com.icanvass.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.icanvass.objects.ListUsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public class CommonHelpers {

    public static String timeStampToTimeString(String timeStamp, SimpleDateFormat format) {
        Date date = new Date(Long.parseLong(timeStamp));
        return format.format(date);
    }

    public static String convertStringToAnotherFormat(String timeString, SimpleDateFormat oldFormat, SimpleDateFormat newFormat){
        String ret = null;
        try {
            Date date = oldFormat.parse(timeString);
            ret = newFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void hideKeyboard(Activity activity){
        InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null){
            keyboard.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showKeyboard(Activity activity, View v){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }
//    /**
//     * compareTimeInString
//     * @param firstString
//     * @param secondString
//     * @return true if firstString greater than secondString
//     */
//    public static final boolean compareTimeInString(String firstString, String secondString){
//        boolean ret = false;
//        try {
//            Date firstDate = SDDefine.serverFormat.parse(firstString);
//            Date secondDate = SDDefine.serverFormat.parse(secondString);
//            long fistLong = firstDate.getTime();
//            long secondLong = secondDate.getTime();
//             ret = (fistLong-secondLong)> 0;
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return ret;
//    }

    public static final ArrayList<JSONObject> getFilteredPins(List<JSONObject> pins, Context context) {
        JSONArray usersJSON = new JSONArray();
        JSONArray statusesJSON = new JSONArray();
        JSONArray datesJSON = new JSONArray();
        String start = null;
        String end = null;
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            usersJSON = new JSONArray(sharedPreferences.getString("FILTER_USERS", "[]"));
            statusesJSON = new JSONArray(sharedPreferences.getString("FILTER_STATUSES", "[]"));
            start = sharedPreferences.getString("FILTER_DATE_FROM", null);
            end = sharedPreferences.getString("FILTER_DATE_TO", null);
        } catch (JSONException e) {
        }

        List<String> users = new ArrayList<String>();
        for (int i = 0; i < usersJSON.length(); i++) {
            users.add(usersJSON.optString(i));
        }
        List<String> statuses = new ArrayList<String>();
        for (int i = 0; i < statusesJSON.length(); i++) {
            statuses.add(statusesJSON.optString(i));
        }

        ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
        for (JSONObject pin : pins) {
            boolean addstatus = false;
            if (statuses.isEmpty()) addstatus = true;
            else addstatus = statuses.contains(pin.optString("Status"));
            boolean adduser = false;
            if (users.isEmpty()) adduser = true;
            else adduser = users.contains(ListUsers.getUsernameByEmail(pin.optString("UserName")));
            String pindate = pin.optString("CreationDate");
            boolean addstart = false;
            if (start != null) {
                addstart = (pindate.compareTo(start) > 0);
            } else {
                addstart = true;
            }
            boolean addend = false;
            if (end != null) {
                addend = (pindate.compareTo(end) < 0);
            } else {
                addend = true;
            }
            if (addstatus && adduser && addstart && addend) {
                objects.add(pin);
            }
        }

        return objects;
    }

    public static final ArrayList<JSONObject> getSeachedPins(List<JSONObject> pins, String search_str) {

        ArrayList<JSONObject> objects = new ArrayList<JSONObject>();
        for (JSONObject pin : pins) {
            boolean adduser = false;
            if(search_str == "")
                adduser = true;
            else {
                JSONObject location = pin.optJSONObject("Location");
                if (location != null) {
                    String houseNumber = location.optString("HouseNumber");
                    String street = location.optString("Street");
                    String address = "";
                    if (!houseNumber.equalsIgnoreCase("null")) {
                        address += houseNumber + " ";
                    }
                    if (!street.equalsIgnoreCase("null")) {
                        address += street + " ";
                    }
                    if (!location.optString("City").equalsIgnoreCase("null")) {
                        address += location.optString("City") + " ";
                    }
                    if (!location.optString("State").equalsIgnoreCase("null")) {
                        address += location.optString("State") + " ";
                    }
                    if (!location.optString("Zip").equalsIgnoreCase("null")) {
                        address += location.optString("Zip");
                    }

                    adduser = address.toLowerCase().contains(search_str.toLowerCase());
                }
            }

            if (adduser) {
                objects.add(pin);
            }
        }

        return objects;
    }
}
