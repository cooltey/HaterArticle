package com.cooltey.hater;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ironsource.mobilcore.MobileCore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
public class ForgetPwdActivity extends SherlockFragmentActivity {
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private Button forgetpwdBtn;
	
	// register url
	private String forgetPwdUrl = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//MobileCore.init(ForgetPwdActivity.this, "", MobileCore.LOG_TYPE.PRODUCTION);
		//MobileCore.getSlider().setContentViewWithSlider(ForgetPwdActivity.this, R.layout.activity_forgetpwd);
		
		setContentView(R.layout.activity_forgetpwd);

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		mEmailView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.email || id == EditorInfo.IME_NULL) {
							attemptSendPwd();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		forgetpwdBtn = (Button) findViewById(R.id.forgetpwd_button);
		
		forgetpwdBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptSendPwd();
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
	public void attemptSendPwd() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();

		boolean cancel = false;
		View focusView = null;
				
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

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_sending);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute(forgetPwdUrl);
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
	public class UserLoginTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			// TODO: attempt authentication against a network service.

			httpUrlConnection setConnection = new httpUrlConnection();
			
			String postName[] 	= {"hm_email"};
			String postValue[]	= {mEmail};
			
			// get result
			String finalResult = "";
			finalResult = setConnection.httpUrlConnection(url[0], postName, postValue);
			
			
			//Toast.makeText(getApplicationContext(), "result" + finalResult, 5000).show();
			// TODO: register the new account here.
			
			Log.d("Login Result: ", finalResult);
			return finalResult;
		}

		@Override
		protected void onPostExecute(final String success) {
			mAuthTask = null;
			showProgress(false);

			if (success.contains("success")) {
				Toast.makeText(getApplicationContext(), getString(R.string.success_forgetpwd), 3000).show();
				
			    Intent intent = new Intent().setClass(
						ForgetPwdActivity.this,
						LoginActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				
			}else if(success.equals("1") || success.equals("2")){
				mEmailView
						.setError(getString(R.string.error_invalid_email));
				mEmailView.requestFocus();
			}else if(success.equals("3")){
				mEmailView
						.setError(getString(R.string.error_field_email_not_exist));
				mEmailView.requestFocus();
			}else if(success.equals("4")){
				mEmailView
						.setError(getString(R.string.error_field_email_sending_fail));
				mEmailView.requestFocus();
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
