package com.icanvass.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.icanvass.R;
import com.icanvass.activities.HomeActivity;
import com.icanvass.adapters.PinListCursorAdapter;
import com.icanvass.database.PINDAO;
import com.icanvass.database.PINTable;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class PinsListFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private String mParam1;
    private String mParam2;

    PinsReceiver mapReceiver;
    StatusesReceiver statusesReceiver;
    FilterReceiver filterReceiver;

    private OnFragmentInteractionListener mListener;
    private ListView lvPins;
    private TextView tvStatus, tvAddress, tvDate;
    private EditText etSearch;
    private Button btClear;
    private PinListCursorAdapter adapter;
    private String searchText="";
//    private ArrayList<JSONObject> objects;
    private HashMap<String, Integer> statusMap;

//    public static PinsListFragment newInstance(String param1, String param2) {
//        PinsListFragment fragment = new PinsListFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    private enum SortState {
        STATUS_ACS, STATUS_DCS, ADDRESS_ACS, ADDRESS_DCS, DATE_ACS, DATE_DCS
    }

    private SortState sortState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pin_list, container, false);
        initDatas();
        initComponents(rootView);
        initListeners();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        HomeActivity activity = (HomeActivity) getActivity();
//        objects = CommonHelpers.getFilteredPins(activity.mPins, getActivity());
//        Cursor cursor = PINDAO.getInstance().getDbHelper().rawSelect(PINTable.TABLE_NAME,null,null,null,null,null,getOrderBy());
        adapter = new PinListCursorAdapter(getActivity(),getFreshCursor(),0);
        lvPins.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("com.spotio.Pins");
        mapReceiver = new PinsReceiver();
        getActivity().registerReceiver(mapReceiver, filter);
        IntentFilter statusesFilter = new IntentFilter("com.spotio.Statuses");
        statusesReceiver = new StatusesReceiver();
        getActivity().registerReceiver(statusesReceiver, statusesFilter);
        IntentFilter filterFilter = new IntentFilter("com.spotio.Filter");
        filterReceiver = new FilterReceiver();
        getActivity().registerReceiver(filterReceiver, filterFilter);

    }

    Cursor getFreshCursor() {
        return PINDAO.getInstance().getDbHelper().rawSelect(PINTable.TABLE_NAME, null, getFilter(), null, null, null, getOrderBy());
    }

    private void initDatas() {
        statusMap = new HashMap<String, Integer>();
        for (int i = 0; i < HomeActivity.mStatuses.size(); i++)
            statusMap.put(HomeActivity.mStatuses.get(i), i);
        sortState = SortState.DATE_DCS;
    }

    private void initListeners() {
        tvStatus.setOnClickListener(this);
        tvAddress.setOnClickListener(this);
        tvDate.setOnClickListener(this);
        etSearch.addTextChangedListener(new TextWatcher(){

            public void afterTextChanged(Editable s){}

            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            public void onTextChanged(CharSequence s, int start, int before, int count){
                searchText = s.toString();
                Cursor cursor = getFreshCursor();
                adapter.changeCursor(cursor);
                adapter.notifyDataSetChanged();
            }
        });

        btClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                etSearch.setText(searchText = "");
                etSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        lvPins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPinSelected(adapter.getPinId(position));
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        // TODO onClick
        switch (view.getId()) {
            case R.id.tv_status:
                sortStatus();
                break;
            case R.id.tv_address:
                sortAddress();
                break;
            case R.id.tv_date:
                sortDate();
                break;
        }
        Cursor cursor = getFreshCursor();
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    private String getFilter() {
        String w="";
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
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

        if(!searchText.equals("")) {
            if(!w.equals("")) w+= " AND ";
            w += "StreetName || HouseNumber like '%" + searchText + "%'";
        }

        return w;
    }

    private String getOrderBy() {
        String o="";
        if (sortState == SortState.STATUS_ACS) {
            o="Status ASC";
        } else if (sortState == SortState.STATUS_DCS) {
            o="Status DESC";
        } else if (sortState == SortState.ADDRESS_ACS) {
            o = PINTable.collumns.StreetName.name() + " ASC," + PINTable.collumns.HouseNumber.name() + " ASC";
        } else if (sortState == SortState.ADDRESS_DCS) {
            o = PINTable.collumns.StreetName.name() + " DESC," + PINTable.collumns.HouseNumber.name() + " DESC";
        } else if (sortState == SortState.DATE_ACS) {
            o="UpdateDate ASC";
        } else if (sortState == SortState.DATE_DCS) {
            o="UpdateDate DESC";
        }
        return o;
    }


    private void sortStatus() {
        if (sortState == SortState.STATUS_ACS) {
            sortState = SortState.STATUS_DCS;
        } else {
            sortState = SortState.STATUS_ACS;
        }
        updateTitleSort();
//        Collections.sort(objects, new Comparator<JSONObject>() {
//            @Override
//            public int compare(JSONObject jsonObject, JSONObject jsonObject2) {
//                int order1 = statusMap.get(jsonObject.optString("Status"));
//                int order2 = statusMap.get(jsonObject2.optString("Status"));
//                if (sortState == SortState.STATUS_ACS) {
//                    return order2 - order1;
//                } else {
//                    return order1 - order2;
//                }
//            }
//        });
    }

    private void sortAddress() {
        if (sortState == SortState.ADDRESS_ACS) {
            sortState = SortState.ADDRESS_DCS;
        } else {
            sortState = SortState.ADDRESS_ACS;
        }
//        Collections.sort(objects, new Comparator<JSONObject>() {
//            @Override
//            public int compare(JSONObject jsonObject, JSONObject jsonObject2) {
//                int order1 = jsonObject.optJSONObject("Location").optInt("HouseNumber");
//                int order2 = jsonObject2.optJSONObject("Location").optInt("HouseNumber");
//                if (sortState == SortState.ADDRESS_ACS) {
//                    return order1 - order2;
//                } else if (sortState == SortState.ADDRESS_DCS) {
//                    return order2 - order1;
//                }
//                return 0;
//            }
//        });
        updateTitleSort();
    }

    private void sortDate() {
        if (sortState == SortState.DATE_ACS) {
            sortState = SortState.DATE_DCS;
        } else {
            sortState = SortState.DATE_ACS;
        }
        updateTitleSort();
//        Collections.sort(objects, new Comparator<JSONObject>() {
//            @Override
//            public int compare(JSONObject jsonObject, JSONObject jsonObject2) {
//                String order1 = jsonObject.optString("DateTimeInputted");
//                String order2 = jsonObject2.optString("DateTimeInputted");
//
//                if (sortState == SortState.DATE_ACS) {
//                    return order1.compareToIgnoreCase(order2);
//                } else if (sortState == SortState.DATE_DCS) {
//                    return order2.compareToIgnoreCase(order1);
//                }
//                return 0;
//            }
//        });
    }

    private void updateTitleSort() {
        String acs = " \u25b2";
        String dcs = " \u25bc";
        String status = "Status";
        String address = "Address";
        String date = "Date";
        switch (sortState) {
            case STATUS_ACS:
                status += acs;
                break;
            case STATUS_DCS:
                status += dcs;
                break;
            case ADDRESS_ACS:
                address += acs;
                break;
            case ADDRESS_DCS:
                address += dcs;
                break;
            case DATE_ACS:
                date += acs;
                break;
            case DATE_DCS:
                date += dcs;
                break;
        }
        tvStatus.setText(status);
        tvAddress.setText(address);
        tvDate.setText(date);

    }

    private void initComponents(View rootView) {
        tvStatus = (TextView) rootView.findViewById(R.id.tv_status);
        tvAddress = (TextView) rootView.findViewById(R.id.tv_address);
        tvDate = (TextView) rootView.findViewById(R.id.tv_date);
        lvPins = (ListView) rootView.findViewById(R.id.lv_pins);
        etSearch = (EditText) rootView.findViewById(R.id.et_search);
        btClear = (Button) rootView.findViewById(R.id.bt_clear);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PinsListFragment() {
    }



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mapReceiver);
        getActivity().unregisterReceiver(statusesReceiver);
        getActivity().unregisterReceiver(filterReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public class PinsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }

    public class FilterReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            refresh();
        }
    }

    public class StatusesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    }

    private void refresh() {
        if(adapter==null) { //we got a broadcast but list is not there, probably too many broadcasts
            return;
        }
//        HomeActivity activity = (HomeActivity) getActivity();
//        adapter.clear();
//        List<JSONObject> objects = CommonHelpers.getFilteredPins(activity.mPins, getActivity());
//        for (JSONObject o : objects) {
//            adapter.add(o);
//        }
        Cursor cursor = getFreshCursor();
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
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
        public void onPinSelected(String id);
    }
}
