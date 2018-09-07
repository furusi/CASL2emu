package jp.ac.fukuoka_u.tl.casl2emu.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
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

import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ac.fukuoka_u.tl.casl2emu.Casl2Emulator;
import jp.ac.fukuoka_u.tl.casl2emu.Casl2Register;
import jp.ac.fukuoka_u.tl.casl2emu.R;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEmailView = findViewById(R.id.email);
        if(preferences.contains("userid")){
            mEmailView.setText(preferences.getString("userid",""));
        }

        mPasswordView = findViewById(R.id.password);
        if(preferences.contains("password")){
            mPasswordView.setText(preferences.getString("password",""));
        }
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        Button guestButton = findViewById(R.id.guest_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        guestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(preferences.contains("userid")){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("userid");
                    editor.remove("password");
                    editor.apply();
                    mEmailView.setText("");
                    mPasswordView.setText("");
                    Toast.makeText(LoginActivity.this,"ログイン情報を消去しました。",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        //if(Build.MODEL.matches("^KF.*")||!mEmailView.getText().toString().matches("TLGUEST")){
        if(Build.MODEL.matches("^KF.*")){
           VerifyLoginTimeTask loginTimeTask = new VerifyLoginTimeTask();
            loginTimeTask.execute();
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("TL") || email.contains("tl");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
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
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
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

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    private class VerifyLoginTimeTask extends AsyncTask<Void,Void,Date>{

        VerifyLoginTimeTask(){

        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Date doInBackground(Void... params) {
            Casl2Ftp ftp = new Casl2Ftp(getApplicationContext());
            return ftp.getDate();
        }



        @Override
        protected void onPostExecute(Date o) {
            super.onPostExecute(o);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if(preferences.contains("LastLoginDate")){
                Date date = o;
                SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd");
                Date olddate = null;
                Log.d("Lastlogin",s.toString());
                try {
                    date = new SimpleDateFormat("yyyyMMdd").parse(android.text.format.DateFormat.format("yyyyMMdd",date).toString());
                    olddate = s.parse(preferences.getString("LastLoginDate",""));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(Math.abs(date.compareTo(olddate))>0){
                    mEmailView.setText("");
                    mPasswordView.setText("");
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("userid");
                    editor.remove("password");
                    editor.apply();
                    Toast.makeText(LoginActivity.this,"ログイン情報を消去しました。",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            try {
                // Simulate network access.
                //Thread.sleep(2000);
                String remoteserver = getString(R.string.server_address);                 //FTPサーバーアドレス
                int remoteport = 21;    //FTPサーバーポート
                String userid = mEmail.toUpperCase();                       //ログインユーザID
                Casl2Ftp ftp = new Casl2Ftp(getApplicationContext());
                result = ftp.appLogin(new InetSocketAddress(remoteserver, remoteport), userid, mPassword);
            } catch (InterruptedException e) {
                return false;
            } catch (Exception e) {
                e.printStackTrace();
            }


            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userid",mEmail.toUpperCase());
                editor.putString("password",mPassword);
                editor.apply();
                Casl2Register.initializeInstance();
                Casl2Emulator.initializeInstance("jp.ac.fukuoka_u.tl.casl2emu.android.Casl2EmulatorAndroid");
                logging("action","login");
                startActivity(new Intent(getApplicationContext(),ContextDisplayScreen.class));
            } else if (mEmail.toUpperCase().equals("TLGUEST")){
                finish();
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userid",mEmail.toUpperCase());
                editor.putString("password",mPassword);
                editor.apply();
                Casl2Register.initializeInstance();
                Casl2Emulator.initializeInstance("jp.ac.fukuoka_u.tl.casl2emu.android.Casl2EmulatorAndroid");
                startActivity(new Intent(getApplicationContext(),ContextDisplayScreen.class));

            }
            else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                Toast.makeText(LoginActivity.this,"ログインできませんでした。",Toast.LENGTH_SHORT).show();
                //mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

