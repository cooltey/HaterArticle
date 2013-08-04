package com.cooltey.hater;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ironsource.mobilcore.MobileCore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class AboutActivity extends SherlockFragmentActivity {

	private String storedName;
	private String storedPwd;
	private String storedIndex;
	
	private String aboutUrl = "";
	
	private TextView aboutContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//MobileCore.init(AboutActivity.this, "", MobileCore.LOG_TYPE.PRODUCTION);
		//MobileCore.getSlider().setContentViewWithSlider(AboutActivity.this, R.layout.activity_about);
		
		setContentView(R.layout.activity_about);
		
		SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
		
		storedName 		= getStoredData.getString("hm_name", "");
		storedPwd 		= getStoredData.getString("hm_pwd", "");
		storedIndex 	= getStoredData.getString("hm_index", "");
		
		//get view
		aboutContent = (TextView) findViewById(R.id.about_content);
		aboutContent.setText(getAboutContent());
		

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	private String getAboutContent(){		
		String returnVal = "";
		// Creating JSON Parser instance
		jsonParser jParser = new jsonParser();
		 
		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = jParser.execute(aboutUrl).get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		 
		try {
		    // Getting Array of Contacts
			String returnString = json.getString("data");
			
			returnVal = returnString;
			
		} catch (JSONException e) {
		    e.printStackTrace();
		}
		
		return returnVal;
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
