package com.cooltey.hater;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ironsource.mobilcore.MobileCore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends SherlockFragmentActivity {
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserRegisterTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private String mPasswordAgain;
	private String mNickname;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mPasswordAgainView;
	private EditText mNicknameView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private Button registerBtn;
	// unique device id
    private String mDeviceId = "";
	
	// register url
	private String registerUrl = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//MobileCore.init(RegisterActivity.this, "", MobileCore.LOG_TYPE.PRODUCTION);
		//MobileCore.getSlider().setContentViewWithSlider(RegisterActivity.this, R.layout.activity_register);
		
		setContentView(R.layout.activity_register);

        // Set Unique ID
		mDeviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordAgainView = (EditText) findViewById(R.id.password_again);
		mNicknameView = (EditText) findViewById(R.id.nickname);
		mNicknameView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.register || id == EditorInfo.IME_NULL) {
							attemptRegister();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);


		registerBtn = (Button) findViewById(R.id.register_button);
		
		registerBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
						 new AlertDialog.Builder(RegisterActivity.this)
					        .setIcon(android.R.drawable.ic_dialog_alert)
					        .setTitle(R.string.confirm_dialog_title_warning)
					        .setMessage(R.string.confirm_dialog_register_content)
					        .setPositiveButton(R.string.confirm_dialog_yes_btn, new DialogInterface.OnClickListener() {
					            @Override
					            public void onClick(DialogInterface dialog, int which) {
									attemptRegister();    
					            }

					        })
					        .setNegativeButton(R.string.confirm_dialog_no_btn, null)
					        .show();
						
					}
				});
		
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptRegister() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mPasswordAgainView.setError(null);
		mNicknameView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mPasswordAgain = mPasswordAgainView.getText().toString();
		mNickname = mNicknameView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}
		
		if (!mPassword.equals(mPasswordAgain)) {
			mPasswordAgainView.setError(getString(R.string.error_incorrect_not_match));
			focusView = mPasswordAgainView;
			cancel = true;
		}
		

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mNickname)) {
			mNicknameView.setError(getString(R.string.error_field_required));
			focusView = mNicknameView;
			cancel = true;
		} else if (mNickname.length() < 1 || mNickname.length() > 16) {
			mNicknameView.setError(getString(R.string.error_field_nickname_length));
			focusView = mNicknameView;
			cancel = true;
		}
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_registering);
			showProgress(true);
			mAuthTask = new UserRegisterTask();
			mAuthTask.execute(registerUrl);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserRegisterTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			// TODO: attempt authentication against a network service.

			httpUrlConnection setConnection = new httpUrlConnection();
			
			String postName[] 	= {"hm_email", "hm_name", "hm_pwd", "ha_unique_id"};
			String postValue[]	= {mEmail, mNickname, mPassword, mDeviceId};
			
			// get result
			String finalResult = "";
			finalResult = setConnection.httpUrlConnection(url[0], postName, postValue);
			
			//Toast.makeText(getApplicationContext(), "result" + finalResult, 5000).show();
			// TODO: register the new account here.
			
			Log.d("Register Result: ", finalResult);
			return finalResult;

		}

		@Override
		protected void onPostExecute(final String success) {
			mAuthTask = null;
			showProgress(false);

			if (success.contains("success")) {
				Toast.makeText(getApplicationContext(), getString(R.string.success_register), 3000).show();
				finish();
			}else if(success.equals("1")){
				mEmailView
						.setError(getString(R.string.error_field_email_used));
				mEmailView.requestFocus();
			}else if(success.equals("2")){
				mEmailView
						.setError(getString(R.string.error_invalid_email));
				mEmailView.requestFocus();
			}else if(success.equals("3")){
				mNicknameView
						.setError(getString(R.string.error_field_nickname_used));
				mNicknameView.requestFocus();
			}else if(success.equals("4")){
				mNicknameView
						.setError(getString(R.string.error_field_nickname_length));
				mNicknameView.requestFocus();
			}else if(success.equals("5")){
				mPasswordView
						.setError(getString(R.string.error_invalid_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case android.R.id.home:

	        	finish();
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
