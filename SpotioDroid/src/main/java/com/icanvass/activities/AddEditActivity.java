package com.icanvass.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.icanvass.R;
import com.icanvass.abtracts.SDActivity;
import com.icanvass.database.PINDAO;
import com.icanvass.helpers.CommonHelpers;
import com.icanvass.helpers.SDDefine;
import com.icanvass.objects.CustomValuesObject;
import com.icanvass.objects.PINObject;
import com.icanvass.views.DateTimeDialog;
import com.icanvass.webservices.FieldsRequest;
import com.icanvass.webservices.SaveRequest;
import com.icanvass.webservices.StatusesRequest;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

//import com.icanvass.views.MyProgressDialog;


public class AddEditActivity extends SDActivity {

    private static final String SAVED_STATE_STATUS = "saved_state_status";
    private static final String SAVED_STATE_NUMBER = "saved_state_number";
    private static final String SAVED_STATE_STREET = "saved_state_street";
    private static final String SAVED_STATE_UNIT   = "saved_state_unit";

    public static final String USER_LOCATION = "userLocation";
    private static final int PRICE_QUOTED_MAXIMUM_CHARS = 6;
    public static ArrayList<JSONObject> mStatuses = new ArrayList<JSONObject>();
    private SaveTask mSaveTask = null;
    private ArrayList<JSONObject> mFields = new ArrayList<JSONObject>();
    private HashMap<String, String> mQuestions = new HashMap<String, String>();
    public HashMap<String, String> mTypes = new HashMap<String, String>();
    private JSONObject mSelectedStatus;
    private PINObject mExistingPin;

    private String mPhoneNumber;
    private String mName;

    private String mNumber;
    private String mStreetName;
    private String mUnit;
    private String mCity;
    private String mState;
    private String mZip;
    private String mCountry;
    private Location mLocation, userLocation;

