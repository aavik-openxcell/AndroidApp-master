package com.icanvass.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.icanvass.R;
import com.octo.android.robospice.request.SpiceRequest;
import com.icanvass.abtracts.SDActivity;
import com.icanvass.helpers.CommonHelpers;
import com.icanvass.helpers.EmailValidator;
import com.icanvass.helpers.NetworkHelper;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends SDActivity implements LoaderCallbacks<Cursor>, OnClickListener {

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
    private CompaniesTask mAuthTask = null;
    private LoginTask mAuthTask2 = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextView tvForgotPass;
    private Button mEmailSignInButton, mRegisterButton;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        track("LoginView");
        initUIComponents();
        initListeners();
        populateAutoComplete();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CommonHelpers.hideKeyboard(this);
    }

    private void initUIComponents() {
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        tvForgotPass = (TextView) findViewById(R.id.tv_forgot_pass);

        // TODO debug
//        mEmailView.setText("justin@nikmesoft.com");
//        mPasswordView.setText("abcde12345-");

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mRegisterButton = (Button) findViewById(R.id.register_button);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void initListeners() {
        mEmailSignInButton.setOnClickListener(this);
        mRegisterButton.setOnClickListener(this);
        tvForgotPass.setOnClickListener(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int keycode, KeyEvent keyEvent) {

                if (keycode == EditorInfo.IME_ACTION_DONE || textView.getImeActionLabel().equals(getResources().getString(R.string.action_sign_in_short))) {
                    attemptLogin();
                    return true;
                } else {
                    return false;
                }
            }
        });
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    mPasswordView.setText("");
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email_sign_in_button:
                attemptLogin();
                break;
            case R.id.register_button:
                openRegistration();
                break;
            case R.id.tv_forgot_pass:
                // TODO forgot pass
                String url = "http://app.spotio.com/Account/ICanvassForgotPassword";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
        }
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

    private void proceedToHomeActivity() {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

    public void openRegistration() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null || mAuthTask2 != null) {
            return;
        }
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        /* Store values at the time of the login attempt. */
        String email;
        String password;
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_empty_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!EmailValidator.validate(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            CommonHelpers.showKeyboard(LoginActivity.this, mEmailView);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (!NetworkHelper.isOnline()){
                Toast.makeText(this, getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
                return;
            }
            CommonHelpers.hideKeyboard(this);
            getCompanies(email, password);
        }
    }

    private void getCompanies(String email, String password) {
        showProgress(true);
        mAuthTask = new CompaniesTask(email, password);
        mAuthTask.execute();
    }

    private void login(String email, String password, String company) {
        showProgress(true);
        mAuthTask2 = new LoginTask(email, password, company);
        mAuthTask2.execute();
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
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    public static class CompaniesRequest extends SpiceRequest<String> {

        private String login;
        private String password;

        public CompaniesRequest(String login, String password) {
            super(String.class);
            this.login = login;
            this.password = password;
        }

        @Override
        public String loadDataFromNetwork() throws Exception {

            // With Uri.Builder class we can build our url is a safe manner
            Uri.Builder uriBuilder = Uri.parse(
                    "http://services.spotio.com:888/MobileApp/GetActiveCompanies").buildUpon();

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
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("login", login);
            jsonParam.put("password", password);

            // Send POST output.
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(jsonParam.toString());
            printout.flush();
            printout.close();

            String result = IOUtils.toString(urlConnection.getInputStream());
            urlConnection.disconnect();

            return result;
        }

    }

    public static class LoginRequest extends SpiceRequest<String> {

        private String login;
        private String password;
        private String company;

        public LoginRequest(String login, String password, String company) {
            super(String.class);
            this.login = login;
            this.password = password;
            this.company = company;
        }

        @Override
        public String loadDataFromNetwork() throws Exception {

            // With Uri.Builder class we can build our url is a safe manner
            Uri.Builder uriBuilder = Uri.parse(
                    "http://services.spotio.com:888/PinService.svc/Pins?$format=json&$top=0").buildUpon();

            String url = uriBuilder.build().toString();

            if (VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }

//            Authenticator.setDefault(new Authenticator() {
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(company+"||"+login, password.toCharArray());
//                }
//            });

            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url)
                    .openConnection();

            byte[] auth = (company + "||" + login + ":" + password).getBytes();
            String basic = Base64.encodeToString(auth, Base64.NO_WRAP);
            urlConnection.setRequestProperty("Authorization", "Basic " + basic);

            String result = IOUtils.toString(urlConnection.getInputStream());
            urlConnection.disconnect();


            return result;
        }

    }

    void saveCredentials(String email, String password, String company) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit();

        editor.putString("EMAIL", email);
        editor.putString("PASSWORD", password);
        editor.putString("COMPANY", company);

        editor.apply();
    }

    void selectCompany(final String login, final String pass, final JSONArray companies) {
        if (companies.length() == 1) {
            login(login, pass, companies.optString(0));
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select company");

        CharSequence[] cs = new CharSequence[companies.length()];
        for (int i = 0; i < companies.length(); i++) {
            cs[i] = companies.optString(i);
        }
        builder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                login(login, pass, companies.optString(which));
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class CompaniesTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;

        CompaniesTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            String companies;
            try {
                companies = new CompaniesRequest(mEmail, mPassword).loadDataFromNetwork();
            } catch (Exception e) {
                return null;
            }

            return companies;
        }

        @Override
        protected void onPostExecute(final String result) {
            mAuthTask = null;
            showProgress(false);

            if (result != null && result.length() > 2) {
//                proceedToHomeActivity();
                try {
                    JSONArray a = new JSONArray(result);

//                    login(mEmail, mPassword, a.getString(1));
                    selectCompany(mEmail, mPassword, a);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(LoginActivity.this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                mEmailView.requestFocus();
                CommonHelpers.showKeyboard(LoginActivity.this, mEmailView);
                mPasswordView.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class LoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mCompany;

        LoginTask(String email, String password, String company) {
            mEmail = email;
            mPassword = password;
            mCompany = company;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String resp;
            try {
                String result = new LoginRequest(mEmail, mPassword, mCompany).loadDataFromNetwork();
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask2 = null;
            showProgress(false);

            if (success) {
                saveCredentials(mEmail, mPassword, mCompany);
                proceedToHomeActivity();
                try {
                    JSONObject jsonObject = new JSONObject(new JSONStringer().object()
                            .key("mEmail").value(mEmail)
                            .key("mPassword").value(mPassword)
                            .key("mCompany").value(mCompany)
                            .endObject().toString());

//                    JSONObject jsonObject = new JSONObject(new JSONStringer().object()
//                            .key("mEmail").value(mEmail)
//                            .key("mPassword").value(mPassword)
//                            .key("mCompany").value(mCompany)
//                            .endObject().toString());
//                    track("MobileSuccessfullLogin", jsonObject);

//                    MobileSuccessfullLoginDate
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(LoginActivity.this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                mEmailView.requestFocus();
                CommonHelpers.showKeyboard(LoginActivity.this, mEmailView);
                mPasswordView.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask2 = null;
            showProgress(false);
        }
    }
}



