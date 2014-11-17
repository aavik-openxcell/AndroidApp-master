package com.icanvass.adapters;

import com.icanvass.objects.LocationObject;

/**
 * Created by alex on 28.08.14.
 */
public interface ILocationReceivedListener {
    public void onLocationReceived(LocationObject locationObject, int position);
}
