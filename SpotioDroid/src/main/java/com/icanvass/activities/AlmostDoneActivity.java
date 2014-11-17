package com.icanvass.activities;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.icanvass.R;
import com.icanvass.abtracts.SDActivity;

import org.json.JSONObject;

public class AlmostDoneActivity extends SDActivity {

    Spinner mIndustrySpinner;
    Spinner mRoleSpinner;
    Spinner mEmployeesSpinner;
    Button mGetStartedButton;
    Button mSkipButton;

    private AnswersTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_almost_done);

        mIndustrySpinner = (Spinner)findViewById(R.id.industry_spinner);
        ArrayAdapter<CharSequence> industryAdapter = ArrayAdapter.createFromResource(this,
                R.array.industries, R.layout.item_spinner);
        industryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        mIndustrySpinner.setAdapter(industryAdapter);

        mRoleSpinner = (Spinner)findViewById(R.id.role_spinner);
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.roles, R.layout.item_spinner);
        roleAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        mRoleSpinner.setAdapter(roleAdapter);

        mEmployeesSpinner = (Spinner)findViewById(R.id.employees_spinner);
        ArrayAdapter<CharSequence> employeeAdapter = ArrayAdapter.createFromResource(this,
                R.array.employees, R.layout.item_spinner);
        employeeAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        mEmployeesSpinner.setAdapter(employeeAdapter);

        mGetStartedButton = (Button) findViewById(R.id.get_started_button);
        mGetStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllAnswersPicked()) {
                    sendAnswers();
                } else {
                    Toast.makeText(AlmostDoneActivity.this, "Choose all answers", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSkipButton = (Button) findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToTutorialActivity();
            }
        });
    }

    private boolean isAllAnswersPicked(){
        return mIndustrySpinner.getSelectedItemPosition()  > 0 &&
               mRoleSpinner.getSelectedItemPosition()      > 0 &&
               mEmployeesSpinner.getSelectedItemPosition() > 0;
    }

    private void sendAnswers() {
        if (mAuthTask != null) {
            return;
        }

        mAuthTask = new AnswersTask((String)mIndustrySpinner.getSelectedItem(),
                (String)mRoleSpinner.getSelectedItem(),
                (String)mEmployeesSpinner.getSelectedItem());
        mAuthTask.execute();
    }

    private void proceedToTutorialActivity() {
        Intent homeIntent = new Intent(this, ScreenSlideActivity.class);
        startActivity(homeIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.almost_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public class AnswersTask extends AsyncTask<Void, Void, Boolean> {

        String mIndustry;
        String mRole;
        String mUsers;

        AnswersTask(String industry, String role, String users) {
            mIndustry=industry;
            mRole=role;
            mUsers=users;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String resp;
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AlmostDoneActivity.this.getApplicationContext());
                String email = sharedPreferences.getString("EMAIL",null);
                String company = sharedPreferences.getString("COMPANY",null);
                JSONObject dic=new JSONObject();
                dic.put("companyLogin",company);
                dic.put("login",email);
                JSONObject answers=new JSONObject();
                answers.put("Industry",mIndustry);
                answers.put("Role",mRole);
                answers.put("EstimateUsersNumber",mUsers);
                dic.put("answers",answers);
//                String result = new AnswersRequest(dic).loadDataFromNetwork();
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                proceedToTutorialActivity();
            } else {
                //proceedToHomeActivity();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
