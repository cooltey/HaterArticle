package com.cooltey.hater;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ironsource.mobilcore.MobileCore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class NewArticleActivity extends SherlockFragmentActivity {

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserPublishTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mContent;
	private int mRemainwords = 0;
	// UI references.
	private EditText mContentView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private TextView mRemainWordsView;
	private Button publishBtn;
	
	// set max length
	private int maxStrLength = 150;

	// unique device id
    private String mDeviceId = "";
        
    // category id
    private String mCategory = "";
    
	// publish url
	private String publishUrl = "";

	// category loader
	private String categoryUrl = ""; 
	private JSONArray categoryArray = null;
	private static String[] categoryIndex = null;
	private static String[] categoryContent = null;
	
	// category adapter
	private ArrayAdapter<String> 	categoryAdapter;
	private Spinner  				categoryArticle;

	
	// emotion url
	private String emotionUrl = "";
	private JSONArray emotionArray = null;
	private static String[] emotionContent = null;
	
	private LinearLayout emotionList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//MobileCore.init(NewArticleActivity.this, "", MobileCore.LOG_TYPE.PRODUCTION);
		//MobileCore.getSlider().setContentViewWithSlider(NewArticleActivity.this, R.layout.activity_new_article);
		
		setContentView(R.layout.activity_new_article);

        // Set Unique ID
		mDeviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		mRemainWordsView = (TextView) findViewById(R.id.remain_words);

		mRemainWordsView.setText(maxStrLength + "");
		
		// Set up the login form.
		mContentView = (EditText) findViewById(R.id.publish_content);
		mContentView.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable str) {
				// TODO Auto-generated method stub
				mRemainwords = maxStrLength - str.length();
				
				mRemainWordsView.setText(mRemainwords + "");
			}
	
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
	
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
    	   
       });

		mLoginFormView = findViewById(R.id.publish_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		publishBtn = (Button) findViewById(R.id.publish_button);
		
		publishBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptPublish();
					}
				});
		
		
		// set up spnning form
		categoryArticle = (Spinner) findViewById(R.id.categoryArticle);
		getCategoryList();
		
		
		// set up horizontal scrollview
		emotionList = (LinearLayout) findViewById(R.id.emotionList);
		getEmotionList();
		 
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptPublish() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mContentView.setError(null);

		// Store values at the time of the login attempt.
		mContent = mContentView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mContent)) {
			mContentView.setError(getString(R.string.error_field_required));
			focusView = mContentView;
			cancel = true;
		} else if (mContent.length() < 2) {
			mContentView.setError(getString(R.string.error_field_content_length));
			focusView = mContentView;
			cancel = true;
		}
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_publishing);
			showProgress(true);
			mAuthTask = new UserPublishTask();
			mAuthTask.execute(publishUrl);
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
	public class UserPublishTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			// TODO: attempt authentication against a network service.


			SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
			String storedPwd 	= getStoredData.getString("hm_pwd", "");
			String storedIndex 	= getStoredData.getString("hm_index", "");

			// get category
			int getCategoryPosition = categoryArticle.getSelectedItemPosition();
			mCategory =  categoryIndex[getCategoryPosition];
			
			// get result
			String finalResult = "";
			
			if(!storedPwd.equals("") || !storedIndex.equals("")){
				httpUrlConnection setConnection = new httpUrlConnection();
				
				String postName[] 	= {"ha_content", "hc_index", "ha_unique_id", "hm_pwd", "hm_index"};
				String postValue[]	= {mContent, mCategory, mDeviceId, storedPwd, storedIndex};
				
				finalResult = setConnection.httpUrlConnection(url[0], postName, postValue);
				
				//Toast.makeText(getApplicationContext(), "result" + finalResult, 5000).show();
				// TODO: register the new account here.
				
				Log.d("Publish Result: ", finalResult);
			}else{
				finalResult = "1";
			}
			return finalResult;
		}

		@Override
		protected void onPostExecute(final String success) {
			mAuthTask = null;
			showProgress(false);

			if (success.contains("success")) {
				Toast.makeText(getApplicationContext(), getString(R.string.success_publish), 3000).show();
								
			    Intent intent = new Intent().setClass(
						NewArticleActivity.this,
						MainActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				
			}else if(success.equals("0")){
				mContentView
						.setError(getString(R.string.error_publish));
				mContentView.requestFocus();
			}else if(success.equals("1")){
				mContentView
						.setError(getString(R.string.error_publish_too_many));
				mContentView.requestFocus();
			}else if(success.equals("2")){
				mContentView
						.setError(getString(R.string.error_publish_no_login));
				mContentView.requestFocus();
			}else if(success.equals("3")){
				mContentView
					.setError(getString(R.string.error_publish_pwd_error));
				mContentView.requestFocus();
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
	            
	        case R.id.action_publish:
	        	Intent intent = new Intent().setClass(
						this,
						NewArticleActivity.class);
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
	
	//get category list
	private void getCategoryList(){		
		// Creating JSON Parser instance
		jsonParser jParser = new jsonParser();
		 
		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = jParser.execute(categoryUrl).get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		 
		try {
		    // Getting Array of Contacts
			categoryArray = json.getJSONArray("data");
			
			// set content size
			int defaultSize = categoryArray.length();
			categoryContent = new String[defaultSize];
			categoryIndex = new String[defaultSize];
			
		    // looping through All Contacts
		    for(int i = 0; i < categoryArray.length(); i++){
			        JSONObject c = categoryArray.getJSONObject(i);
			         
			        // Storing each json item in variable
			        String hc_index = c.getString("hc_index");
			        String hc_name 	= c.getString("hc_name");
			        
			        // push values into string array
			        categoryContent[i] = hc_name;
			        categoryIndex[i] = hc_index;
		    }
		    
		    
		    // load category adapter
	        categoryAdapter 	= new ArrayAdapter<String>(this, R.layout.spinner_item_layout, categoryContent);
	        categoryArticle.setAdapter(categoryAdapter);
		} catch (JSONException e) {
		    e.printStackTrace();
		}
	}
	
	//get category list
	private void getEmotionList(){		
			// Creating JSON Parser instance
			jsonParser jParser = new jsonParser();
			 
			// getting JSON string from URL
			JSONObject json = null;
			try {
				json = jParser.execute(emotionUrl).get();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			 
			try {
			    // Getting Array of Contacts
				emotionArray = json.getJSONArray("data");
				
				// set content size
				int defaultSize = emotionArray.length();
				emotionContent = new String[defaultSize];
			     
			    // looping through All Contacts
			    for(int i = 0; i < emotionArray.length(); i++){
				        JSONObject c = emotionArray.getJSONObject(i);
				         
				        // Storing each json item in variable
				        String content = c.getString("data");
				        
				        // push values into string array
				        emotionContent[i] = content;

				        TextView tv_divider = new TextView(NewArticleActivity.this);
				        tv_divider.setText("   ");
				        
				        TextView tv = new TextView(NewArticleActivity.this);
				        tv.setPadding(10, 10, 10, 10);
				        tv.setBackgroundResource(R.drawable.emotion_item_selector);
				        tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				        tv.setText(content);
				        
				        	emotionList.addView(tv_divider);
				        emotionList.addView(tv);
				        
				        // set click listener
				        tv.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								TextView tv = (TextView) v;
								String words = tv.getText().toString();
								
								mContentView.setText(mContentView.getText().toString() + words);

								// force position to the end
								Selection.setSelection(mContentView.getText(), mContentView.getText().length());
							}
				        	
				        	
				        });
			    }
			    
			    			    
			    
			} catch (JSONException e) {
			    e.printStackTrace();
			}
		}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	        getSupportMenuInflater().inflate(R.menu.main, menu);

	        menu.removeItem(R.id.action_login);
	        menu.removeItem(R.id.action_refresh);
	        
		return true;
	}
}
