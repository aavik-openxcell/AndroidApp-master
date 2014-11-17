package com.icanvass.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.icanvass.R;
import com.icanvass.activities.HomeActivity;
import com.icanvass.helpers.SDDefine;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by romek on 10.05.2014.
 */
public class PinsListAdapter extends ArrayAdapter<JSONObject> {

    public PinsListAdapter(Context context, List<JSONObject> objects, Map<String, String> colors) {
        super(context, R.layout.pin_list_item, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemCell cell = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pin_list_item, null);
            cell = new ListItemCell(convertView);
            convertView.setTag(cell);
        } else {
            cell = (ListItemCell) convertView.getTag();
        }
        JSONObject o = getItem(position);
        JSONObject location = o.optJSONObject("Location");
        if (location != null) {
            String houseNumber = location.optString("HouseNumber");
            String street = location.optString("Street");
            String address = "";
            if (!houseNumber.equalsIgnoreCase("null")) {
                address += houseNumber + " ";
            }
            if (!street.equalsIgnoreCase("null")) {
                address += street;
            }
            String bottomLine = "";
            if (!location.optString("City").equalsIgnoreCase("null")) {
                bottomLine += location.optString("City") + ", ";
            }
            if (!location.optString("State").equalsIgnoreCase("null")) {
                bottomLine += location.optString("State");
            }
            if (!location.optString("Zip").equalsIgnoreCase("null")) {
                bottomLine += ", " + location.optString("Zip");
            }
            String stringDate = o.optString("CreationDate");
            Date date = null;
            try {
                date = SDDefine.simpleServerFormat.parse(stringDate);
                stringDate = SDDefine.localFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                stringDate = "";
            }
            cell.setDatas(o, address, bottomLine, stringDate);
        }

        return convertView;
    }

    private class ListItemCell {
        public TextView tvAddress, tvCity, tvTime;
        private View rectangle;

        public ListItemCell(View rootView) {
            tvAddress = (TextView) rootView.findViewById(R.id.tvAddress);
            tvCity = (TextView) rootView.findViewById(R.id.tvCity);
            tvTime = (TextView) rootView.findViewById(R.id.tvDateTime);
            rectangle = rootView.findViewById(R.id.rectangle_background);
        }

        public void setDatas(JSONObject object, String address, String city, String time) {
            if (!address.isEmpty())
                tvAddress.setText(address);
            if (!city.isEmpty())
                tvCity.setText(city);
            if (!time.isEmpty())
                tvTime.setText(time);

            String status = object.optString("Status");
            if (HomeActivity.mColors.containsKey(status)) {
                rectangle.setBackgroundColor(Color.parseColor(HomeActivity.mColors.get(status)));
            }
        }
    }
}
