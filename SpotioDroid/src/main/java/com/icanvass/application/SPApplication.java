package com.icanvass.application;

import android.app.Application;

import com.icanvass.R;
import com.icanvass.database.ClientDataDAO;
import com.icanvass.database.CustomValuesDAO;
import com.icanvass.database.LocationDAO;
import com.icanvass.database.PINDAO;
import com.icanvass.database.SDSQLiteHelper;
import com.icanvass.helpers.SDDefine;
import com.icanvass.helpers.SPHelper;
import com.newrelic.agent.android.NewRelic;

import java.util.TimeZone;

/**
 * Created by Justin CAO on 6/3/2014.
 */
public class SPApplication extends Application {

    public static SPApplication instance;

    public static SPApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        SPHelper.initializeInstance(getApplicationContext());

        // init database
        SDSQLiteHelper.initializeInstance(getApplicationContext());
        ClientDataDAO.initializeInstance(getApplicationContext());
        CustomValuesDAO.initializeInstance(getApplicationContext());
        LocationDAO.initializeInstance(getApplicationContext());
        PINDAO.initializeInstance(getApplicationContext());

        NewRelic.withApplicationToken(getString(R.string.new_relic_api_key)).start(this);

        SDDefine.serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SDDefine.simpleServerFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

}
