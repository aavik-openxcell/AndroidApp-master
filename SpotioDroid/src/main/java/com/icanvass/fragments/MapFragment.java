package com.icanvass.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.icanvass.R;
import com.icanvass.abtracts.CustomClusterRenderer;
import com.icanvass.activities.HomeActivity;
import com.icanvass.adapters.SDInfoWindowAdapter;
import com.icanvass.database.PINDAO;
import com.icanvass.objects.AbstractMarker;
import com.icanvass.objects.PINObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapFragment extends SupportMapFragment implements LocationListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMyLocationButtonClickListener {

    public static final String TAG = "MapFragment";
    private static final float CLUSTERING_ZOOM_OUT_THRESHOLD = 12;
    private LocationManager locationManager;
    private PinsReceiver pinsReceiver;

    private OnFragmentInteractionListener mListener;
    private MapReceiver mapReceiver;
    public HashMap<Marker, String> markerPins = new HashMap<Marker, String>();
    private int zoomLevel = 21;
    private boolean isMyLocation = false;
    private boolean isShowingInfoWindow;
//    public boolean b = false;
    private Drawable drawable;

    private Handler mHandler=new Handler();
    private Runnable mRunnable;

    private PINObject autoPIN;
    private PINObject infoPIN;
    private boolean doNotRefreshThisTime;
    private boolean isStatusesLoaded = false;

    private CameraPosition lastCameraPosition;
    private Location initial;
    boolean located;
    boolean clusteringAllowed = true;

    private ClusterManager<AbstractMarker> clusterManager;
    public static int pinsFinalCount=0;
    private float maxZoomLevel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"=======onCreate========");
        drawable = getResources().getDrawable(R.drawable.pin);
        IntentFilter mapFilter = new IntentFilter("com.spotio.Satellite");
        mapReceiver = new MapReceiver();
        getActivity().registerReceiver(mapReceiver, mapFilter);
        IntentFilter pinsFilter = new IntentFilter("com.spotio.Pins");
        pinsFilter.addAction("com.spotio.Statuses");
        pinsReceiver = new PinsReceiver();
        getActivity().registerReceiver(pinsReceiver, pinsFilter);

        HomeActivity homeActivity=(HomeActivity)getActivity();
        initial=homeActivity.getmLocation();
//        GeoPoint newCurrent = new GeoPoint(23.034049,72.510826);
//        initial = new Location("reverseGeocoded");
//        initial.setLatitude(23.034049);
//        initial.setLongitude(72.510826);
//        initial.setAccuracy(10);
//        initial.setBearing(96);

