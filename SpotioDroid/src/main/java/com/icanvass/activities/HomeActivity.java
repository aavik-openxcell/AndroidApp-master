package com.icanvass.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.icanvass.R;
import com.icanvass.abtracts.SDActivity;
import com.icanvass.database.PINDAO;
import com.icanvass.database.PINTable;
import com.icanvass.database.SDSQLiteHelper;
import com.icanvass.fragments.GoToWebsiteFragment;
import com.icanvass.fragments.MapFragment;
import com.icanvass.fragments.NavigationDrawerFragment;
import com.icanvass.fragments.PinsListFragment;
import com.icanvass.fragments.RefreshListener;
import com.icanvass.helpers.CommonHelpers;
import com.icanvass.helpers.SPHelper;
import com.icanvass.objects.CustomValuesObject;
import com.icanvass.objects.PINObject;
import com.icanvass.webservices.FieldsRequest;
import com.icanvass.webservices.PinsRequest;
import com.icanvass.webservices.StatusesRequest;
import com.icanvass.webservices.UsersRequest;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//import com.freshdesk.mobihelp.Mobihelp;
//import com.freshdesk.mobihelp.MobihelpConfig;

public class HomeActivity extends SDActivity  implements NavigationDrawerFragment.NavigationDrawerCallbacks,PinsListFragment.OnFragmentInteractionListener,
                                                         MapFragment.OnFragmentInteractionListener, LocationListener, View.OnClickListener{

    private static final int MAX_PINS_COUNT_FOR_SHARE = 150;
    //    private final int EDIT_PIN_CODE = 1000;
    private final int REQUEST_CODE_GOOGLE_SERVICES_ERROR = 1;
    private final int FILTER_CODE = 1001;
    public static final int ADD_CODE = 2;
    private final double DELTA_AROUND_USER_CURENT_LOCATION = 0.0002;

    private PinsReceiver pinsReceiver;
    private FanOutReceiver fanoutReceiver;
    public ProgressDialog pd;
    private boolean isStopped = true;
    private ProgressBar progressBarTopLeft;

    private RefreshListener mRefreshListener;

    protected LocationManager locationManager;
    protected Location mLocation, mapLocation, clickedLocation;

//    public static ArrayList<JSONObject> mPins = new ArrayList<JSONObject>();
//    public static Map mPinsMap = new HashMap<String, JSONObject>();
    public static ArrayList<String> mStatuses = new ArrayList<String>();
    public static HashMap<String, String> mColors = new HashMap<String, String>();

    public static PINObject pin ;

    private boolean requesting = false;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private NavigationDrawerFragment mNavigationDrawerFragment;

    // latitude and longitude
    private DecimalFormat decimalFormat = new DecimalFormat("#.######", DecimalFormatSymbols.getInstance(Locale.US));
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private String mCurrentDrawerItemName;
    private List<String> mGoToWebsiteTexts;
    private Handler h;

    public void showTopLeftProgress(boolean isShow){
        if (isShow){
            progressBarTopLeft.setVisibility(View.VISIBLE);
        } else {
            progressBarTopLeft.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        pd = new ProgressDialog(HomeActivity.this, R.style.NewDialog);
        pd.setMessage("Please wait while your pins are downloading, this will only happen once.");
        pd.setCancelable(false);
        pd.setIndeterminate(true);

        progressBarTopLeft = (ProgressBar) findViewById(R.id.progress_topLeft);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //mapLocation = mLocation;
//        mLocation = new Location("");
//        mLocation.setLatitude(96.800365);
//        mLocation.setLongitude(32.835679);
        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 5, this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mGoToWebsiteTexts = Arrays.asList(
                "Want to add some people to your team? No problem! Just click the button to go to the web app and hit the \"Add User\" button in the top right corner.",
                "Make it work for you! Go ahead and login to the web app by clicking the button below and in the settings menu in the top right you can customize pretty much everything.",
                "Need to gather more info? Add all you want in the SPOTIO web app, click the button below to go to the login page.",
                "I know, I know. Made a mistake and need to delete a PIN. Go to the web app where you can do that and much more, just click below to go to the login screen.",
                "Custom reports with all your data are right around the corner in the web app, click below."
        );

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        requestPins();
        requestStatuses();
        requestUsers();
        IntentFilter filter = new IntentFilter(AddEditActivity.SaveTask.BROADCAST_PIN_CHANGED);
        filter.addAction("com.spotio.PinsEnd");
        pinsReceiver = new PinsReceiver();
        registerReceiver(pinsReceiver, filter);

        IntentFilter fanoutfilter=new IntentFilter("com.spotio.fanout");
        fanoutReceiver=new FanOutReceiver();
        registerReceiver(fanoutReceiver,fanoutfilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(pinsReceiver);
        unregisterReceiver(fanoutReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStopped = false;
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (resultCode!= ConnectionResult.SUCCESS){
//            GooglePlayServicesUtil.getErrorDialog(resultCode, this, REQUEST_CODE_GOOGLE_SERVICES_ERROR, new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialogInterface) {
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
//                    alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            finish();
//                        }
//                    });
//                    alertDialog.setTitle(getString(R.string.no_google_services));
//                    alertDialog.show();
//                }
//            }).show();
//        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String name = sharedPreferences.getString("EMAIL", null);
        if (name == null) {
            showLogin();
            return;
        }
        if(pin != null){
            FragmentTabHost tabHost = (FragmentTabHost)  this.findViewById(android.R.id.tabhost);
            tabHost.setCurrentTab(0);

            /*double lat = pin.optDouble("Latitude");
            double lng = pin.optDouble("Longitude");
            Location cLocation = new Location("");
            cLocation.setLatitude(lat);
            cLocation.setLongitude(lng);
            moveCamera(cLocation);*/
            FragmentManager fragmentManager = this.getSupportFragmentManager();
            MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.container).getChildFragmentManager().findFragmentByTag("map");
            mapFragment.autoNavigateToPinId(pin.Id);
            //mapFragment.move
            //if (mapFragment.markerPins.containsValue(pin)){
//                for (Map.Entry<Marker, String> e : mapFragment.markerPins.entrySet()) {
//                    Marker key = e.getKey();
//                    String value = e.getValue();
//                    if (value != null)
//                    {
//                        if(value.equals(pin.Id)) {
//                            mapFragment.b = true;
//                            mapFragment.onMarkerClick(key);
                            //key.showInfoWindow();
//                            mapFragment.autoNavigateToPinId(pin.Id);
//                        }
//                    }
//                }
            //}

            /*List<Fragment> fragments = fragmentManager.getFragments();
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible()) {
                    MapFragment tabFragment = (FragmentTabsFragmentSupport) fragment;
                    tabFragment.setTab(0);
                }
            }*/
            pin = null;
        }
    }

    @Override
    public void onClick(View view) {
    }

    public void setRefreshListener(RefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    private void requestPins() {
        String refreshdate = SPHelper.getInstance().getDate("refresh");
        mRefreshListener.onRefreshBegins();
        if(refreshdate==null) {
            pd.show();
        }
//        PinsRequest request = new PinsRequest(HomeActivity.this);
//        ArrayList<JSONObject> tmpPins =  SPHelper.getInstance().getCache(PinsRequest.createCacheKey());
//        if (tmpPins == null) {
//            spiceManager.execute(request, PinsRequest.createCacheKey(),
//                    DurationInMillis.ALWAYS_RETURNED, new PinsRequestListener());
//        } else {
//            mPins = tmpPins;
//            notifyAboutPins();
//        }
        if(!requesting) {
            requesting=true;
            new RequestPinTask().execute();
        }
    }

    private class RequestPinTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            PinsRequest request = new PinsRequest(HomeActivity.this);

            notifyAboutPins();
            spiceManager.execute(request, new PinsRequestListener());
            return null;
        }
    }

    private void requestStatuses() {
        StatusesRequest request = new StatusesRequest(this);

        spiceManager.execute(request/*, request.createCacheKey(),
                DurationInMillis.ALWAYS_RETURNED*/, new StatusesRequestListener());
    }

    private void requestUsers() {
//        setProgressBarIndeterminateVisibility(true);
        UsersRequest request = new UsersRequest(this);
        // TODO requestUsers
        spiceManager.execute(request/*, request.createCacheKey(), DurationInMillis.ALWAYS_RETURNED*/, new UsersRequsetListenner());
    }

    private void notifyAboutPins() {
        Intent intent = new Intent();
        intent.setAction("com.spotio.Pins");
        sendBroadcast(intent);
    }

    private void notifyAboutStatuses() {
        Intent intent = new Intent();
        intent.setAction("com.spotio.Statuses");
        sendBroadcast(intent);
    }

    private void notifyAboutFilter() {
        Intent intent = new Intent();
        intent.setAction("com.spotio.Filter");
        sendBroadcast(intent);
    }

    private void clearCredentials() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit();
        editor.remove("EMAIL");
        editor.remove("PASSWORD");
        editor.remove("COMPANY");
        editor.apply();
        SPHelper.getInstance().clearCache(PinsRequest.createCacheKey());
        SPHelper.getInstance().clearCache("refresh");
        SPHelper.getInstance().clearCache("sharing");
        SPHelper.getInstance().clearFilter();
        SDSQLiteHelper.getInstance().clearAll();
        spiceManager.removeAllDataFromCache();
        notifyAboutPins();
    }

    private void showLogin() {
        track("Logout");
        clearCredentials();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void showDetails(String id) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("pinId", id);
        intent.putExtra(AddEditActivity.USER_LOCATION, mLocation);
        startActivityForResult(intent, 1218);
    }

    /**
     * Show the add pin screen
     *
     * @param location
     */
    private void showAddPin(Location location) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra("location", location);
        intent.putExtra(AddEditActivity.USER_LOCATION, mLocation);
        startActivity(intent);
    }

    private void showFilter() {
        Intent intent = new Intent(this, FilterActivity.class);
        startActivityForResult(intent, FILTER_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == FILTER_CODE) {
                notifyAboutPins();
            }else if (requestCode == 1218) {
                String s = data.getExtras().getString("pinId");
                pin = PINDAO.getInstance().get(s);
                //FragmentTabHost tabHost = (FragmentTabHost)  this.findViewById(android.R.id.tabhost);
                //tabHost.setCurrentTab(0);
                /*FragmentManager fragmentManager = this.getSupportFragmentManager();
                List<Fragment> fragments = fragmentManager.getFragments();
                for(Fragment fragment : fragments){
                    if(fragment != null && fragment.isVisible()) {
                        FragmentTabsFragmentSupport tabFragment = (FragmentTabsFragmentSupport) fragment;
                        tabFragment.setTab(0);
                    }
                }*/
                //String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
                //FragmentTabsFragmentSupport fragment = (FragmentTabsFragmentSupport) getSupportFragmentManager().findFragmentByTag(tag);
                //fragment.setTab(0);
                //FragmentTabHost tabHst = new FragmentTabHost(this);
                //tabHst.setCurrentTab(0);
                //notifyAboutPins();
            }
        }
    }

    @Override
    public void onStop() {
        locationManager.removeUpdates(this);
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        isStopped = true;
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, String name) {
        if (name.equals(mCurrentDrawerItemName)) {
            return;
        }
        if (name.equals(getString(R.string.title_section_logout))) {
            showLogin();
            return;
        }
        if (name.equals(getString(R.string.title_section_refresh))){
            requestPins();
            return;
        }
        mCurrentDrawerItemName = name;
        final Fragment fragment;
        if (position > 1 && position < 6) {
            fragment = GoToWebsiteFragment.newInstance(name, mGoToWebsiteTexts.get(position - 3));
        } else if (position == 6) {     /** Support */
            /*MobihelpConfig config = new MobihelpConfig(
                    "https://spotio.freshdesk.com",
                    "spotiodroid-1-0e56064bf9d572e1b14660f3e7df28ae",
                    "1ea99c7e2826dbcc4748296ab495c35df2478562");
            Mobihelp.init(HomeActivity.this, config);
            //Mobihelp.init(this,new MobihelpConfig("https://spotio.freshdesk.com", "spotiodroid-1-0e56064bf9d572e1b14660f3e7df28ae", "1ea99c7e2826dbcc4748296ab495c35df2478562"));
            Mobihelp.showSupport(this);*/

            final Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "support@spotio.com" });
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            return;
        } else if (position == 0) {
            fragment = new FragmentTabsFragmentSupport();
        } else {
            return;
//            fragment = PlaceholderFragment.newInstance(position, name);
        }

        // update the main content by replacing fragments
        h = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(!isStopped) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, fragment)
                            .commitAllowingStateLoss();
                }
            }
        };
        h.sendMessage(new Message());
