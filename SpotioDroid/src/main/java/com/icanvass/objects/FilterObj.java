package com.icanvass.objects;

/**
 * Created by Justin CAO on 6/7/2014.
 */
public class FilterObj {
    private String statuses;
    private String dateFrom, dateTo;
    private String useres;

    public FilterObj(String statuses, String dateFrom, String dateTo, String useres){
        this.statuses = statuses;
        this.dateFrom = dateFrom;
        this.dateTo= dateTo;
        this.useres = useres;
    }

    public String getStatuses() {
        return statuses;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public String getUseres() {
        return useres;
    }
}
