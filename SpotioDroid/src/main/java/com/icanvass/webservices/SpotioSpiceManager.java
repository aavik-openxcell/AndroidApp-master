package com.icanvass.webservices;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

/**
 * Created by romek on 27.07.2014.
 */
public class SpotioSpiceManager extends SpiceManager {
    /**
     * Creates a {@link com.octo.android.robospice.SpiceManager}. Typically this occurs in the construction
     * of an Activity or Fragment. This method will check if the service to bind
     * to has been properly declared in AndroidManifest.
     *
     * @param spiceServiceClass the service class to bind to.
     */
    public SpotioSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
        super(spiceServiceClass);
    }

    @Override
    protected int getThreadCount() {
        return 3;
    }
}