//        if(mPins.isEmpty()) {
//        }
        try {
            Locale current = getResources().getConfiguration().locale;
            String displayCountry = current.getDisplayCountry();
            String displayName = current.getDisplayName();
            String displayLanguage = current.getDisplayLanguage();

            JSONObject jsonObject = new JSONObject(new JSONStringer().object()
                    .key("displayCountry").value(displayCountry)
                    .key("displayName").value(displayName)
                    .key("displayLanguage").value(displayLanguage)
                    .endObject().toString());
            track("Locale", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onSectionAttached(String title) {
        mTitle = title;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (h != null) {
            h.sendMessage(new Message());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.share:
                boolean allowed = SPHelper.getInstance().getSharer().getString("sharing", "0").equals("1");
                if(!allowed) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Share");
                    builder.setMessage("You do not have permission to share the reports.");
                    builder.show();
                    return true;
                }
                final HomeActivity home = this;
                CharSequence colors[] = new CharSequence[] {/*"Text",*/ "Email"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Share with");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            new WritePinsToSpreadSheet().execute();
                        }
                        // the user clicked on colors[which]
                });
                builder.show();
                /*clickedLocation = mLocation;
                Double lat = Double.parseDouble(decimalFormat.format(mLocation.getLatitude()));
                Double longitude = Double.parseDouble(decimalFormat.format(mLocation.getLongitude()));

                for (JSONObject jsonObject : mPins) {
                    Double pinedLongitude = jsonObject.optDouble("Longitude");
                    Double pinedLatitude = jsonObject.optDouble("Latitude");
                    pinedLongitude = Double.parseDouble(decimalFormat.format(pinedLongitude));
                    pinedLatitude = Double.parseDouble(decimalFormat.format(pinedLatitude));
                    if (Math.abs(lat - pinedLatitude) < DELTA_AROUND_USER_CURENT_LOCATION && Math.abs(longitude - pinedLongitude) < DELTA_AROUND_USER_CURENT_LOCATION) {
                        // TODO go to edit
    //                    Intent intent = new Intent(this, AddEditActivity.class);
    //                    intent.putExtra("pin", jsonObject.toString());
    //                    intent.putExtra(AddEditActivity.USER_LOCATION, mLocation);
    //                    startActivity(intent);
                        showDetails(jsonObject);
                        return true;
                    }
                }
                showAddPin(mLocation);*/
                return true;
            case R.id.action_filter:
                showFilter();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private Uri writeToSpreadShit(String combinedString){
        File file = null;
        File root = Environment.getExternalStorageDirectory();
        if (root.canWrite()) {
            File dir = new File(root.getAbsolutePath() + "/SpreadData");
            dir.mkdirs();
            file = new File(dir, "Spreadsheet.csv");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.write(combinedString.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Uri.fromFile(file);
    }

    private String getFilter() {
        String w="";
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            JSONArray a = new JSONArray(sharedPreferences.getString("FILTER_USERS", "[]"));
            if(a.length()>0) {
                if(!w.equals("")) w+= " AND ";
                w+="(";
                for (int i = 0; i < a.length(); ++i) {
                    if(i>0) w+= " OR ";
                    w+="UserName == '"+a.getString(i)+"'";
                }
                w+=")";
            }
            a = new JSONArray(sharedPreferences.getString("FILTER_STATUSES", "[]"));
            if(a.length()>0) {
                if(!w.equals("")) w+= " AND ";
                w+="(";
                for (int i = 0; i < a.length(); ++i) {
                    if(i>0) w+= " OR ";
                    w+="Status == '"+a.getString(i)+"'";
                }
                w+=")";
            }
            String start = sharedPreferences.getString("FILTER_DATE_FROM", null);
            if(start!=null) {
                if(!w.equals("")) w+= " AND ";
                w += "UpdateDate >= '" + start + "'";
            }
            String end = sharedPreferences.getString("FILTER_DATE_TO", null);
            if(end!=null) {
                if(!w.equals("")) w+= " AND ";
                w += "UpdateDate <= '" + end + "'";
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return w;
    }

    private Cursor getFreshCursor() {
        return PINDAO.getInstance().getDbHelper().rawSelect(PINTable.TABLE_NAME, null, getFilter(), null, null, null, "UpdateDate DESC");
    }

    public Location getClickedLocation() {
        return clickedLocation;
    }

    public void mapClicked(LatLng latLng) {
        clickedLocation = new Location("");
        clickedLocation.setLatitude(latLng.latitude);
        clickedLocation.setLongitude(latLng.longitude);
        showAddPin(clickedLocation);
    }

    // PinListFragment
    @Override
    public void onPinSelected(String id) {
        showDetails(id);
    }

    private void setClickedLocationPIN(PINObject pin) {
        double lat = pin.Latitude;
        double lng = pin.Longitude;
        clickedLocation = new Location("");
        clickedLocation.setLatitude(lat);
        clickedLocation.setLongitude(lng);
    }

    // MapFragment
    @Override
    public void onMapPinSelected(PINObject pin) {
        setClickedLocationPIN(pin);
        showDetails(pin.Id);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /*public void cameraChanged(Location location, int newZoom) {
        mapLocation = location;
        deltaZoom = 21 - newZoom;
    }*/

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public Location getmLocation() {
        return mLocation;
    }

//    private class HandlePinsTask extends AsyncTask<JSONArray, Void, Map> {
//        private Context mContext;
//
//        public HandlePinsTask(Context context) {
//            mContext=context;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            HomeActivity.this.setProgressBarIndeterminateVisibility(true);
//        }
//
//        @Override
//        protected Map doInBackground(JSONArray... pinsa) {
//            JSONArray pins=pinsa[0];
//            Map map = new HashMap<String, JSONObject>();
//            map.putAll(mPinsMap);
//            for (int i = 0; i < pins.length(); i++) {
//                JSONObject pin=pins.optJSONObject(i);
//                map.put(pin.optString("Id"), pin);
//            }
//
//            ArrayList<JSONObject> list = new ArrayList<JSONObject>();
//            list.addAll(map.values());
//            SPHelper.getInstance().saveCache(PinsRequest.createCacheKey(), list);
//            return map;
//        }
//
//        @Override
//        protected void onPostExecute(Map map) {
//            mPinsMap=map;
//            mPins.clear();
//            mPins.addAll(mPinsMap.values());
//            notifyAboutPins();
//            HomeActivity.this.setProgressBarIndeterminateVisibility(false);
//        }
//    }
    private class HandleStatusesTask extends AsyncTask<JSONArray, Void, ArrayList<String>> {
        private Context mContext;

        public HandleStatusesTask(Context context) {
            mContext=context;
        }

        @Override
        protected ArrayList<String> doInBackground(JSONArray... pinsa) {
            JSONArray statuses=pinsa[0];
            ArrayList<JSONObject> tmpList = new ArrayList<JSONObject>();

            for (int i = 0; i < statuses.length(); i++) {
                tmpList.add(statuses.optJSONObject(i));
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

            for (int i = 0; i < tmpList.size(); i++) {
                JSONObject status = tmpList.get(i);
                boolean isActive = status.optBoolean("IsActive");
                if (isActive) {
                    String s = status.optString("Name");
                    mStatuses.add(s);
                    if (!mColors.containsKey(s)) {
                        mColors.put(s, status.optString("Color"));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            notifyAboutStatuses();
//            HomeActivity.this.setProgressBarIndeterminateVisibility(false);
        }
    }


    private class PinsRequestListener implements
            RequestListener<JSONObject> {
        int count = 0;

        public PinsRequestListener() {
            count = 0;
        }

        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(HomeActivity.this,
                    "Error during request: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
            mRefreshListener.onRefreshEnds();
            HomeActivity.this.pd.dismiss();
            requesting=false;
        }

        @Override
        public void onRequestSuccess(JSONObject response) {
//            HomeActivity.this.setProgressBarIndeterminateVisibility(false);
//            HomeActivity.this.pd.dismiss();
//            requesting=false;
        }
    }


    private class StatusesRequestListener implements
            RequestListener<JSONObject> {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(HomeActivity.this,
                    "Error during request: " + e.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
//            HomeActivity.this.setProgressBarIndeterminateVisibility(false);
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

            new HandleStatusesTask(HomeActivity.this).execute(statuses);
        }
    }

    private class UsersRequsetListenner implements RequestListener<JSONObject> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(HomeActivity.this,
                    "Error during request: " + spiceException.getLocalizedMessage(), Toast.LENGTH_LONG)
                    .show();
//            HomeActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onRequestSuccess(JSONObject jsonObject) {

        }
    }

    public static class FragmentTabsFragmentSupport extends Fragment {

        private static final String STATE_SELECTED_TAB = "selected_tab";

        private FragmentTabHost mTabHost;
        private int mCurrentSelectedTab = 0;

        public FragmentTabsFragmentSupport() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                mCurrentSelectedTab = savedInstanceState.getInt(STATE_SELECTED_TAB);
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mTabHost.setCurrentTab(mCurrentSelectedTab);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mTabHost = new FragmentTabHost(getActivity());
            mTabHost.setup(getActivity(), getChildFragmentManager(), R.layout.fragment_tabs);

            mTabHost.addTab(mTabHost.newTabSpec("map").setIndicator("Map"),
                    MapFragment.class, null);
            mTabHost.addTab(mTabHost.newTabSpec("list").setIndicator("List"),
                    PinsListFragment.class, null);
            mTabHost.requestTransparentRegion(mTabHost);

            TabWidget widget = mTabHost.getTabWidget();
            for (int i = 0; i < widget.getChildCount(); i++) {
                View v = widget.getChildAt(i);

                // Look for the title view to ensure this is an indicator and not a divider.
                TextView tv = (TextView) v.findViewById(android.R.id.title);
                if (tv == null) {
                    continue;
                }
                v.setBackgroundResource(R.drawable.tab_indicator_ab_spotio);
            }
            mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    CommonHelpers.hideKeyboard(getActivity());
                    if (tabId.equals("map")) {
                        mCurrentSelectedTab = 0;
                    } else {
                        mCurrentSelectedTab = 1;
                    }
                }
            });

            return mTabHost;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(STATE_SELECTED_TAB, mCurrentSelectedTab);
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mTabHost = null;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((HomeActivity) activity).onSectionAttached("Spotio");
//            ((HomeActivity) activity).onSectionAttached("");
        }
    }

    public class PinsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(intent.getAction().equals("com.spotio.PinsEnd")){
                    mRefreshListener.onRefreshEnds();
                    pd.dismiss();
                    requesting=false;
                    return;
                }


                JSONObject pinjson = new JSONObject(intent.getStringExtra("pin"));
//                PINObject pin=PinsRequest.pinFromJSON(pinjson);
                JSONArray arr = new JSONArray();
                arr.put(pinjson);
                PINDAO.getInstance().insert(arr);
                PINObject pin = PINDAO.getInstance().get(pinjson.getString("Id"));
                pin.toString();
//                mPinsMap.put(pinjson.optString("Id"), pinjson);
//                mPins=new ArrayList<JSONObject>(mPinsMap.values());
//                mPins.addAll(mPinsMap.values());
//                SPHelper.getInstance().saveCache(PinsRequest.createCacheKey(), mPins);
                notifyAboutPins();
                requestPins();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // TODO PinsReceiver

        }
    }

    public class FanOutReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String extra=intent.getStringExtra("what");
            if(extra.equals("pin")) {
                requestPins();
            }else if(extra.equals("users")) {
                requestUsers();
            }else if(extra.equals("statuses")) {
                requestStatuses();
            }else if(extra.equals("fields")) {
                spiceManager.removeDataFromCache(FieldsRequest.class);
            }
        }
    }

    private class WritePinsToSpreadSheet extends AsyncTask<Void, Integer, String>{

        private ProgressDialog processPinsDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            processPinsDialog = new ProgressDialog(HomeActivity.this);
            processPinsDialog.setMessage(getString(R.string.processing_pins));
            processPinsDialog.setCancelable(false);
            processPinsDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            StringBuilder combinedString = new StringBuilder("\"Status\"," +
                    "\"Address\"," +
                    "\"City\"," +
                    "\"State\"," +
                    "\"Zip\"," +
                    "\"Name\"," +
                    "\"Phone\"," +
                    "\"Email\"," +
                    "\"Notes\"," +
                    "\"Created Date\"," +
                    "\"Created Time\"," +
                    "\"Last Updated Date\"," +
                    "\"Last Updated Time\"," +
                    "\"User Name\"");

                    /*Double lat = Double.parseDouble(decimalFormat.format(mapLocation.getLatitude()));
                    Double longitude = Double.parseDouble(decimalFormat.format(mapLocation.getLongitude()));
                    double delta = DELTA_AROUND_USER_CURENT_LOCATION;
                    for(int j = 0; j<deltaZoom; j++)
                        delta = 2 * delta;*/

            Cursor cursor = getFreshCursor();
            int count = 0;
            while (cursor.moveToNext() && count++ < MAX_PINS_COUNT_FOR_SHARE) {
                publishProgress(cursor.getPosition(),MAX_PINS_COUNT_FOR_SHARE);
                PINObject pin = PINDAO.getInstance().parser(cursor);
                //Double pinedLongitude = jsonObject.optDouble("Longitude");
                //Double pinedLatitude = jsonObject.optDouble("Latitude");
                //pinedLongitude = Double.parseDouble(decimalFormat.format(pinedLongitude));
                //pinedLatitude = Double.parseDouble(decimalFormat.format(pinedLatitude));
                //if (Math.abs(lat - pinedLatitude) < delta && Math.abs(longitude - pinedLongitude) < delta) {

                String[] custom = {"","","",""};
                int index=0;
                if (pin.CustomValues != null) {
                    for (CustomValuesObject cvo : pin.CustomValues) {
                        if (index < 4) {
                            custom[index] = cvo.StringValue;
                            index++;
                        } else {
                            break;
                        }
                    }
                }

                String[] creation = pin.CreationDate.split("T");
                String[] update = pin.UpdateDate.split("T");
                if(update[0].equals("")) {
                    update = new String[2];
                    update[0]=""; update[1]="";
                }

                String dataString = "\"" + pin.Status + "\",\"" + pin.Location.HouseNumber
                        + " " + pin.Location.Street + "\",\"" + pin.Location.City + "\",\""
                        + pin.Location.State + "\",\"" + pin.Location.Zip;
                dataString += "\",\"" + custom[0] + "\",\"" + custom[1] + "\",\"" + custom[2] + "\",\"" + custom[3] + "\",\"" + creation[0] + "\",\"" + creation[1] + "\",\"" + update[0] + "\",\"" + update[1] + "\",\"" + pin.UserName + "\"";
                combinedString.append("\n").append(dataString);
                //}
            }
            return combinedString.toString();
        }

        @Override
        protected void onPostExecute(String combinedString) {
            super.onPostExecute(combinedString);
            if (processPinsDialog != null && processPinsDialog.isShowing()){
                processPinsDialog.dismiss();
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Spreadsheet");
            sendIntent.putExtra(Intent.EXTRA_STREAM, writeToSpreadShit(combinedString));
            sendIntent.setType("text/html");
            startActivity(sendIntent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            processPinsDialog.setMessage(getString(R.string.processing_pins) + " " + values[0] + " / " + values[1]);
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SECTION_NAME = "section_name";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String sectionName) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(ARG_SECTION_NAME, sectionName);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((HomeActivity) activity).onSectionAttached(
                    getArguments().getString(ARG_SECTION_NAME));
        }
    }

}
