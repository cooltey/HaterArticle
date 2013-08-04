package com.cooltey.hater;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cooltey.hater.util.IabHelper;
import com.cooltey.hater.util.IabResult;
import com.cooltey.hater.util.Inventory;
import com.cooltey.hater.util.Purchase;
import com.ironsource.mobilcore.MobileCore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class SetInformationActivity extends SherlockFragmentActivity {
	
	private String storedName;
	private String storedPwd;
	private String storedIndex;
	
	private String TAG = "IN APP BILLING";
	static final String SKU_IAP_ITEM = "buy_change_name_3_times";
	//static final String SKU_IAP_ITEM = "android.test.purchased";
	//static final String SKU_IAP_ITEM = "android.test.refunded";
	//static final String SKU_IAP_ITEM = "android.test.item_unavailable";
    static final int RC_REQUEST = 10001;
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserSetinfoTask mAuthTask = null;
	private ChangeNameTask mChgNameTask = null;

	// Values for email and password at the time of the login attempt.
	private String mPassword;
	private String mPasswordAgain;
	private String mNickname;
	private String mRemainTimes;
	

	private String jNickname;
	private String jEmail;
	private String jNameChgTimes;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mPasswordAgainView;
	private EditText mNicknameView;
	
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private TextView mRemainTimesView;
	
	private Button setInfoBtn;
	private Button buyItemBtn;
	
	// set info url
	private String getInfoUrl = "";
	private String setInfoUrl = "";
	private String changeNameUrl = "";

	// unique device id
    private String mDeviceId = "";
    private String payloadString = "";
    //private String payloadString = "";
    
    // The helper object
    IabHelper mHelper;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		//MobileCore.init(SetInformationActivity.this, "", MobileCore.LOG_TYPE.PRODUCTION);
		//MobileCore.getSlider().setContentViewWithSlider(SetInformationActivity.this, R.layout.activity_setinfo);

		setContentView(R.layout.activity_setinfo);

        // Set Unique ID
		mDeviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		
		SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
		
		storedName 		= getStoredData.getString("hm_name", "");
		storedPwd 		= getStoredData.getString("hm_pwd", "");
		storedIndex 	= getStoredData.getString("hm_index", "");
		

		mEmailView = (EditText) findViewById(R.id.reg_email);
		mPasswordView = (EditText) findViewById(R.id.reg_password);
		mPasswordAgainView = (EditText) findViewById(R.id.reg_password_again);
		mNicknameView = (EditText) findViewById(R.id.reg_nickname);
		mRemainTimesView = (TextView) findViewById(R.id.reg_nickname_remain_times);
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		
		mEmailView.setEnabled(false);
		mNicknameView.setEnabled(false);
		
		getMemberInfo();
		
		setInfoBtn = (Button) findViewById(R.id.setinfo_button);
		buyItemBtn = (Button) findViewById(R.id.buy_button);
		
		setInfoBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptSetInfo();    						
					}
				});
			
		
		buyItemBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
						
						onGetItemButtonClicked(null);						
					}
				});
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		buyItem();
	}

	private void getMemberInfo(){
		// Creating JSON Parser instance
		jsonParser jParser = new jsonParser();

		getInfoUrl = getInfoUrl + "?hm_index=" + storedIndex + "&hm_pwd=" + storedPwd;
		
		//Log.d(getInfoUrl, getInfoUrl);
		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = jParser.execute(getInfoUrl).get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		 
		try {

		         
		        // Storing each json item in variable
		        jNickname 		= json.getString("hm_name");
		        jEmail			= json.getString("hm_email");
		    	jNameChgTimes	= json.getString("hm_name_change_times");
		        
		    
		    // set default values
		    mRemainTimes = mRemainTimesView.getText().toString();
		    mRemainTimesView.setText(mRemainTimes + jNameChgTimes);
		    
		    mEmailView.setText(jEmail);
		    
		    mNicknameView.setText(jNickname);
		    
		    if(Integer.parseInt(jNameChgTimes) > 0){

				mNicknameView.setEnabled(true);
		    }
		    
		} catch (JSONException e) {
		    e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptSetInfo() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mPasswordView.setError(null);
		mPasswordAgainView.setError(null);
		mNicknameView.setError(null);

		// Store values at the time of the login attempt.
		mPassword = mPasswordView.getText().toString();
		mPasswordAgain = mPasswordAgainView.getText().toString();
		

		mNickname = mNicknameView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (!TextUtils.isEmpty(mPassword)) {
			if (mPassword.length() < 4) {
				mPasswordView.setError(getString(R.string.error_invalid_password));
				focusView = mPasswordView;
				cancel = true;
			}else if(!mPassword.equals(mPasswordAgain)){
				mPasswordAgainView.setError(getString(R.string.error_incorrect_not_match));
				focusView = mPasswordAgainView;
				cancel = true;
			} 
		}else{
			// no update
			mPassword = "";
			mPasswordAgain = "";
		}

		int remainTimes = Integer.parseInt(jNameChgTimes);

		if(remainTimes > 0){
			if(!mNickname.equals(jNickname)){
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
			}else{
				// no update
				mNickname = "";
			}
		}else{
			// no update
			mNickname = "";
		}
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_updating);
			showProgress(true);
			mAuthTask = new UserSetinfoTask();
			mAuthTask.execute(setInfoUrl);
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
	public class UserSetinfoTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			// TODO: attempt authentication against a network service.

			httpUrlConnection setConnection = new httpUrlConnection();
			
			String postName[] 	= {"hm_index", "hm_pwd", "new_hm_pwd", "new_hm_pwd_check", "new_hm_name"};
			String postValue[]	= {storedIndex, storedPwd, mPassword, mPasswordAgain, mNickname};
			
			// get result
			String finalResult = "";
			finalResult = setConnection.httpUrlConnection(url[0], postName, postValue);
			
			//Toast.makeText(getApplicationContext(), "result" + finalResult, 5000).show();
			// TODO: register the new account here.
			
			Log.d("Set Result: ", finalResult);
			return finalResult;

		}

		@Override
		protected void onPostExecute(final String success) {
			mAuthTask = null;
			showProgress(false);

			if (success.contains("success")) {
				
				if(mPassword.equals("")){
					Toast.makeText(getApplicationContext(), getString(R.string.success_setinfo), 3000).show();
					
					finish();
					startActivity(getIntent());
					
				}else{
					Toast.makeText(getApplicationContext(), getString(R.string.success_setinfo_login), 3000).show();
					
					SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
		    		SharedPreferences.Editor getStoredDataEdit = getStoredData.edit();
		        	
		        	// clear stored information and refresh
		        	getStoredDataEdit.clear();
		        	getStoredDataEdit.commit();
		        	

		        	finish();
		        	
					Intent intent = new Intent().setClass(
							SetInformationActivity.this,
							LoginActivity.class);
		        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
				}
				
			}else if(success.equals("1")){
				mPasswordView
						.setError(getString(R.string.error_invalid_password));
				mPasswordView.requestFocus();
			}else if(success.equals("2")){
				mPasswordAgainView
						.setError(getString(R.string.error_incorrect_not_match));
				mPasswordAgainView.requestFocus();
			}else if(success.equals("3")){
				mNicknameView
						.setError(getString(R.string.error_field_nickname_length));
				mNicknameView.requestFocus();
			}else if(success.equals("4")){
				mNicknameView
						.setError(getString(R.string.error_field_nickname_used));
				mNicknameView.requestFocus();
			}else{
				Toast.makeText(getApplicationContext(), getString(R.string.error_publish_no_login), 3000).show();
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class ChangeNameTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			// TODO: attempt authentication against a network service.

			httpUrlConnection setConnection = new httpUrlConnection();
			
			String postName[] 	= {"hm_index", "hm_pwd", "hm_unique_id"};
			String postValue[]	= {storedIndex, storedPwd, mDeviceId};
			
			// get result
			String finalResult = "";
			finalResult = setConnection.httpUrlConnection(url[0], postName, postValue);
			
			//Toast.makeText(getApplicationContext(), "result" + finalResult, 5000).show();
			// TODO: register the new account here.
			
			Log.d("Set Result: ", finalResult);
			return finalResult;

		}

		@Override
		protected void onPostExecute(final String success) {
			mAuthTask = null;
			showProgress(false);

			if (success.contains("success")) {	
					alert(getString(R.string.dialog_purchase_success));
				
			}else{
				Toast.makeText(getApplicationContext(), getString(R.string.error_publish_no_login), 3000).show();
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	        getSupportMenuInflater().inflate(R.menu.main, menu);


	        // if its login, then change button
	        if(storedIndex.equals("") || storedIndex.equals("0")){
	        	menu.removeItem(R.id.action_control_panel);
	        }else{
	        	menu.removeItem(R.id.action_login);
	        }
	        menu.removeItem(R.id.action_refresh);
	        
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	    		finish();
	    	return true;
	    
	        case R.id.action_login:
	        	Intent intent = new Intent().setClass(
						this,
						LoginActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
	        	
	            return true;
	            
	        case R.id.action_mypage:
	        	intent = new Intent().setClass(
						this,
						MyArticleActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
	        	
	            return true;
	            
	        case R.id.action_publish:
	        	intent = new Intent().setClass(
						this,
						NewArticleActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
	        	
	            return true;
	        case R.id.action_logout:
	        	

	    		SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
	    		SharedPreferences.Editor getStoredDataEdit = getStoredData.edit();
	        	
	        	// clear stored information and refresh
	        	getStoredDataEdit.clear();
	        	getStoredDataEdit.commit();
	        	
	        	finish();
	        	
	        	intent = new Intent().setClass(
						this,
						MainActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				
	        	
	            return true;
	        case R.id.action_about:
	        	intent = new Intent().setClass(
						this,
						AboutActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
	        	
	            return true;  
	        case R.id.action_setinfo:
	        	intent = new Intent().setClass(
						this,
						SetInformationActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
	        	
	            return true;  
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","
                + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
	
	private void buyItem(){
        String base64EncodedPublicKey = "";
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.enableDebugLogging(false);
        
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    //complain("出問題了！請聯絡作者！: " + result);
                    return;
                }

                // Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
	}
	
	// Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                //complain("出問題了！請聯絡作者！: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");
            
            // Do we have the infinite gas plan?
            Purchase itemPurchased = inventory.getPurchase(SKU_IAP_ITEM);
            if (itemPurchased != null && verifyDeveloperPayload(itemPurchased)) {
                Log.d(TAG, "We have itemPurchased. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_IAP_ITEM), mConsumeFinishedListener);
                return;
            }
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };
    
 // "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
    // flow for subscription.
    public void onGetItemButtonClicked(View arg0) {
        String payload = payloadString; 
        
        try{
	        mHelper.launchPurchaseFlow(this, SKU_IAP_ITEM, RC_REQUEST, 
	                mPurchaseFinishedListener, payload);
        }catch (Exception e){
        }
    }
    
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        boolean returnVal = false;
        if(payload.equals(payloadString)){
        	returnVal = true;        	
        }
        
        return true;
    }
    
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
                complain(getString(R.string.dialog_purchase_failed));
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain(getString(R.string.dialog_purchase_failed));
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_IAP_ITEM)) {
                // bought the infinite gas subscription
                Log.d(TAG, "SKU_IAP_ITEM purchased.");
                
    			mLoginStatusMessageView.setText(R.string.login_progress_updating);
    			showProgress(true);
    			mChgNameTask = new ChangeNameTask();
    			mChgNameTask.execute(changeNameUrl);
            }
        }
    };
    
    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
            }
            else {
            	Log.d(TAG, "Consumption failed. Provisioning.");
            }
           
          
            Log.d(TAG, "End consumption flow.");
        }
    };
    
    
    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert(getString(R.string.dialog_purchase_failed));
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton(getString(R.string.dialog_btn_yes), new OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub

				finish();
				startActivity(getIntent());
			}
        	
        });
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
	
	// in app billing
	@Override
    public void onDestroy() {
        super.onDestroy();
        
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
