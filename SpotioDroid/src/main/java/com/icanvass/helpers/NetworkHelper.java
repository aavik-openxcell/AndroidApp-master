package com.icanvass.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.icanvass.application.SPApplication;

/**
 * Created by dev2 on 01.04.14.
 */
public abstract class NetworkHelper {

    public static boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) SPApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