//        initial = "Location[gps 23.034049,72.510826 acc=10 et=+1d5h49m9s438ms alt=13.0 vel=2.3584952 bear=96.0 {Bundle[mParcelledData.dataSize=44]}]";
        Log.e(TAG,"=======initial========"+initial);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG,"=========onActivityCreated==========");
        GoogleMap map = getMap();
        if (map != null) {
            //Your initialization code goes here
            map.setOnMyLocationButtonClickListener(this);

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    HomeActivity home = (HomeActivity) getActivity();
                    String pinId = markerPins.get(marker);
                    Log.e(TAG,"=======onActivityCreated,pinId========"+pinId);
                    PINObject pinObject = PINDAO.getInstance().get(pinId);
                    //todo pinojbect can be null here, fast fix, should understand why
                    if (pinObject!=null)
                        home.onMapPinSelected(pinObject);
                }
            });
            Log.e(TAG,"max zoom level=>"+map.getMaxZoomLevel());
            maxZoomLevel = map.getMaxZoomLevel();
            setupClusterManager();
            map.setOnCameraChangeListener(this);
        }
    }


    private void setupClusterManager(){
        Log.e(TAG,"=========setupClusterManager==========");
        if(clusterManager!=null) return;

        Log.e(TAG,"=========setupClusterManager,clusterManager=========="+clusterManager);
        clusterManager = new ClusterManager<AbstractMarker>(getActivity(), getMap());
        clusterManager.setRenderer(new StatusMarkerIconRenderer(
                getActivity(), getMap(), clusterManager));

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<AbstractMarker>() {
            @Override
            public boolean onClusterItemClick(AbstractMarker abstractMarker) {
                Log.e(TAG,"==========setOnClusterItemClickListener===========");
                onMarkerClick(abstractMarker.getAssociatedMarker());
                return true;
            }
        });

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<AbstractMarker>() {
            @Override
            public boolean onClusterClick(Cluster<AbstractMarker> abstractMarkerCluster) {
                Log.e(TAG,"=======setOnClusterClickListener======");
//                Iterator iterator = abstractMarkerCluster.getItems().iterator();
//
//                ClusterMarkersItem notContacted  = new ClusterMarkersItem();
//                ClusterMarkersItem notHome       = new ClusterMarkersItem();
//                ClusterMarkersItem notInterested = new ClusterMarkersItem();
//                ClusterMarkersItem Lead          = new ClusterMarkersItem();
//                ClusterMarkersItem Sold          = new ClusterMarkersItem();
//
//                while (iterator.hasNext()){
//                    AbstractMarker marker = (AbstractMarker) iterator.next();
//                    PINObject pin = PINDAO.getInstance().get(marker.getPinId());
//                    if ("NotContacted".equals(pin.Status)) {
//                        notContacted.increment();
//                        notContacted.setStatus(pin.Status);
//                    }
//                    if ("NotInterested".equals(pin.Status)) {
//                        notInterested.increment();
//                        notInterested.setStatus(pin.Status);
//                    }
//                    if ("NotHome".equals(pin.Status)) {
//                        notHome.increment();
//                        notHome.setStatus(pin.Status);
//                    }
//                    if ("Lead".equals(pin.Status)) {
//                        Lead.increment();
//                        Lead.setStatus(pin.Status);
//                    }
//                    if ("Sold".equals(pin.Status)) {
//                        Sold.increment();
//                        Sold.setStatus(pin.Status);
//                    }
//                }
//                ArrayList<ClusterMarkersItem> items = new ArrayList<ClusterMarkersItem>();
//                items.add(notContacted);
//                items.add(notInterested);
//                items.add(notHome);
//                items.add(Sold);
//                items.add(Lead);
//                clusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(new ClusterInfoWindow(items));
//                return false;
                return true;
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"=========onDestroy==========");
        getActivity().unregisterReceiver(mapReceiver);
        getActivity().unregisterReceiver(pinsReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"=========onStart==========");
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 5, this);

        setUpMapIfNeeded();
        setMapStyle();

        if(initial!=null) {
            moveCamera(initial, null);
            initial=null;
        }
//        if(!b) {
//            setPosition();
//            b = true;
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG,"=========onStop==========");
        locationManager.removeUpdates(this);
    }

    private void setUpMapIfNeeded() {
        Log.e(TAG,"======setUpMapIfNeeded========");
        GoogleMap map = getMap();
        if (map == null) return;


    }

