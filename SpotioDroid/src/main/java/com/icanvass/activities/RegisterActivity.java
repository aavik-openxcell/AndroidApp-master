package com.icanvass.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.icanvass.R;
import com.icanvass.abtracts.SDActivity;
import com.icanvass.helpers.CommonHelpers;
import com.icanvass.helpers.NetworkHelper;
import com.octo.android.robospice.request.SpiceRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends SDActivity implements LoaderCallbacks<Cursor> {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RegisterTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mFirstNameView;
//    private EditText mLastNameView;
    private EditText mCompanyNameView;
    private EditText mPhoneView;
    private View mProgressView;
    private View mLoginFormView;
    Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        track("RegisterView");
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.register_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.register_email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mFirstNameView   = (EditText) findViewById(R.id.register_first_name);
        mCompanyNameView = (EditText) findViewById(R.id.register_company_name);
//        mLastNameView    = (EditText) findViewById(R.id.register_last_name);
        mPhoneView       = (EditText) findViewById(R.id.register_phone_number);

        mLoginFormView   = findViewById(R.id.register_form);
        mProgressView    = findViewById(R.id.register_progress);
    }

    private void populateAutoComplete() {
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    public void showServerErrors(JSONArray a){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");

        String m="";
        for (int i = 0; i < a.length(); i++) {
            m += a.optString(i) + "\n";
        }
        builder.setMessage(m);
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        //return;
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mCompanyNameView.setError(null);
        mPhoneView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        String firstName = mFirstNameView.getText().toString();
//        String lastName = mLastNameView.getText().toString();

        String company = mCompanyNameView.getText().toString();
//        if(TextUtils.isEmpty(company)) {
//            company = email + " " + SDDefine.serverFormat.format(new Date());
//        }
        String phone = mPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(company)) {
            mCompanyNameView.setError(getString(R.string.error_field_required));
            if (focusView == null) focusView = mCompanyNameView;
            cancel = true;
        }

//        if (TextUtils.isEmpty(lastName)) {
//            mLastNameView.setError(getString(R.string.error_field_required));
//            if (focusView == null) focusView = mLastNameView;
//            cancel = true;
//        }

        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            if (focusView == null) focusView = mPhoneView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            if (focusView == null) focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            if (focusView == null) focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            if (focusView == null) focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            CommonHelpers.showKeyboard(this,focusView);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (!NetworkHelper.isOnline()){
                Toast.makeText(this, getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
                return;
            }
            CommonHelpers.hideKeyboard(this);
            showProgress(true);
            mAuthTask = new RegisterTask(email, password, firstName, "", company, phone);
            mAuthTask.execute();
        }
    }

    private void proceedToTutorialActivity() {
        Intent homeIntent = new Intent(this, ScreenSlideActivity.class);
        startActivity(homeIntent);
        finish();
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<String>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    void saveCredentials(String email, String password, String company) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit();

        editor.putString("EMAIL", email);
        editor.putString("PASSWORD", password);
        editor.putString("COMPANY", company);

        editor.commit();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RegisterTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;
        private final String mFirstName;
        private final String mLastName;
        private final String mCompany;
        private final String mPhone;

        RegisterTask(String email, String password, String firstName, String lastName,
                     String company, String phone) {
            mEmail      = email;
            mPassword   = password;
            mFirstName  = firstName;
            mLastName   = lastName;
            mCompany    = company;
            mPhone      = phone;
        }

        @Override
        protected String  doInBackground(Void... params) {
            try {
                JSONObject dic = new JSONObject();
                String[] f=mFirstName.split(" ");
                StringBuilder lastStringBuilder=new StringBuilder();
                for(int i = 1; i < f.length; ++i){
                    lastStringBuilder.append(f[i]).append(" ");
                }
                dic.put("FirstName", f[0]);
                dic.put("LastName", lastStringBuilder.toString().trim());
                dic.put("CompanyName", mCompany);
                dic.put("Phone", mPhone);
                dic.put("EmailAddress", mEmail);
                dic.put("Password", mPassword);

                return new RegisterRequest(dic).loadDataFromNetwork();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            mAuthTask = null;
            showProgress(false);

            try {
                JSONObject json = new JSONObject(result);
                if (json.optJSONArray("Message") == null) {
                    String companyNameFromTheServer = json.optString("CompanyLogin");
                    saveCredentials(mEmail, mPassword, companyNameFromTheServer);

                    proceedToTutorialActivity();
                } else {
                    //show errors;
                    showServerErrors(json.optJSONArray("Message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public static class RegisterRequest extends SpiceRequest<String> {

        private JSONObject params;

        public RegisterRequest(JSONObject params) {
            super(String.class);
            this.params = params;
        }

        @Override
        public String loadDataFromNetwork() throws Exception {

            // With Uri.Builder class we can build our url is a safe manner
            Uri.Builder uriBuilder = Uri.parse(
                    "http://services.spotio.com:888/MobileApp/RegisterCompanyExtended").buildUpon();

            String url = uriBuilder.build().toString();

            if (VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }

            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                    .openConnection();


            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();
//Create JSONObject here


// Send POST output.
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(params.toString());
            printout.flush();
            printout.close();

            String result = IOUtils.toString(urlConnection.getInputStream());
            urlConnection.disconnect();

            return result;
        }
    }
}



