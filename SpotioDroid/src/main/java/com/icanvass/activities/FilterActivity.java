package com.icanvass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.icanvass.R;
import com.icanvass.abtracts.SDActivity;
import com.icanvass.helpers.JsonHelper;
import com.icanvass.helpers.SDDefine;
import com.icanvass.helpers.SPHelper;
import com.icanvass.objects.FilterObj;
import com.icanvass.objects.ListUsers;
import com.icanvass.objects.UserObject;
import com.icanvass.views.DateDialog;
import com.icanvass.webservices.StatusesRequest;
import com.icanvass.webservices.UsersRequest;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class FilterActivity extends SDActivity
        implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupCollapseListener {

    private SpiceManager spiceManager;

    private ArrayList<String> mGroups;
    private ArrayList<ArrayList<String>> mChildren;

    private ArrayList<String> mSelectedStatuses = new ArrayList<String>();
    private HashMap<String,String> mSelectedUsers = new HashMap<String, String>();
    public static ArrayList<String> mStatuses;
    public static HashMap<String, String> mColors;
    public static ArrayList<UserObject> mUsers;
    private Date mFromDate, mToDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spiceManager = new SpiceManager(
                JacksonSpringAndroidSpiceService.class);
        requestStatuses();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_filter);
        setSupportProgressBarIndeterminateVisibility(true);
        track("FilterView");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    private void initDatas() {
        setGroupsAndChilds();
        readSavedData();

        ExpandableListView listView = (ExpandableListView) findViewById(android.R.id.list);
        FilterListAdapter adapter = new FilterListAdapter();
        listView.setAdapter(adapter);
        listView.setOnChildClickListener(this);
        listView.setOnGroupCollapseListener(this);


        if (mSelectedStatuses.size() > 0) {
            listView.expandGroup(0);
        }
        if (mFromDate != null || mToDate != null) {
            listView.expandGroup(1);
        }
        if (mSelectedUsers.size() > 0) {
            listView.expandGroup(2);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == 16908332) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        if (id == R.id.action_add_edit_cancel) {
            finish();
            return true;
        } else if (id == R.id.action_add_edit_save) {
            saveSelection();
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSelection() {
        JSONArray users = new JSONArray();
        for (String email : mSelectedUsers.values()) {
            users.put(email);
        }

        JSONArray statuses = new JSONArray();
        for (String st : mSelectedStatuses) {
            statuses.put(st);
        }
        String fromDate = null;
        if (mFromDate != null) {
            fromDate = SDDefine.serverFormat.format(mFromDate);
        }
        String toDate = null;
        if (mToDate != null) {
            toDate = SDDefine.serverFormat.format(mToDate);
        }

        SPHelper.getInstance().saveFilter(new FilterObj(statuses.toString(), fromDate, toDate, users.toString()));

        // TAG_MIXPANEL
        track("Filter");
    }

    private String getUserNameByEmail(String email){
        for (UserObject user : mUsers){
            if (user.EmailAddress.equals(email)) return user.getFullName();
        }
        return null;
    }

    private void readSavedData() {
        JSONArray usersJSON = new JSONArray();
        JSONArray statusesJSON = new JSONArray();
        String dateFrom = null;
        String dateTo = null;
        try {
            FilterObj filterObj = SPHelper.getInstance().getFilter();
            dateFrom = filterObj.getDateFrom();
            dateTo = filterObj.getDateTo();
            if (filterObj.getUseres() != null)
                usersJSON = new JSONArray(filterObj.getUseres());
            if (filterObj.getStatuses() != null)
                statusesJSON = new JSONArray(filterObj.getStatuses());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSelectedStatuses = new ArrayList<String>();
        mSelectedUsers = new HashMap<String, String>();
        for (int i = 0; i < usersJSON.length(); i++) {
            String email = usersJSON.optString(i);
            mSelectedUsers.put(getUserNameByEmail(email), email);
        }
        mSelectedStatuses.clear();
        for (int i = 0; i < statusesJSON.length(); i++) {
            mSelectedStatuses.add(statusesJSON.optString(i));
        }

        try {
            if (dateFrom != null) mFromDate = SDDefine.serverFormat.parse(dateFrom);
            if (dateTo != null) mToDate = SDDefine.serverFormat.parse(dateTo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void setGroupsAndChilds() {
        mGroups = new ArrayList<String>();
        mChildren = new ArrayList<ArrayList<String>>();

        mGroups.add("Status");
        mGroups.add("Date");
        mGroups.add("User");

        mChildren.add(mStatuses);

        ArrayList<String> dates = new ArrayList<String>();
        dates.add("From");
        dates.add("To");
        mChildren.add(dates);

        mUsers = ListUsers.load();
        ArrayList<String> users = ListUsers.getListUserName();
        mChildren.add(users);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, final int childPosition, long id) {
        if (groupPosition == 0 || groupPosition == 2) {
            CheckedTextView checkedTextView = (CheckedTextView) v;
            String text = checkedTextView.getText().toString();
            checkedTextView.setChecked(!checkedTextView.isChecked());
            if (groupPosition == 0) {
                mSelectedStatuses.remove(text);
                if (checkedTextView.isChecked()) {
                    mSelectedStatuses.add(text);
                }
            } else {
                mSelectedUsers.remove(text);
                if (checkedTextView.isChecked()) {
                    UserObject user = mUsers.get(childPosition);
                    String email = user.EmailAddress;
                    mSelectedUsers.put(getUserNameByEmail(email), email);
                }
            }
        } else {
            //date filter selected
            Date date = childPosition == 0 ? mFromDate : mToDate;
            Calendar calendar = Calendar.getInstance();
            if (date != null)
                calendar.setTime(date);
            DateDialog dateTimeDialog = new DateDialog(this, calendar, new DateDialog.DateDialogCallback() {
                @Override
                public void onPicked(DateDialog view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar calendar = Calendar.getInstance();
                    if (childPosition == 0) {
                        calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 1);
                        mFromDate = calendar.getTime();
                        if (mToDate != null && mToDate.getTime() < mFromDate.getTime()){
                            mFromDate = mToDate;
                            Toast.makeText(FilterActivity.this, "Date From must be earlier than date To", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 1);
//                        calendar.set(year, monthOfYear, dayOfMonth + 1, 0, 0, 1);
                        mToDate = calendar.getTime();
                        if (mFromDate != null && mToDate.getTime() < mFromDate.getTime()){
                            mToDate = mFromDate;
                            Toast.makeText(FilterActivity.this, "Date To must be greater or equal than date From", Toast.LENGTH_SHORT).show();
                        }
                    }
                    ExpandableListView listView = (ExpandableListView) FilterActivity.this.findViewById(android.R.id.list);
                    FilterListAdapter adapter = (FilterListAdapter) listView.getExpandableListAdapter();
                    adapter.notifyDataSetChanged();
                    if (view != null && view.isShowing()) {
                        view.dismiss();
                    }
                }
            });
            dateTimeDialog.show();
        }
        return true;
    }

    public void onGroupCollapse(int groupPosition) {
        switch (groupPosition) {
            case 0:
                mSelectedStatuses.clear();
                break;
            case 1:
                mFromDate = null;
                mToDate = null;
                break;
            case 2:
                mSelectedUsers.clear();
                break;
            default:
        }
    }

    private void requestStatuses() {
        setSupportProgressBarIndeterminateVisibility(true);

        StatusesRequest request = new StatusesRequest(this);

        spiceManager.execute(request, request.createCacheKey(),
                DurationInMillis.ALWAYS_RETURNED, new StatusesRequestListener());
    }

    private class StatusesRequestListener implements
            RequestListener<JSONObject> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(FilterActivity.this,
                    "Error during request: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
            FilterActivity.this.setSupportProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onRequestSuccess(JSONObject response) {

            // listFollowers could be null just if contentManager.getFromCache(...)
            // doesn't return anything.
            if (response == null) {
                return;
            }

            JSONArray statuses = response.optJSONArray("value");
            if (statuses == null) {
                return;
            }

            ArrayList<JSONObject> tmpList = new ArrayList<JSONObject>();

            for (int i = 0; i < statuses.length(); i++) {
                JSONObject status = statuses.optJSONObject(i);
                boolean isActive = status.optBoolean("IsActive");
                if (isActive) {
                    tmpList.add(status);
                }
            }
            Collections.sort(tmpList, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject jsonObject, JSONObject jsonObject2) {
                    int retVal = 0;
                    try {
                        retVal = jsonObject.getInt("Order") - jsonObject2.optInt("Order");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return retVal;
                }
            });

            mStatuses = new ArrayList<String>();
            mColors = new HashMap<String, String>();
            for (JSONObject status : tmpList) {
                String s = status.optString("Name");
                mStatuses.add(s);
                if (!mColors.containsKey(s)) {
                    mColors.put(s, status.optString("Color"));
                }
            }
//            notifyAboutStatuses();
            requestUsers();
        }
    }

    private void notifyAboutStatuses() {
        Intent intent = new Intent();
        intent.setAction("com.spotio.Statuses");
        sendBroadcast(intent);
    }

    private void requestUsers() {
        UsersRequest request = new UsersRequest(this);
        // TODO requestUsers
        spiceManager.execute(request, new UsersRequsetListener());
    }

    private class UsersRequsetListener implements RequestListener<JSONObject> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(FilterActivity.this,
                    "Error during request: " + spiceException.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onRequestSuccess(JSONObject jsonObject) {
            ListUsers listUsers = JsonHelper.fromJson(jsonObject, ListUsers.class);
            ListUsers.save(listUsers);
            initDatas();
            setSupportProgressBarIndeterminateVisibility(false);
        }
    }


    public class FilterListAdapter extends BaseExpandableListAdapter {

        public LayoutInflater inflater;

        public FilterListAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mChildren.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String children = (String) getChild(groupPosition, childPosition);
            if (groupPosition == 0 || groupPosition == 2) {
                CheckedTextView text;
                convertView = inflater.inflate(R.layout.filter_list_child, parent, false);
                text = (CheckedTextView) convertView.findViewById(R.id.textView_filterName);
                text.setText(children);
                if (groupPosition == 0) {
                    text.setChecked(mSelectedStatuses.contains(children));
                } else {
                    String email = mUsers.get(childPosition).EmailAddress;
                    text.setChecked(mSelectedUsers.containsValue(email));
                }
            } else if (groupPosition == 1) {
                // date time
                convertView = inflater.inflate(R.layout.date_list_item, parent, false);
                TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);

                text1.setText(children);
                Date date = childPosition == 0 ? mFromDate : mToDate;
                if (date != null) {
                    text2.setText(SDDefine.DateFormat.format(date));
                }
            }

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mChildren.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroups.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mGroups.size();
        }

        @Override
        public void onGroupCollapsed(int groupPosition) {
            super.onGroupCollapsed(groupPosition);
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            super.onGroupExpanded(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.filter_list_group, null);
            }

            ((CheckedTextView) convertView).setText(mGroups.get(groupPosition));
            ((CheckedTextView) convertView).setChecked(isExpanded);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

}
