package com.icanvass.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.icanvass.R;
import com.icanvass.helpers.SDDefine;
import com.icanvass.objects.ListUsers;
import com.icanvass.objects.LocationObject;
import com.icanvass.objects.PINObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Justin CAO on 6/2/2014.
 */
public class SDInfoWindowAdapter
        implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private PINObject pin;
    private int color;
    private View statusView;
    private TextView tvStatus, tvUserName, tvAddressFirst, tvAddressSecond, tvDateCreated;

    public SDInfoWindowAdapter(Context context, PINObject pin, int color) {
        this.context = context;
        this.pin = pin;
        this.color = color;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.info_window_map, null);
        initViews(rootView);
        setDatas();
        return rootView;
    }

    private void initViews(LinearLayout rootView) {
        statusView = rootView.findViewById(R.id.status_view);
        tvStatus = (TextView) rootView.findViewById(R.id.tv_status);
        tvUserName = (TextView) rootView.findViewById(R.id.tv_uesr_name);
        tvDateCreated = (TextView) rootView.findViewById(R.id.tv_date_created);
        tvAddressFirst = (TextView) rootView.findViewById(R.id.tv_address_first);
        tvAddressSecond = (TextView) rootView.findViewById(R.id.tv_address_second);
    }

    private void setDatas() {
        SimpleDateFormat oldFormat = SDDefine.serverFormat;
        SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yy 'at' h:mma");
        String status = pin.Status;
        String userName = ListUsers.getUsernameByEmail(pin.UserName);
        String created;
        try {
            String createdStr = pin.CreationDate;
            Date date = oldFormat.parse(createdStr);
            created = newFormat.format(date);
            tvDateCreated.setText(created);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        LocationObject address = pin.Location;

        if (address != null) {
            String[] strs = new String[2];
            strs[0] = ""+address.HouseNumber;
            strs[1] = address.Street;
            StringBuilder builder = new StringBuilder();
            for (String str : strs) {
                if (!str.equalsIgnoreCase("null"))
                    builder.append(" " + str);
            }
            String addressFirst = builder.toString();


            strs[0] = address.State;
            strs[1] = address.City;
            builder = new StringBuilder();
            for (String str : strs) {
                if (!str.equalsIgnoreCase("null"))
                    builder.append(" " + str);
            }
            String addressSecond = builder.toString();
            tvAddressFirst.setText(addressFirst);
            tvAddressSecond.setText(addressSecond);
        }else {
            tvAddressFirst.setVisibility(View.GONE);
            tvAddressSecond.setVisibility(View.GONE);
        }

        statusView.setBackgroundColor(color);
        tvStatus.setText(status);
        tvUserName.setText(userName);
    }
}