//    private void setPosition() {
//        HomeActivity activity = (HomeActivity) getActivity();
//        Location location = activity.getmLocation();
//        moveCamera(location);
//    }

    private void setMapStyle() {
        Log.e(TAG,"======setMapStyle========");
        GoogleMap map = getMap();
        if (map == null) return;
        map.setMyLocationEnabled(true);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean on = sharedPreferences.getString("SATELLITE", null) != null;
        Log.e(TAG,"======setMapStyle========"+on);
        map.setMapType(on ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.e(TAG,"======onAttach========");
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG,"======onLocationChanged========");

        if(autoPIN==null && infoPIN==null && !located) {

            moveCamera(location, null);
            located=true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"==========onResume===========");
        HomeActivity activity = (HomeActivity) getActivity();
        if (autoPIN == null && infoPIN == null && activity != null && activity.getClickedLocation() != null/* && !b*/) {
//            moveCamera(activity.getClickedLocation());
        }
        if(getMap()!=null) {
            getMap().setOnMarkerClickListener(clusterManager);
            getMap().setOnMapClickListener(this);
        }
        refresh(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG,"======onPause========");
        if (curDrawTask!=null) curDrawTask.cancel(true);
        if(getMap()!=null) {
            getMap().setOnMarkerClickListener(null);
            getMap().setOnMapClickListener(null);
        }
    }

    public void moveCamera(Location location, Integer zoom) {
        GoogleMap map = getMap();
        Log.e(TAG,"======moveCamera========");
        if (map == null) return;

//        map.setOnMarkerClickListener(this);
//        map.setOnMapClickListener(this);
        if (location != null) {
            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            map.moveCamera(center);
            Log.e(TAG,"======moveCamera,value of zoom========"+(zoom == null ? zoomLevel : zoom));
            map.animateCamera(CameraUpdateFactory.zoomTo(zoom == null ? zoomLevel : zoom));
            //locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        isMyLocation = true;
        located = false;
        return false;
    }

    private Marker getMarkerByPinId(String pinId){
        Log.e(TAG,"======getMarkerByPinId========");
        for (Map.Entry entry : markerPins.entrySet()){
            if (entry.getValue().equals(pinId)){
                return (Marker) entry.getKey();
            }
        }
        return null;
    }

    public boolean autoNavigateToPinId(String pinid) {
        Log.e(TAG,"======autoNavigateToPinId========"+pinid);
        initial = null;
        PINObject pin=PINDAO.getInstance().get(pinid);
        autoPIN = pin;
        double lat = pin.Latitude;
        double lng = pin.Longitude;
        Log.e(TAG,"======autoNavigateToPinId,lat-long========"+lat+','+lng);
        Location clickedLocation = new Location("");
        clickedLocation.setLatitude(lat);
        clickedLocation.setLongitude(lng);
//        moveCamera(clickedLocation, 14);
        moveCamera(clickedLocation, (int)maxZoomLevel);

        return true;
    }
    private Marker lastMarker;
    @Override
    public boolean onMarkerClick(Marker marker) {
//        Log.e(TAG,"========onMarkerClick,marker shown========="+marker.isInfoWindowShown());
        GoogleMap map = getMap();
        // TODO onMarkerClick
        if(marker.isInfoWindowShown()){
            marker.hideInfoWindow();
            isShowingInfoWindow = false;
            infoPIN = null;
        }
        getMap().setInfoWindowAdapter(null);
        int color = Color.BLACK;
        String pinid = markerPins.get(marker);
        PINObject pin = PINDAO.getInstance().get(pinid);
//        autoPIN=pin;
        if(pin!=null) {
            String status = pin.Status;
            if (!HomeActivity.mColors.isEmpty()) {
                String hex = HomeActivity.mColors.get(status);
                if (hex != null) {
                    color = Color.parseColor(hex);
                }
            }
        }

        if (pin == null){
            return false;
        }
        double lat = pin.Latitude;
        double lng = pin.Longitude;
        doNotRefreshThisTime =true;

//        original
//        CameraUpdate center =
//                CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
//        map.animateCamera(center);
//        map.setInfoWindowAdapter(new SDInfoWindowAdapter(getActivity(), pin, color));
//        marker.showInfoWindow();
//        infoPIN=pin;
//        isShowingInfoWindow = true;
//        return true;


        Log.e(TAG,"========onMarkerClick,isShowingInfoWindow========"+isShowingInfoWindow   );

        if(lastMarker == null){
                        CameraUpdate center =
                    CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
            map.animateCamera(center);
            map.setInfoWindowAdapter(new SDInfoWindowAdapter(getActivity(), pin, color));
            marker.showInfoWindow();
            infoPIN=pin;
            lastMarker = marker;
            isShowingInfoWindow=true;
        }else if (marker.getId().equals(lastMarker.getId())) {
            if (isShowingInfoWindow) {
                marker.hideInfoWindow();
                isShowingInfoWindow = false;
            } else {
                CameraUpdate center =
                        CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
                map.animateCamera(center);
                map.setInfoWindowAdapter(new SDInfoWindowAdapter(getActivity(), pin, color));
                marker.showInfoWindow();
                infoPIN=pin;
                isShowingInfoWindow = true;
            }
        }
        else{
            //это щелчок по другому маркеру
            if (isShowingInfoWindow) {//если открыто инфовиндов предыдущего маркера, скрываем его
                lastMarker.hideInfoWindow();
                //и отображаем для нового
                CameraUpdate center =
                        CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
                map.animateCamera(center);
                map.setInfoWindowAdapter(new SDInfoWindowAdapter(getActivity(), pin, color));
                marker.showInfoWindow();
                infoPIN=pin;
                isShowingInfoWindow = true;
                lastMarker = marker;
            } else {
                CameraUpdate center =
                        CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
                map.animateCamera(center);
                map.setInfoWindowAdapter(new SDInfoWindowAdapter(getActivity(), pin, color));
                marker.showInfoWindow();
                infoPIN=pin;
                isShowingInfoWindow = true;
                lastMarker = marker;
            }
        }
        return true;

    }

    @Override
    public void onMapClick(LatLng latLng) {
        // TODO onMapClick
        Log.e(TAG,"======onMapClick========");
        if (isShowingInfoWindow) {
            isShowingInfoWindow = false;
            infoPIN=null;
        }
        /*else {

            HomeActivity home = (HomeActivity) getActivity();
            home.mapClicked(latLng);
        }*/

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        /** Uncomment to prevent updating pins on zoom without changing camera position*/
//        float[] results=new float[]{0};
//        if(lastCameraPosition!=null) {
//            Location.distanceBetween(cameraPosition.target.latitude, cameraPosition.target.longitude, lastCameraPosition.target.latitude, lastCameraPosition.target.longitude, results);
//            if (results[0] < 100) {
//                Log.i(TAG, "refresh");
//                return;
//            }
//        }

        Log.e(TAG,"======onCameraChange========");
        lastCameraPosition = cameraPosition;
        int newZoom = (int) cameraPosition.zoom;
        if(isStatusesLoaded && !doNotRefreshThisTime || autoPIN != null) {
            Log.e(TAG,"======onCameraChange,go to refresh========");
                refresh(false);

        }
        if(isMyLocation)
        {
            zoomLevel = 21;
            //getMap().animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));
            isMyLocation = false;
        }
        else
            zoomLevel = newZoom;
        doNotRefreshThisTime =false;

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onMapPinSelected(PINObject pin);
    }

    public class MapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"=======MapReceiver,onReceive========");
            setMapStyle();
        }
    }

    public class PinsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"======PinsReceiver,onReceive========");
            isStatusesLoaded = true;
            refresh(intent.getAction().equals("com.spotio.Statuses") ||
                    intent.getAction().equals("com.spotio.Pins"));
        }
    }

    private String getFilter() {
        Log.e(TAG,"======getFilter========");
        String w="Latitude > ? AND Latitude < ? AND Longitude > ? AND Longitude < ?";
        try {
            if (getActivity() == null) return w;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            JSONArray a = new JSONArray(sharedPreferences.getString("FILTER_USERS", "[]"));
            if(a.length()>0) {
                w+="AND (";
                for (int i = 0; i < a.length(); ++i) {
                    if(i>0) w+= " OR ";
                    w+="UserName == '"+a.getString(i)+"'";
                }
                w+=")";
            }
            a = new JSONArray(sharedPreferences.getString("FILTER_STATUSES", "[]"));
            if(a.length()>0) {
                w+="AND (";
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

    private void refresh(final boolean clear) {
        GoogleMap map = getMap();
        if (map == null) return;
        Log.e(TAG,"======refresh,clear========"+clear);
        if(clear) {
            bitmaps.clear();
            map.clear();
        }
        Log.e(TAG,"======refresh========");
        drawPins();
    }

    private AsyncTask curDrawTask;

    private void drawPins() {
        Log.e(TAG,"======drawPins========");
        GoogleMap map = getMap();
        if (map == null) return;
        //todo fast fix, claster manager can be null here
        if (clusterManager == null){
            Log.e(TAG,"======drawPins,clusterManager========"+clusterManager);
            setupClusterManager();
        }
        clusterManager.clearItems();
        if (curDrawTask!=null) curDrawTask.cancel(true);
        markerPins.clear();
        curDrawTask = new AsyncDrawPins(map).execute();
    }

    private class AsyncDrawPins extends AsyncTask<Void, PINObject, Void>{
        private VisibleRegion region;

        public AsyncDrawPins (GoogleMap map){
            region = map.getProjection().getVisibleRegion();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getActivity() != null ) {
                ((HomeActivity) getActivity()).showTopLeftProgress(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (isCancelled()) return null;
            Log.e(TAG,"======AsyncDrawPins,region========"+region);
            Cursor cursor = PINDAO.getMapMarkersCursor(getFilter(), region);
            if (cursor!=null) {
                int count = cursor.getCount();
                pinsFinalCount = count;
                Log.e(TAG,"======AsyncDrawPins,count========"+count);
                int i = count / 500;
                if (i == 0) i = 1;
                PINDAO.MapsMarkerCursorIds cursorIds = PINDAO.matchMapMarkerCursorIds(cursor);
                while (cursor.moveToNext()) {
                    if (isCancelled()) break;
                    PINObject pin = PINDAO.getInstance().getMapMarkerInfo(cursor, cursorIds);
                    if (!markerPins.containsValue(pin.Id)) {
                        int position = cursor.getPosition();
//                        if (position % i == 0 || (autoPIN != null && pin.Id.equals(autoPIN.Id)) || (infoPIN != null && pin.Id.equals(infoPIN.Id))) {
                            String status = pin.Status;
//                            Log.e(TAG,"======AsyncDrawPins,status========"+status);
                            int color = Color.BLACK;
                            if (!HomeActivity.mColors.isEmpty()) {
                                String hex = HomeActivity.mColors.get(status);
                                if (hex != null) {
                                    color = Color.parseColor(hex);
                                }
                            }
                            if (!bitmaps.containsKey(status)) {
                                Bitmap b = drawableToBitmap(drawable);
                                b = replaceColor(b, Color.BLACK, color);
                                bitmaps.put(status, BitmapDescriptorFactory.fromBitmap(b));
                            }
                            if (isCancelled()) break;
                            publishProgress(pin);
//                        }
                    }
                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(PINObject... pins) {
            super.onProgressUpdate(pins);
            PINObject pin = pins[0];
            String status = pin.Status;
            LatLng latLng = new LatLng(pin.Latitude, pin.Longitude);
//            Log.e(TAG,"status=>"+status+",latLng=>"+latLng);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("nil")
                    .icon(bitmaps.get(status));
            AbstractMarker marker = new AbstractMarker(markerOptions);
            marker.setPinId(pin.Id);
            clusterManager.addItem(marker);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (clusterManager == null){
                setupClusterManager();
            }
            clusterManager.cluster();
            if (getActivity() != null) {
                ((HomeActivity) getActivity()).showTopLeftProgress(false);
            }
        }
    }

    static HashMap<String, BitmapDescriptor> bitmaps = new HashMap<String, BitmapDescriptor>();

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Log.e(TAG,"======drawableToBitmap========");
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize = 8;

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap replaceColor(Bitmap src, int fromColor, int targetColor) {
        Log.e(TAG,"======replaceColor========");
        if (src == null) {
            return null;
        }
        // Source image size
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        //get pixels
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int x = 0; x < pixels.length; ++x) {
            pixels[x] = (pixels[x] == fromColor) ? targetColor : pixels[x];
        }
        // create result bitmap output
        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        //set pixels
        result.setPixels(pixels, 0, width, 0, 0, width, height);

        return result;
    }

    public class StatusMarkerIconRenderer extends CustomClusterRenderer<AbstractMarker> {

        public StatusMarkerIconRenderer(Context context, GoogleMap map, ClusterManager<AbstractMarker> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(AbstractMarker item,
                                                   MarkerOptions markerOptions) {
//            Log.e(TAG,"======onBeforeClusterItemRendered========");
            markerOptions.icon(item.getMarker().getIcon());
        }

        @Override
        protected void onClusterItemRendered(AbstractMarker clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
            markerPins.put(marker, clusterItem.getPinId());
            clusterItem.setAssociatedMarker(marker);
            if (autoPIN != null && autoPIN.Id.equals(clusterItem.getPinId())){
                onMarkerClick(marker);
                autoPIN = null;
                //TODO refresh?
            }
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<AbstractMarker> cluster) {
//            Log.e(TAG,"======shouldRenderAsCluster========");
            if (lastCameraPosition != null) {
//              condition for cluster, if pins lenght are greater than 10 then its become a cluster.
//                boolean enoughItemsToCluster = cluster.getSize() >= 5;
                boolean enoughItemsToCluster = cluster.getSize() >= 10;
//                Log.e(TAG,"======enoughItemsToCluster========"+enoughItemsToCluster);

//                return lastCameraPosition.zoom < CLUSTERING_ZOOM_OUT_THRESHOLD && enoughItemsToCluster;
                return enoughItemsToCluster;
            } else {
                return super.shouldRenderAsCluster(cluster);
            }
        }
    }

    private class ClusterInfoWindow implements GoogleMap.InfoWindowAdapter {

        private ArrayList<ClusterMarkersItem> items;

        private ClusterInfoWindow(ArrayList<ClusterMarkersItem> items) {
            this.items = items;
        }

        @Override
        public View getInfoContents(Marker arg0) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
//            Log.e(TAG,"======getInfoWindow========");
            LayoutInflater inflater = (LayoutInflater) MapFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = inflater.inflate(R.layout.cluster_window, null);

            ListView lvClusterInfoWindow = (ListView) v.findViewById(R.id.lvClusterInfoWindow);
            lvClusterInfoWindow.setAdapter(new ClusterInfoWindowArrayAdapter(getActivity(),items));
            return v;
        }
    }

    private class ClusterInfoWindowArrayAdapter extends ArrayAdapter {

        private ArrayList<ClusterMarkersItem> items;

        public ClusterInfoWindowArrayAdapter(Context context, ArrayList<ClusterMarkersItem> items) {
            super(context, R.layout.item_cluster_info_window);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) MapFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = inflater.inflate(R.layout.item_cluster_info_window, parent, false);

            ImageView statusColor   = (ImageView) v.findViewById(R.id.ivStatusColor);
            TextView tvStatus       = (TextView) v.findViewById(R.id.tvStatusName);
            TextView tvMarkerCount  = (TextView) v.findViewById(R.id.tvMarkersCount);

            ClusterMarkersItem item = (ClusterMarkersItem) getItem(position);
            String status = item.getStatus();
            int color = Color.BLACK;
            if (!HomeActivity.mColors.isEmpty()) {
                String hex = HomeActivity.mColors.get(status);
                if (hex != null) {
                    color = Color.parseColor(hex);
                }
            }
            statusColor.setBackgroundColor(color);
            tvStatus.setText(status );
//            Log.e(TAG,"item=====>"+ item.getCount());
            tvMarkerCount.setText("" + item.getCount());

            return v;
        }
    }

    private class ClusterMarkersItem{

        private int mColor;
        private int mCount;
        private String mStatus;

        private ClusterMarkersItem() {
            mCount = 0;
        }

        public int getColor() {
            return mColor;
        }

        public int getCount() {
            return mCount;
        }

        public String getStatus() {
            return mStatus;
        }

        public void setColor(int color) {
            mColor = color;
        }

        public void increment() {
            mCount++;
        }

        public void setStatus(String status) {
            mStatus = status;
        }
    }
}
