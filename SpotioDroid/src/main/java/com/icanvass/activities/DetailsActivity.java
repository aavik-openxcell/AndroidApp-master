package com.icanvass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.icanvass.R;
import com.icanvass.abtracts.SDActivity;
import com.icanvass.adapters.PinDetailsAdapter;
import com.icanvass.database.PINDAO;
import com.icanvass.dummy.DummyContent;
import com.icanvass.objects.PINObject;
import com.icanvass.webservices.FieldsRequest;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class DetailsActivity extends SDActivity {

    private PINObject mPin;
    private SpiceManager spiceManager = new SpiceManager(
            JacksonSpringAndroidSpiceService.class);
    public static LinkedHashMap<String, JSONObject> mFields = new LinkedHashMap<String, JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String pinId = getIntent().getExtras().getString("pinId");
        mPin = PINDAO.getInstance().get(pinId);

        PlaceholderFragment f = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag(PlaceholderFragment.TAG);

        if (f == null){
            f = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f, PlaceholderFragment.TAG)
                    .commit();
        }
        f.setPin(mPin);

        requestFields();
    }

    private void requestFields() {
        setProgressBarIndeterminateVisibility(true);

        FieldsRequest request = new FieldsRequest(this);

        spiceManager.execute(request, request.createCacheKey(),
                DurationInMillis.ALWAYS_RETURNED, new FieldsRequestListener());
    }

    void notifyAboutFields() {
        PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentByTag(PlaceholderFragment.TAG);
        PinDetailsAdapter adapter = (PinDetailsAdapter) fragment.getListAdapter();
        adapter.setmFields(mFields);
        adapter.notifyDataSetChanged();
    }

    private void editPin() {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra("pinId", mPin.Id);
        intent.putExtra(AddEditActivity.USER_LOCATION, getIntent().getExtras().getParcelable(AddEditActivity.USER_LOCATION));
        intent.putExtra("edit", true);
        startActivityForResult(intent, 2);
    }

    private void viewPinOnMap() {
        Intent output = new Intent();
        output.putExtra("pinId", mPin.Id);
        Log.e("MapFragment","pin id for view on map=>" + mPin.Id);
        setResult(RESULT_OK, output);
        //setResult(RESULT_OK);
        /*if(getIntent().getExtras().getParcelable(AddEditActivity.USER_LOCATION) == null) {
            Intent homeIntent = new Intent(this, HomeActivity.class);
            startActivity(homeIntent);
        }*/
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.edit_item) {
            editPin();
            return true;
        }
        if (id == R.id.view_map) {
            viewPinOnMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {

        public static final String TAG = "placeholder";

        private PINObject mPin;

        public PlaceholderFragment() {
        }

        public void setPin(PINObject pin) {
            mPin = pin;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_details, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            PinDetailsAdapter adapter = new PinDetailsAdapter(getActivity(), DummyContent.ITEMS);
            adapter.setPin(mPin);
            adapter.setQuestions(mPin.CustomValues);
            setListAdapter(adapter);
        }
    }

    private class FieldsRequestListener implements RequestListener<JSONObject> {
        @Override
        public void onRequestFailure(SpiceException e) {
        }

        @Override
        public void onRequestSuccess(JSONObject response) {
            if (response == null) {
                return;
            }

            JSONArray fields = response.optJSONArray("value");
            if (fields == null) {
                return;
            }

            mFields.clear();
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.optJSONObject(i);
                mFields.put(field.optString("Id"), field);
            }
            notifyAboutFields();
        }
    }
}
