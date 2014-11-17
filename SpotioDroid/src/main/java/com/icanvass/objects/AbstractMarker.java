package com.icanvass.objects;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by alex on 06.09.14.
 */
public class AbstractMarker implements ClusterItem  {

    protected Marker mAssociatedMarker;
    protected MarkerOptions mMarker;
    protected String pinId;

    public AbstractMarker(MarkerOptions marker) {
        this.mMarker = marker;
    }

    @Override
    public LatLng getPosition() {
        return mMarker.getPosition();
    }

    public MarkerOptions getMarker() {
        return mMarker;
    }

    public String getPinId() {
        return pinId;
    }

    public AbstractMarker setPinId(String pinId) {
        this.pinId = pinId;
        return this;
    }

    public Marker getAssociatedMarker() {
        return mAssociatedMarker;
    }

    public void setAssociatedMarker(Marker associatedMarker) {
        mAssociatedMarker = associatedMarker;
    }
}
