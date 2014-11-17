package com.icanvass.database;

import android.content.Context;

/**
 * Created by Justin CAO on 6/6/2014.
 */
public abstract class SDDAO<T> extends NMDAO<T> {
    public SDDAO(Context context) {
    }

    public SDSQLiteHelper getDbHelper() {
        return SDSQLiteHelper.getInstance();
    }
}
