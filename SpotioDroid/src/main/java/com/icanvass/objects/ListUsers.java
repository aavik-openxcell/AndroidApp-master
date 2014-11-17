package com.icanvass.objects;

import com.icanvass.helpers.JsonHelper;
import com.icanvass.helpers.SDDefine;
import com.icanvass.helpers.SPHelper;

import java.util.ArrayList;

/**
 * Created by Justin CAO on 6/3/2014.
 */
public class ListUsers {
    public ArrayList<UserObject> value;

    public static void save(ListUsers listUsers) {
        SPHelper.getInstance().getSharer().edit().putString(SDDefine.LIST_USRS_KEY, JsonHelper.toJson(listUsers)).commit();
    }

    public static ArrayList<UserObject> load() {
        return JsonHelper.fromJson(SPHelper.getInstance().getSharer().getString(SDDefine.LIST_USRS_KEY, ""), ListUsers.class).value;
    }

    public static ArrayList<String> getListUserName() {
        ArrayList<String> listUserNames = new ArrayList<String>();
        ArrayList<UserObject> listUser = load();
        for (UserObject object : listUser) {
            listUserNames.add(object.getFullName());
        }
        return listUserNames;
    }

    public static ArrayList<String> getListUserEmail() {
        ArrayList<String> listUserNames = new ArrayList<String>();
        ArrayList<UserObject> listUser = load();
        for (UserObject object : listUser) {
            listUserNames.add(object.EmailAddress);
        }
        return listUserNames;
    }

    public static String getUsernameByEmail(String userEmail){
        ArrayList<String> listUserNames = new ArrayList<String>();
        ArrayList<UserObject> listUser = load();
        for (UserObject object : listUser) {
            if(object.EmailAddress.equalsIgnoreCase(userEmail))
                return object.getFullName();
        }
        return null;
    }
}