    private ProgressDialog pd;
    private boolean isDestroyed;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_NUMBER, mNumber);
        outState.putString(SAVED_STATE_STREET, mStreetName);
        outState.putString(SAVED_STATE_UNIT, mUnit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isDestroyed = false;
        //Start of Dialog
        //pd = MyProgressDialog.show(AddEditActivity.this, "Hold on, App is working...", "");
        pd = new ProgressDialog(AddEditActivity.this, R.style.NewDialog);
        pd.setMessage("Hold on, App is working...");

        if (savedInstanceState != null){
            mStreetName = savedInstanceState.getString(SAVED_STATE_STREET);
            mNumber     = savedInstanceState.getString(SAVED_STATE_NUMBER);
            mUnit       = savedInstanceState.getString(SAVED_STATE_UNIT);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        requestStatuses();
        requestFields();

        PlaceholderFragment f = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag(PlaceholderFragment.TAG);

        if (f == null){
            f = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f, PlaceholderFragment.TAG)
                    .commit();
        }

        f.setListAdapter(new PinEditAdapter());

        boolean edit;
        Bundle arguments = getIntent().getExtras();
        if (arguments != null){
            userLocation = arguments.getParcelable(USER_LOCATION);
            mLocation    = arguments.getParcelable("location");
            edit         = arguments.getBoolean("edit");
        } else {
            return;
        }

        if (mLocation != null) {
            pd.setMessage("Hold on, App is working...");
            pd.show();
            getAddressFromLocation(mLocation, this, new GeocoderHandler());
        }
        if(edit)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_edit, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pd != null && pd.isShowing()){
            pd.dismiss();
            pd = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_edit_cancel) {
            finish();
            return true;
        }
        if (id == R.id.action_add_edit_save) {
            handleSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

   private void fillDataFromPin() {
        String currentStatus = mExistingPin.Status;
        for (JSONObject status : mStatuses) {
            if (status.optString("Name").equalsIgnoreCase(currentStatus)) {
                mSelectedStatus = status;
                break;
            }
        }
        notifyAboutStatuses();
        mNumber = ""+mExistingPin.Location.HouseNumber;
        mCity = mExistingPin.Location.City;
        mState = mExistingPin.Location.State;
        if (mState.equals("null")) mState = null;
        mZip = mExistingPin.Location.Zip;
        mStreetName = mExistingPin.Location.Street;
        mUnit = mExistingPin.Location.Unit;
        if (mUnit.equals("null")) mUnit = null;
        mCountry = mExistingPin.Location.Country;

        Location current = new Location("");
        current.setLatitude(mExistingPin.Latitude);
        current.setLongitude(mExistingPin.Longitude);
        mLocation = current;

        // fill fields
        ArrayList<CustomValuesObject> questions = mExistingPin.CustomValues;
        if(questions==null) return;

       for (CustomValuesObject q : questions) {
           String key = "" + q.DefinitionId;
           String value = "";
           if (!q.DateTimeValue.equalsIgnoreCase("null")) {
               value = q.DateTimeValue;
           } else if (!q.IntValue.equalsIgnoreCase("null")) {
               value = q.IntValue;
           } else if (!q.DecimalValue.equalsIgnoreCase("null")) {
               value = q.DecimalValue;
           } else {
               value = "" + q.StringValue;
           }
           mQuestions.put(key, value);
       }

    }

//    private boolean checkSaveButtonState(){
//        return mSelectedStatus != null;
//        // TODO
//    }

    private void handleSave() {
        // TODO handleSave
        if (mSelectedStatus == null) {
            Toast.makeText(this, "You must choose the status!", Toast.LENGTH_LONG).show();
            return;
        }
        if (mSaveTask != null) return;


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        try {
            JSONObject a = new JSONObject();

            if (mExistingPin != null) {     //edit
                a.put("Id", mExistingPin.Id);
            }

            JSONObject location = new JSONObject();
            //location.put("Address", mNumber + "\n" + mStreetName);
            location.put("Street",mStreetName);
            location.put("HouseNumber",mNumber);
            location.put("City", mCity);
            location.put("State", mState);
            location.put("Zip", mZip);
            location.put("Country",mCountry);
            if (mUnit != null && !mUnit.equals("")) location.put("Unit", mUnit);

            a.put("Status", mSelectedStatus.optString("Name"));
            a.put("Location", location);

            DecimalFormat df = new DecimalFormat("#.######", DecimalFormatSymbols.getInstance(Locale.US));
            double latitude = 0.0;
            double longitude = 0.0;
            if (mLocation != null) {
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
            }
            a.put("Latitude", df.format(latitude));
            a.put("Longitude", df.format(longitude));
            a.put("UserName", sp.getString("EMAIL", ""));
            if (userLocation != null) {
                a.put("UserCurrentLongitude", df.format(userLocation.getLongitude()));
                a.put("UserCurrentLatitude", df.format(userLocation.getLatitude()));

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                Address address = null;
                try {
                    List<Address> list = geocoder.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        address = list.get(0);
                        String number = address.getSubThoroughfare();
                        if (number != null) {
                            String[] parts = number.split("-");
                            if (parts.length > 0) {
                                number = parts[0];
                            }
                            a.put("UserLocation", number + "\n" + address.getThoroughfare());
                        }
                        else
                            a.put("UserLocation", address.getThoroughfare());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            a.put("DateTimeInputted", SDDefine.simpleServerFormat.format(new Date()));
            a.put("ClientData", new JSONObject());

            JSONArray cv = new JSONArray();
            int fakeId = 1;
            for (String key : mQuestions.keySet()) {
                if (mTypes.isEmpty()) break;
                String type = mTypes.get(key);
                //todo type can null here (not available in hashmap, should check, fast fix)
                if (type == null) continue;
                JSONObject v = new JSONObject();
                v.put("DefinitionId", key);
                if (type.equals("DateTime")) {
                    // TODO use server format timestamp
                    String dateTime = CommonHelpers.timeStampToTimeString(mQuestions.get(key), SDDefine.serverFormat);
                    v.put("DateTimeValue", dateTime);
                } else if (type.equals("Money")) {
                    v.put("DecimalValue", mQuestions.get(key));
                } else if (type.equals("Number")) {
                    v.put("IntValue", mQuestions.get(key));
                } else {
                    v.put("StringValue", mQuestions.get(key));
                }
                v.put("Id",fakeId++);
                cv.put(v);
            }

            a.put("CustomValues", cv);
            mSaveTask = new SaveTask(a);
            mSaveTask.execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showStatusSelection() {
        if (mStatuses.size() < 1) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select status");

        CharSequence[] cs = new CharSequence[mStatuses.size()];
        for (int i = 0; i < mStatuses.size(); i++) {
            cs[i] = mStatuses.get(i).optString("Name");
        }
        builder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSelectedStatus = mStatuses.get(which);
                notifyAboutStatuses();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showOptionsDialog(final JSONObject field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(field.optString("Name"));

        String options = field.optString("Settings");
        final String lines[] = options.split("\\n");
        CharSequence[] cs = new CharSequence[lines.length];

        System.arraycopy(lines, 0, cs, 0, lines.length);

        builder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mQuestions.put(field.optString("Id"), lines[which]);
                mTypes.put(field.optString("Id"), "DropDown");
                notifyAboutStatuses();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDateDialog(final JSONObject field) {
        Calendar calendar = Calendar.getInstance();
        new DateTimeDialog(this, calendar, new DateTimeDialog.DateTimeDialogCallback() {
            @Override
            public void onPicked(DateTimeDialog view, int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute, 0);

                mQuestions.put(field.optString("Id"), String.valueOf(calendar.getTimeInMillis()));
                mTypes.put(field.optString("Id"), "DateTime");

                PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("placeholder");
                PinEditAdapter adapter = (PinEditAdapter) fragment.getListAdapter();
                adapter.notifyDataSetChanged();
                if (view != null && view.isShowing()) {
                    view.dismiss();
                }

                if (field.optString("Name").equalsIgnoreCase("Set Appointment")) {
                    String fullAddress  = mNumber + " " + mStreetName;
                    String name         = mName == null ? "" : mName;
                    String phoneNumber  = mPhoneNumber == null ? "" : mPhoneNumber;
                    String status       = mSelectedStatus == null ? "No status" :  mSelectedStatus.optString("Name");
                    setAppointment(calendar.getTimeInMillis(), calendar.getTimeInMillis() + 60 * 60 * 1000,status + ":" + name, fullAddress, phoneNumber, name);
                }
            }
        }).show();
    }


    public static void getAddressFromLocation(
            final Location location, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                Address address = null;
                try {
                    List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        address = list.get(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //Log.e("SPOTIO", "Impossible to connect to Geocoder", e);
                } finally {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (address != null) {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("address", address);
                        msg.setData(bundle);
                    } else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }

    private void requestStatuses() {
        setSupportProgressBarIndeterminateVisibility(true);

        StatusesRequest request = new StatusesRequest(this);

        spiceManager.execute(request, request.createCacheKey(),DurationInMillis.ALWAYS_RETURNED, new StatusesRequestListener());
    }

    private void requestFields() {
        setSupportProgressBarIndeterminateVisibility(true);

        FieldsRequest request = new FieldsRequest(this);

        spiceManager.execute(request, request.createCacheKey(),
                DurationInMillis.ALWAYS_RETURNED, new FieldsRequestListener());
    }

    void notifyAboutStatuses() {
        PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag("placeholder");
        PinEditAdapter adapter = (PinEditAdapter) fragment.getListAdapter();
        adapter.notifyDataSetChanged();
    }

    void notifyAboutFields() {
        Fragment f = getSupportFragmentManager().findFragmentByTag("placeholder");
        if (f == null ) return;
        PlaceholderFragment fragment = (PlaceholderFragment)f;

        PinEditAdapter adapter = (PinEditAdapter) fragment.getListAdapter();
        adapter.notifyDataSetChanged();
    }

    void handleAddress(Address address) {
        String number = address.getSubThoroughfare();
        if (number != null) {
            String[] parts = number.split("-");
            if (parts.length > 0) {
                number = parts[0];
                mNumber = number;
            }

        }

        mStreetName = address.getThoroughfare();
        mUnit = "";
        mState = address.getAdminArea();
        mCity = address.getLocality();
        if (mCity == null) {
            mCity = mState;
        }
        mZip = address.getPostalCode();
        mCountry = address.getCountryName();
        notifyAboutFields();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            if (!isDestroyed) {
                switch (message.what) {
                    case 1:
                        Bundle bundle = message.getData();
                        Address address = (Address) bundle.getParcelable("address");
                        handleAddress(address);
                        break;
                    default:
                        result = null;
                }
            }
        }
    }

    private class StatusesRequestListener implements RequestListener<JSONObject> {
        private final String TAG = StatusesRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.e(TAG, "Statuses request error");
        }

        @Override
        public void onRequestSuccess(JSONObject response) {
            if (response == null) {
                return;
            }

            JSONArray statuses = response.optJSONArray("value");
            if (statuses == null) {
                return;
            }

            mStatuses.clear();
            for (int i = 0; i < statuses.length(); i++) {
                JSONObject status = statuses.optJSONObject(i);
                boolean isActive = status.optBoolean("IsActive");
                if (isActive) {
                    mStatuses.add(status);
                }
            }
            Collections.sort(mStatuses, new Comparator<JSONObject>() {
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
            notifyAboutStatuses();
        }
    }

    private class FieldsRequestListener implements RequestListener<JSONObject> {
        private final String TAG = FieldsRequestListener.class.getSimpleName();

        @Override
        public void onRequestFailure(SpiceException e) {
            Log.e(TAG, "Fields request error");
        }

        @Override
        public void onRequestSuccess(JSONObject response) {
            if (response == null) {
                return;
            }

            JSONArray fields = response.optJSONArray("value");
            if (fields == null) {
                return;
            }

            mFields.clear();
            for (int i = 0; i < fields.length(); i++) {
                JSONObject status = fields.optJSONObject(i);
                boolean isDisabled = status.optBoolean("IsDisabled");
                if (!isDisabled) {
                    mFields.add(status);
                }
            }

            Collections.sort(mFields, new Comparator<JSONObject>() {
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

            String s = getIntent().getExtras().getString("pinId");
            if (s != null) {
                mExistingPin = PINDAO.getInstance().get(s);
                if (mExistingPin != null) {
                    fillDataFromPin();
                }
            }

            notifyAboutFields();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {

        public static final String TAG = "placeholder";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            setListAdapter(new PinEditAdapter());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_details, container, false);
        }
    }

    public class PinEditAdapter extends ArrayAdapter {

        ArrayList<String> mAddressHeaders = new ArrayList<String>();

        private EditText etNumber;
        private EditText etStreet;
        private EditText etUnit;
        private EditText etName;
        private EditText etPhoneNumber1;
        private EditText etEmail;
        private EditText etNotes;


        public PinEditAdapter() {
            super(AddEditActivity.this, R.layout.pin_list_item);
            mAddressHeaders.add("Status");
            mAddressHeaders.add("Number");
            mAddressHeaders.add("Street");
            mAddressHeaders.add("Unit");
        }

        @Override
        public int getCount() {
            return 5 + mFields.size();
        }

        @Override
        public boolean isEnabled(int position) {
            return false;//position!=4;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (position == 0) {
                convertView = inflater.inflate(R.layout.details_edit_status, parent, false);
                Button button = (Button) convertView.findViewById(R.id.button);
                button.setText(mSelectedStatus != null ? mSelectedStatus.optString("Name") : "Status");
                button.setTextColor(mSelectedStatus != null ?
                        Color.parseColor(mSelectedStatus.optString("Color")) :
                        getResources().getColor(android.R.color.black));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showStatusSelection();
                    }
                });
                button.setEnabled(mStatuses.size() > 0);
            } else if (position < 4) {
                convertView = inflater.inflate(R.layout.details_edit_item, parent, false);
                final EditText editText = (EditText) convertView.findViewById(R.id.editText);
                editText.setHint(mAddressHeaders.get(position));
                editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                editText.setLines(1);
                editText.setMinLines(1);
                editText.setMaxLines(1);
                if (position == mAddressHeaders.size() - 3) {
                    Button imgvAdd = (Button) convertView.findViewById(R.id.imgv_add);
                    Button imgvRemove = (Button) convertView.findViewById(R.id.imgv_remove);
                    imgvAdd.setVisibility(View.VISIBLE);
                    imgvRemove.setVisibility(View.VISIBLE);
                    editText.setText(mNumber);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                    editText.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            mNumber = s.toString();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });
                    imgvAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int intNumber;
                            try {
                                intNumber = Integer.parseInt(mNumber);
                            } catch (NumberFormatException e) {
                                intNumber = 0;
                            }
                            mNumber = String.valueOf(intNumber + 1);
                            editText.setText(mNumber);
                        }
                    });
                    imgvRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int intNumber;
                            try {
                                intNumber = Integer.parseInt(mNumber);
                            } catch (NumberFormatException e) {
                                intNumber = 0;
                            }
                            mNumber = String.valueOf(intNumber > 0 ? intNumber - 1 : intNumber);
                            editText.setText(mNumber);
                        }
                    });
                } else if (position == 2) {
                    editText.setText(mStreetName);
                    editText.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            mStreetName = s.toString();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });
                } else if (position == 3) {
                    editText.setText(mUnit);
                    editText.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            mUnit = s.toString();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });
                }
            } else if (position == 4) {
                convertView = inflater.inflate(R.layout.details_edit_item, parent, false);
                EditText editText = (EditText) convertView.findViewById(R.id.editText);
                editText.setVisibility(View.GONE);
            } else {
                // TODO
                final String type = mFields.get(position - 5).optString("Type");
                if (type.equals("DropDown")) {
                    convertView = inflater.inflate(R.layout.addedit_dialog_list_item, parent, false);
                    TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
                    if (mQuestions.containsKey(mFields.get(position - 5).optString("Id"))) {
                        text2.setText(mQuestions.get(mFields.get(position - 5).optString("Id")));
                    }
                    text1.setText(mFields.get(position - 5).optString("Name"));
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showOptionsDialog(mFields.get(position - 5));
                        }
                    });
                } else if ((type.equals("DateTime"))) {
                    convertView = inflater.inflate(R.layout.addedit_dialog_list_item, parent, false);
                    TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
                    TextView text3 = (TextView) convertView.findViewById(R.id.text3);
                    if (mQuestions.containsKey(mFields.get(position - 5).optString("Id"))) {
//                        DateFormat df = DateFormat.getDateTimeInstance();

                        String timeStamp = mQuestions.get(mFields.get(position - 5).optString("Id"));
                        if (timeStamp.contains("-")) {
                            Date date;
                            try {
                                date = SDDefine.simpleServerFormat.parse(timeStamp);
                                text2.setText(SDDefine.localFormat.format(date));
                                Calendar cal = Calendar.getInstance(); // creates calendar
                                cal.setTime(date); // sets calendar time/date
                                cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
                                text3.setText(SDDefine.localFormat.format(cal.getTime()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            String dateTime = CommonHelpers.timeStampToTimeString(timeStamp, SDDefine.localFormat);
                            text2.setText(dateTime);
                            Date date = new Date(Long.parseLong(timeStamp)+ 60 * 60 * 1000);
                            text3.setText(SDDefine.localFormat.format(date));
                        }

                    }
                    text1.setText(mFields.get(position - 5).optString("Name"));
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDateDialog(mFields.get(position - 5));
                        }
                    });
                } else {
                    convertView = inflater.inflate(R.layout.details_edit_item, parent, false);
                    final EditText editText = (EditText) convertView.findViewById(R.id.editText);
                    final JSONObject field = mFields.get(position - 5);
                    String hint = field.optString("Name");
                    editText.setHint(hint);
                    if (type.equalsIgnoreCase("NoteBox")) {
                        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                        editText.setSingleLine(false);
                        editText.setMaxLines(20);
                        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    } else {
                        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                        editText.setSingleLine(true);
                        if (hint.equals("Name")) {
                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    mName = s.toString();
                                }
                            });
                        }
                    }
                    if (type.equalsIgnoreCase("PhoneNumber")
                            || type.equalsIgnoreCase("FaxNumber")) {
                        editText.setInputType(InputType.TYPE_CLASS_PHONE);
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                mPhoneNumber = s.toString();
                            }
                        });
                    }
                    if (type.equalsIgnoreCase("Number")
                            || type.equalsIgnoreCase("Money")) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        InputFilter[] filters = new InputFilter[1];
                        filters[0] = new InputFilter.LengthFilter(PRICE_QUOTED_MAXIMUM_CHARS); //Filter to 10 characters
                        editText.setFilters(filters);
                    }
                    if (mQuestions.containsKey(field.optString("Id"))) {
                        editText.setText(mQuestions.get(field.optString("Id")));
                    }
                    editText.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            mQuestions.put(field.optString("Id"), s.toString());
                            mTypes.put(field.optString("Id"), field.optString("Type"));
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }
                    });

                }
            }
            return convertView;
        }
    }

    private void createEvent(String title, String name, String fullAddress, String phoneNumber, long startDate, long endDate){
        String description = name + "\n" + fullAddress + "\n" + phoneNumber;
        ContentValues calEvent = new ContentValues();
        calEvent.put(CalendarContract.Events.CALENDAR_ID, 1);   // Default calendar
        calEvent.put(CalendarContract.Events.TITLE, title);
        calEvent.put(CalendarContract.Events.DESCRIPTION, description);
        calEvent.put(CalendarContract.Events.DTSTART, startDate + 1000 * 60 * 1);
        calEvent.put(CalendarContract.Events.DTEND, endDate);
        calEvent.put(CalendarContract.Events.HAS_ALARM, "1");
        calEvent.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Uri uri = getContentResolver().insert(Uri.parse("content://com.android.calendar/events"), calEvent);
        if (!TextUtils.isEmpty(uri.toString())){
            Toast.makeText(this, getString(R.string.toast_event_successfully_added), Toast.LENGTH_SHORT).show();
        }
        // TODO get event id if needed
        // addedEventId = Long.parseLong(uri.getLastPathSegment());
    }

    private void setAppointment(final long beginTime, final long endTime, final String title, final String fullAddress, final String phoneNumber, final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEditActivity.this);
        builder.setTitle("Spotio");
        builder.setMessage("Do you want to set appointment in Calendar app?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                createEvent(title, name, fullAddress, phoneNumber, beginTime, endTime);
                dialog.cancel();
            }

        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        try {
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SaveTask extends AsyncTask<Void, Void, String> {
        public static final String BROADCAST_PIN_CHANGED = "com.spotio.PinChanged";
        JSONObject mDictionary;

        SaveTask(JSONObject dic) {
            mDictionary = dic;
            //Log.i("SaveTask", mDictionary.toString());
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AddEditActivity.this, R.style.NewDialog);
            pd.setMessage("Loading..");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            try {
                result = new SaveRequest(mDictionary, AddEditActivity.this).loadDataFromNetwork();
            } catch (Exception e) {
                e.printStackTrace();
//                Toast.makeText(AddEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(final String result) {
            mSaveTask = null;

            if (pd != null && pd.isShowing()){
                pd.dismiss();
            }

            if (result!=null) {
                Intent intent = new Intent();
                intent.setAction(BROADCAST_PIN_CHANGED);
                try {
                    JSONObject pin = new JSONObject(result);
                    //JSONObject pin=res.optJSONObject("value");
                    JSONArray a=mDictionary.optJSONArray("CustomValues");
                    for(int i=0;i<a.length();++i){
                        a.getJSONObject(i).put("PinId",pin.getString("Id"));
                    }
                    pin.put("CustomValues",a);
                    intent.putExtra("pin",pin.toString());
                    sendBroadcast(intent);
                    setResult(RESULT_OK);
                    finish();
                }catch(JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //proceedToHomeActivity();
            }
        }

        @Override
        protected void onCancelled() {
            mSaveTask = null;
        }
    }


}
