package com.icanvass.abtracts;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;

import com.icanvass.helpers.FanOutHelper;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
//import com.bugsense.trace.BugSenseHandler;
import com.icanvass.webservices.SpotioSpiceManager;

import org.json.JSONObject;

/**
 * Created by Justin CAO on 6/4/2014.
 */
public abstract class SDActivity extends ActionBarActivity {
    public static final String MIXPANEL_TOKEN = "3d3406adba1edf53af7443468c7efad8";
    protected final String TAG_MIXPANEL = "TAG_MIXPANEL";
    protected MixpanelAPI mMixpanel;
    protected SpiceManager spiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        BugSenseHandler.initAndStartSession(this, "3c41a8a8");
        spiceManager = new SpotioSpiceManager(
                JacksonSpringAndroidSpiceService.class);
        mMixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), MIXPANEL_TOKEN);

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ViewGroup v=(ViewGroup)findViewById(android.R.id.content);
        v.addView(FanOutHelper.getWebView(this));
    }

    protected void track(String event) {
        mMixpanel.track(event, null);
    }

    protected void track(String event, JSONObject properties) {
        mMixpanel.track(event, properties);
    }

    @Override
    protected void onDestroy() {
        mMixpanel.flush();
        spiceManager.shouldStop();
        ViewGroup v=(ViewGroup)findViewById(android.R.id.content);
        v.removeView(v.findViewWithTag("FanOut"));
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        if (!spiceManager.isStarted()) {
            spiceManager.start(this);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
