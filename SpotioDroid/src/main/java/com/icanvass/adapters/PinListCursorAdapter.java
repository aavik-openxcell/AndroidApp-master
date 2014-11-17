package com.icanvass.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.icanvass.R;
import com.icanvass.activities.HomeActivity;
import com.icanvass.database.PINDAO;
import com.icanvass.helpers.SDDefine;
import com.icanvass.objects.LocationObject;
import com.icanvass.objects.PINObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by romek on 09.07.2014.
 */
public class PinListCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private ListView mListView;

    static class ViewHolder{
        View rectangle;
        TextView tvAddress;
        TextView tvCity;
        TextView tvTime;
    }

    public PinListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v =  mInflater.inflate(R.layout.pin_list_item, parent, false);

        mListView = (ListView) parent;
        ViewHolder holder = new ViewHolder();
        holder.tvAddress = (TextView) v.findViewById(R.id.tvAddress);
        holder.tvCity = (TextView) v.findViewById(R.id.tvCity);
        holder.tvTime = (TextView) v.findViewById(R.id.tvDateTime);
        holder.rectangle = v.findViewById(R.id.rectangle_background);
        v.setTag(holder);

        return v;
    }

    public String getPinId(int position) {
        return PINDAO.getInstance().getId((Cursor) getItem(position));
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.tvAddress.setText(context.getString(R.string.loading));
        holder.tvCity.setText(context.getString(R.string.loading));

        PINObject pin = PINDAO.getInstance().parseFields(cursor, cursor.getPosition(), new ILocationReceivedListener() {
            @Override
            public void onLocationReceived(LocationObject locationObject, int position) {
                if (locationObject == null) return;
                if (position < mListView.getFirstVisiblePosition() ||
                    position > mListView.getLastVisiblePosition()) return;

                int houseNumber = locationObject.HouseNumber;
                String street = locationObject.Street;
                String address = "";
                if (houseNumber > 0) {
                    address += houseNumber + " ";
                }
                if (!street.equalsIgnoreCase("null")) {
                    address += street;
                }
                holder.tvAddress.setText(address);

                String bottomLine = "";
                if (!locationObject.City.equalsIgnoreCase("null")) {
                    bottomLine += locationObject.City;
                }
                if (!locationObject.State.equalsIgnoreCase("null")) {
                    bottomLine += ", "+locationObject.State;
                }
                if (!locationObject.Zip.equalsIgnoreCase("null")) {
                    bottomLine += ", " + locationObject.Zip;
                }
                holder.tvCity.setText(bottomLine);
            }
        });

        String stringDate = pin.UpdateDate;
        Date date;
        try {
            date = SDDefine.simpleServerFormat.parse(stringDate);
            stringDate = SDDefine.localFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            stringDate = "";
        }
        holder.tvTime.setText(stringDate);
        String status = pin.Status;
        if (HomeActivity.mColors.containsKey(status)) {
            holder.rectangle.setBackgroundColor(Color.parseColor(HomeActivity.mColors.get(status)));
        }
    }
}
