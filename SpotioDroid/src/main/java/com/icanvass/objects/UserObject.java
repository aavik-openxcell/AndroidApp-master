package com.icanvass.objects;

import org.json.JSONObject;

/**
 * Created by Justin CAO on 6/3/2014.
 */
public class UserObject {
    public Integer UserId;
    public String UserName;
    public String FirstName;
    public String LastName;
    public String EmailAddress;
    public String PhoneNumber;
    public String Type;
    public String Address;
    public String City;
    public String State;
    public String Zip;
    public String Phone2Number;
    public String FaxNumber;
    public Integer CommissionPlanId;
    public Integer PrimaryLocationId;
    public Integer SwitchedLocationId;
    public Integer ChargifyID;
    public Boolean IsActive;
    public Integer RecentReportId;

    public UserObject(JSONObject jsonObject) {
        UserName = jsonObject.optString("UserName");
        FirstName = jsonObject.optString("UserName");
        LastName = jsonObject.optString("LastName");
        EmailAddress = jsonObject.optString("EmailAddress");
    }

    public String getFullName() {
        return (FirstName == null ? "" : FirstName)  + " " + (LastName == null ? "" : LastName);
    }
}
