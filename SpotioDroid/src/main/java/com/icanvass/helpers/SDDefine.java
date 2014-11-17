package com.icanvass.helpers;

import java.text.SimpleDateFormat;

/**
 * Created by Justin CAO on 6/3/2014.
 */
public class SDDefine {
    public static final String dbName ="db_spotio";
    public static final int dbDefaultVersion = 1;    public static String LIST_USRS_KEY = "List users";
//    public static final String DATE_TIME_FORMAT = "MM/dd/yyyy h:mm a";
//    public static final String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final SimpleDateFormat localFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
    public static final SimpleDateFormat DateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static SimpleDateFormat simpleServerFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final String FILTER_USERS = "FILTER_USERS";
    public static final String FILTER_STATUSES = "FILTER_STATUSES";
    public static final String FILTER_DATE_FROM = "FILTER_DATE_FROM";
    public static final String FILTER_DATE_TO = "FILTER_DATE_TO";
}
