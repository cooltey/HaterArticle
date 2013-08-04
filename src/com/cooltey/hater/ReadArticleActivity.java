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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class ReadArticleActivity extends SherlockFragmentActivity {

	private UserPublishTask mAuthTask = null;

	// UI references.
	private View mArticleZoneView;
	private LinearLayout mArticleContentView;
	private LinearLayout mReplyContentView;
	private View mProgressStatusView;
	private View mNeedLoginZone;
	private View mReplyTextZone;
	private TextView mProgressStatusMessageView;
	private EditText mReplyInputContent;
	private String mReplyInput;
	private String mHateType;
	
	private String storedName;
	private String storedPwd;
	private String storedIndex;

	// unique device id
    private String mDeviceId = "";

	// replay content loader
	private String replyUrl = ""; 
	private JSONArray replyArray = null;

	private String newReplyUrl = ""; 
	
	// category adapter
	private ArrayAdapter<String> 	emotionAdapter;
	private int[]					emotionTypeArray = {R.string.hater_type_1, R.string.hater_type_2, R.string.hater_type_3, R.string.hater_type_4, R.string.hater_type_5, R.string.hater_type_6, R.string.hater_type_7};
	private int[]					emotionColorArray = {R.string.hater_type_color_1, R.string.hater_type_color_2, R.string.hater_type_color_3, R.string.hater_type_color_4, R.string.hater_type_color_5, R.string.hater_type_color_6, R.string.hater_type_color_7};
	
	// reply setting
	private String ha_index;
	
	
	// spinner
	private Spinner emotionTypeSpinner;

	// button
	private Button replyBtn;
	private Button loginBtn;
	
	private int bottomPadding = 0;
	
	private LayoutInflater layoutInflater;
	

    // dialog view
    private LinearLayout process_status;							
	private ScrollView dialog_action;
	private LinearLayout score_list;
	private TextView score_title;
	private TextView score_btn;
	private View score_dialog_vi;
    
    private String scoreListUrl = "";
    private JSONArray scoreArray = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MobileCore.init(ReadArticleActivity.this, "", MobileCore.LOG_TYPE.PRODUCTION);
		MobileCore.getSlider().setContentViewWithSlider(ReadArticleActivity.this, R.layout.activity_read_article);

		//setContentView(R.layout.activity_read_article);
		
		layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
		
		storedName 		= getStoredData.getString("hm_name", "");
		storedPwd 		= getStoredData.getString("hm_pwd", "");
		storedIndex 	= getStoredData.getString("hm_index", "");

        // Set Unique ID
		mDeviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);



		mArticleZoneView 			= findViewById(R.id.article_zone);
		mArticleContentView 		= (LinearLayout) findViewById(R.id.article_content);
		mReplyContentView 			= (LinearLayout) findViewById(R.id.reply_content);
		mProgressStatusView 		= findViewById(R.id.process_status);
		mProgressStatusMessageView 	= (TextView) findViewById(R.id.process_message);

		mReplyInputContent 			= (EditText) findViewById(R.id.reply_input_content);
		
		// btn
		loginBtn = (Button) findViewById(R.id.login_btn);
		loginBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent().setClass(
						ReadArticleActivity.this,
						LoginActivity.class);
	        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			
		});
		
		
		replyBtn = (Button) findViewById(R.id.reply_btn);
		replyBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				attemptPublish();
			}
			
		});
		
		// check login status

		mNeedLoginZone 			= findViewById(R.id.needLoginZone);
		mReplyTextZone 			= findViewById(R.id.replyTextZone);
		
		if(storedIndex.equals("") || storedIndex.equals("0")){
			mNeedLoginZone.setVisibility(View.VISIBLE);
			mReplyTextZone.setVisibility(View.GONE);

			
			// loading reply
			Handler handler = new Handler();
	    	
	    	handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				bottomPadding = mNeedLoginZone.getHeight();
			}}, 500);
        }else{
			mNeedLoginZone.setVisibility(View.GONE);
			mReplyTextZone.setVisibility(View.VISIBLE);
			
			// loading reply
			Handler handler = new Handler();
	    	
	    	handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				bottomPadding = mReplyTextZone.getHeight();
			}}, 500);
        }
		
		emotionTypeSpinner = (Spinner) findViewById(R.id.emotionType);
		// gen string array
		String[] emptionTypeArrayString = new String[emotionTypeArray.length];
		
		for(int s = 0; s < emotionTypeArray.length; s++){
			emptionTypeArrayString[s] = getString(emotionTypeArray[s]);
		}
		
		// load category adapter
		emotionAdapter 	   = new ArrayAdapter<String>(this, R.layout.spinner_item_layout, emptionTypeArrayString);
		emotionTypeSpinner.setAdapter(emotionAdapter);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		// loading view
		mArticleContentView.addView(loadingArticleContent());

    	showProgress(true, mProgressStatusView, mReplyContentView);
    	
		// loading reply
		Handler handler = new Handler();
    	
    	handler.postDelayed(new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			replyLoader();
		}}, 500);
	}

	public void attemptPublish() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mReplyInputContent.setError(null);

		// Store values at the time of the login attempt.
		mReplyInput = mReplyInputContent.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mReplyInput)) {
			mReplyInputContent.setError(getString(R.string.error_field_required));
			focusView = mReplyInputContent;
			cancel = true;
		} else if (mReplyInput.length() < 2) {
			mReplyInputContent.setError(getString(R.string.error_field_content_length));
			focusView = mReplyInputContent;
			cancel = true;
		}
		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true, mProgressStatusView, mReplyContentView);
			mAuthTask = new UserPublishTask();
			mAuthTask.execute(newReplyUrl);
		}
	}
	
	public class UserPublishTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			// TODO: attempt authentication against a network service.

			// get category
			int getCurrentPosition 	= emotionTypeSpinner.getSelectedItemPosition();
			mHateType 				= getString(emotionTypeArray[getCurrentPosition]);
			
			// get result
			String finalResult  = "";
			
			if(!storedPwd.equals("") || !storedIndex.equals("")){
				httpUrlConnection setConnection = new httpUrlConnection();
				
				String postName[] 	= {"ha_index", "hr_type", "hr_content", "hr_unique_id", "hm_pwd", "hm_index"};
				String postValue[]	= {ha_index, mHateType, mReplyInput, mDeviceId, storedPwd, storedIndex};
				
				finalResult = setConnection.httpUrlConnection(url[0], postName, postValue);
				
				//Toast.makeText(getApplicationContext(), "result" + finalResult, 5000).show();
				// TODO: register the new account here.
				
				//Log.d("Publish Result: ", finalResult);
			}else{
				finalResult = "1";
			}
			return finalResult;
		}

		@Override
		protected void onPostExecute(final String success) {
			mAuthTask = null;
			showProgress(false, mProgressStatusView, mReplyContentView);

			if (success.contains("success")) {
				Toast.makeText(getApplicationContext(), getString(R.string.success_publish), 3000).show();
				finish();
				startActivity(getIntent());
				
				
				
			}else if(success.equals("0")){
				mReplyInputContent
						.setError(getString(R.string.error_publish));
				mReplyInputContent.requestFocus();
			}else if(success.equals("1")){
				mReplyInputContent
						.setError(getString(R.string.error_publish_too_many));
				mReplyInputContent.requestFocus();
			}else if(success.equals("2")){
				mReplyInputContent
						.setError(getString(R.string.error_publish_no_login));
				mReplyInputContent.requestFocus();
			}else if(success.equals("3")){
				mReplyInputContent
					.setError(getString(R.string.error_publish_pwd_error));
				mReplyInputContent.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false, mProgressStatusView, mReplyContentView);
		}
	}
	
	private View loadingArticleContent(){
		
		JSONObject jData = ArticleContentFragment.replyContent;
				
        View vi = layoutInflater.inflate(R.layout.article_list_item, null);
        try {
	        // Storing each json item in variable
			ha_index 				= jData.getString("ha_index");
	        String ha_content 		= jData.getString("ha_content");
	        final String hm_name 			= jData.getString("hm_name");
	        String ha_date 			= jData.getString("ha_date");
	        String ha_pos_score 	= jData.getString("ha_pos_score");
	        String ha_neg_score 	= jData.getString("ha_neg_score");
	        String ha_comments_num 	= jData.getString("ha_comments_num");
	
	        // set tag
	        vi.setTag(ha_index);
	        
	        TextView  content			=	(TextView)vi.findViewById(R.id.content);
	        TextView  name				=	(TextView)vi.findViewById(R.id.name);
	        TextView  date				=	(TextView)vi.findViewById(R.id.date);
	        TextView  pos_num			=	(TextView)vi.findViewById(R.id.pos_num);
	        TextView  neg_num			=	(TextView)vi.findViewById(R.id.neg_num);
	        TextView  comments_num		=	(TextView)vi.findViewById(R.id.comment_num);
	        
	        content.setText(ha_content);
	        name.setText(this.getResources().getString(R.string.article_name_prefix) + hm_name);
	        date.setText(this.getResources().getString(R.string.article_date_prefix) + ha_date);
	        pos_num.setText(ha_pos_score + this.getResources().getString(R.string.article_pos_string));
	        neg_num.setText(ha_neg_score + this.getResources().getString(R.string.article_neg_string));
	        comments_num.setText(ha_comments_num + this.getResources().getString(R.string.article_comment_string));
	        
	        name.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Bundle bundle = new Bundle();
					bundle.putString("hm_name", hm_name);
					Intent intent = new Intent().setClass(
							ReadArticleActivity.this,
							UserArticleActivity.class);
		        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		        	intent.putExtras(bundle);
		        	startActivity(intent);
				}
	        	
	        });
	        
	        pos_num.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					checkScoreList(ha_index, "0");
				}
	        	
	        });
	        
	        neg_num.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					checkScoreList(ha_index, "1");
				}
	        	
	        });
	        

	        name.setOnTouchListener(onTouchListener);
	        pos_num.setOnTouchListener(onTouchListener);
	        neg_num.setOnTouchListener(onTouchListener);
        } catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return vi;
	}
	
	
    // only load article for json data
    private void replyLoader(){
    	//String returnVal = "0";    	
    	String currentUrl = replyUrl;
        	
    	currentUrl = currentUrl + "?ha_index=" + "" + ha_index;
        
        // Creating JSON Parser instance
     	jsonParser jParser = new jsonParser();
        
     	// getting JSON string from URL
 		JSONObject json = null;
 		try {
 			json = jParser.execute(currentUrl).get();
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 
 		try {
		    // Getting Array of Contacts	

 			replyArray = json.getJSONArray("data");
 			//returnVal = "success";
 			
 			// generate view
 			for(int i = 0; i < replyArray.length(); i++){

 	 			View vi = layoutInflater.inflate(R.layout.reply_list_item, null);
		        JSONObject jData 	= replyArray.getJSONObject(i);
 		        String r_content 	= jData.getString("hr_content");
 		        String r_name 		= jData.getString("hm_name");
 		        String r_type 		= jData.getString("hr_type");
 		        String r_date	 	= jData.getString("hr_date");
 		        
 		        TextView  content	=	(TextView)vi.findViewById(R.id.content);
 		        TextView  name		=	(TextView)vi.findViewById(R.id.name);
 		        TextView  date		=	(TextView)vi.findViewById(R.id.date);
 		        TextView  type		=	(TextView)vi.findViewById(R.id.hate_type);
 		        
 		        content.setText(r_content);
 		        name.setText(r_name);
 		        date.setText(r_date);
 		        
 		        // set color
 		        type.setText(r_type);
 		        
 		        int typeBg = 0;
 		        for(int j = 0; j < emotionTypeArray.length; j++){
 		        	String getTxt = getString(emotionTypeArray[j]);
 		        	if(getTxt.contains(r_type)){
 		        		//typeBg = getString(emotionColorArray[j]);
 		        		typeBg = emotionColorArray[j];
 		        	}
 		        }
 		        
 		        type.setBackgroundColor(Color.parseColor(getString(typeBg)));
 		        
 		        // add view
 		       mReplyContentView.addView(vi);
 			}
 			
 			
 			// add padding to the bottom of view
 			mReplyContentView.setPadding(0, 0, 0, bottomPadding);

 			showProgress(false, mProgressStatusView, mReplyContentView);
 			
		    //Log.d("jSON Length", replyArray.length() + "");
		    
		} catch (JSONException e) {
		    e.printStackTrace();
		    
		}    	
 		
 		//Log.d("jSON currentUrl", currentUrl);
 		
 		//return returnVal;
    }

 private void loadingScoreList(String ha_index, String hs_mode){
    	
    	String currentUrl = scoreListUrl + "?ha_index=" + ha_index + "&hs_mode=" + hs_mode;
    	
    	// Creating JSON Parser instance
     	jsonParser jParser = new jsonParser();
        
     	// getting JSON string from URL
 		JSONObject json = null;
 		try {
 			json = jParser.execute(currentUrl).get();
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (ExecutionException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 
 
 		try {
		    // Getting Array of Contacts	

 			scoreArray = json.getJSONArray("data");				
			
		    //Log.d("jSON Url", currentUrl);
		    //Log.d("totalNum", totalNum + "");
		} catch (JSONException e) {
		    e.printStackTrace();
		    
		}    	
    }
    
    private void genScoreList(){
    	try{
	    	for(int i = 0; i < scoreArray.length(); i++){
				
				JSONObject jData = scoreArray.getJSONObject(i);
				
				final String hm_name 			= jData.getString("hm_name");
				String hsr_create_time 			= jData.getString("hsr_create_time");
				
				View dialog_item_vi 			= layoutInflater.inflate(R.layout.score_list_item, null);
				
				TextView hm_name_view 			= (TextView) dialog_item_vi.findViewById(R.id.name);
				TextView hsr_create_time_view 	= (TextView) dialog_item_vi.findViewById(R.id.date);
				
				hm_name_view.setText(hm_name);
				hsr_create_time_view.setText(hsr_create_time);
				
				hm_name_view.setOnClickListener(new OnClickListener(){
	
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Bundle bundle = new Bundle();
						bundle.putString("hm_name", hm_name);
						Intent intent = new Intent().setClass(
								ReadArticleActivity.this,
								UserArticleActivity.class);
			        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        	intent.putExtras(bundle);
			        	startActivity(intent);
					}
		        	
		        });
				
				hm_name_view.setOnTouchListener(onTouchListener);
				
				//add view into main view
				score_list.addView(dialog_item_vi);
			}
		
	    	if(scoreArray.length() < 1){
	    		TextView tv = new TextView(ReadArticleActivity.this);
	    		tv.setText(getString(R.string.dialog_no_score_member));
	    		score_list.addView(tv);
	    	}
	    	
    	}catch (Exception e){
    		TextView tv = new TextView(ReadArticleActivity.this);
    		tv.setText(getString(R.string.dialog_no_score_member));
    		score_list.addView(tv);
    	}
    }
    
    private void checkScoreList(final String ha_index, final String hs_mode){

		score_dialog_vi = layoutInflater.inflate(R.layout.dialog_score_list, null);				 			
		process_status 	= (LinearLayout) score_dialog_vi.findViewById(R.id.process_status);							
		dialog_action 	= (ScrollView) score_dialog_vi.findViewById(R.id.dialog_action);
		score_list 		= (LinearLayout) score_dialog_vi.findViewById(R.id.score_list);
		score_title		= (TextView) score_dialog_vi.findViewById(R.id.score_title);
		score_btn		= (TextView) score_dialog_vi.findViewById(R.id.score_btn);

		if(hs_mode.equals("0")){
			score_title.setText(getString(R.string.dialog_score_title_prefix) + getString(R.string.dialog_pos_btn) + getString(R.string.dialog_score_title_lastfix));
			score_btn.setText(getString(R.string.dialog_give_score_btn) + getString(R.string.dialog_pos_btn));
		}else{
			score_title.setText(getString(R.string.dialog_score_title_prefix) + getString(R.string.dialog_neg_btn) + getString(R.string.dialog_score_title_lastfix));
			score_btn.setText(getString(R.string.dialog_give_score_btn) + getString(R.string.dialog_neg_btn));
		}
		
		score_btn.setVisibility(View.GONE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ReadArticleActivity.this);
		builder.setView(score_dialog_vi);
		Dialog dialog = builder.create();
		dialog.show();
		
    	showProgress(true, process_status, dialog_action);
    	
		final Handler handler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
				showProgress(false, process_status, dialog_action);
        		genScoreList();
        	}
    	};
    	
    	new Thread
		(
	        new Runnable() 
			{
	        	@Override
				public void run() 
				{      				
					loadingScoreList(ha_index, hs_mode);
					handler.sendEmptyMessage(0);
				}
			}	
         ).start();  
    }
    
    private OnTouchListener onTouchListener = new OnTouchListener(){
		@Override
		public boolean onTouch(View v, MotionEvent me) {
			// TODO Auto-generated method stub
			
			if(me.getAction() == MotionEvent.ACTION_DOWN){
				TextView tv = (TextView) v;
				tv.setTextColor(Color.RED);
			}else{
				TextView tv = (TextView) v;
				tv.setTextColor(Color.BLACK);						
			}
			
			return false;
		}
    };
    
    /**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show, final View processView, final View contentView) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			processView.setVisibility(View.VISIBLE);
			processView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							processView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			contentView.setVisibility(View.VISIBLE);
			contentView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							contentView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			processView.setVisibility(show ? View.VISIBLE : View.GONE);
			contentView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	/*Intent intent = new Intent().setClass(
						this,
						MainActivity.class);
	        	intent.setFlags(Intent.);
				startActivity(intent);*/
	        	
	        	finish();
	        	
	            return true;
	        case R.id.action_login:
	        	
	        	Intent intent = new Intent().setClass(
						this,
						LoginActivity.class);
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
	            
	        case R.id.action_mypage:
	        	intent = new Intent().setClass(
						this,
						MyArticleActivity.class);
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
}
