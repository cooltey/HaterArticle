package com.cooltey.hater;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ironsource.mobilcore.MobileCore;
import com.viewpagerindicator.TabPageIndicator;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends SherlockFragmentActivity{

	private String storedName;
	private String storedPwd;
	private String storedIndex;
	
	// category loader
	private String categoryUrl = ""; 
	private JSONArray categoryArray = null;
	
	private static String[] CONTENT_ID = null;
	private static String[] CONTENT = null;
	
	private String articleUrl = "";
	
	private FragmentPagerAdapter adapter;
	private TabPageIndicator indicator;
	private ViewPager pager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(checkInternet(true)){	
			
			MobileCore.init(MainActivity.this, "", MobileCore.LOG_TYPE.PRODUCTION);
			MobileCore.getSlider().setContentViewWithSlider(MainActivity.this, R.layout.activity_main);
			
			//setContentView(R.layout.activity_main);
			SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
			
			storedName 		= getStoredData.getString("hm_name", "");
			storedPwd 		= getStoredData.getString("hm_pwd", "");
			storedIndex 	= getStoredData.getString("hm_index", "");			
		
			// create
			getCategoryList();
			
		    adapter = new ArticleContentAdapter(getSupportFragmentManager());
	
	        pager = (ViewPager)findViewById(R.id.pager);
	        pager.setAdapter(adapter);
	
	        indicator = (TabPageIndicator)findViewById(R.id.indicator);
	        indicator.setViewPager(pager);
		}
	}
	
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
			CONTENT = new String[defaultSize];
			CONTENT_ID = new String[defaultSize];
		    // looping through All Contacts
		    for(int i = 0; i < categoryArray.length(); i++){
		        JSONObject c = categoryArray.getJSONObject(i);
		         
		        // Storing each json item in variable
		        String hc_index = c.getString("hc_index");
		        String hc_name 	= c.getString("hc_name");
		        
		        // push values into string array
		        CONTENT[i] = hc_name;
		        CONTENT_ID[i] = hc_index;
		    }
		    
		} catch (JSONException e) {
		    e.printStackTrace();
		}
	}

	class ArticleContentAdapter extends FragmentPagerAdapter {
        public ArticleContentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	String getCategoryId = CONTENT_ID[position];
            return ArticleContentFragment.newInstance(getCategoryId, articleUrl);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
          return CONTENT.length;
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	        
	        if(checkInternet(false)){	
		        getSupportMenuInflater().inflate(R.menu.main, menu);
		        // if its login, then change button
		        if(storedIndex.equals("") || storedIndex.equals("0")){
		        	menu.removeItem(R.id.action_control_panel);
		        }else{
		        	menu.removeItem(R.id.action_login);
		        }
	        }
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_refresh:	
	        		        	
	        	Toast.makeText(getApplicationContext(), getString(R.string.btn_updating), 1000).show();

	        	int getCurrentID = pager.getCurrentItem();
	        	adapter = new ArticleContentAdapter(getSupportFragmentManager());

	            pager = (ViewPager)findViewById(R.id.pager);
	            pager.setAdapter(adapter);
	        	pager.setCurrentItem(getCurrentID);
	        	
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)){
			exitOptionsDialog(getString(R.string.dialog_exit_sure_title), getString(R.string.dialog_exit_sure_content));
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void exitOptionsDialog(String title, String Content) { 
        new AlertDialog.Builder(MainActivity.this) 
        .setTitle(title)
        .setMessage(Content) 
        .setPositiveButton(getString(R.string.dialog_btn_yes), 
        		new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.finish();
					} 
        		}) 
        .setNegativeButton(getString(R.string.dialog_btn_no), 
        		new DialogInterface.OnClickListener(){ 
					@Override
					public void onClick( 
							DialogInterface dialoginterface, int i){ 
					} 
        }) 
        .show();
    }
	
	
	private boolean checkInternet(boolean show)
    {
		boolean result;
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
    	NetworkInfo info=connManager.getActiveNetworkInfo();
    	if (info == null || !info.isConnected())
    	{

    		if(show){
    			exitOptionsDialog(getString(R.string.dialog_internet_title), getString(R.string.dialog_internet_content));
    		}
    		result = false;
    	}else{
    		if (!info.isAvailable())
    		{
    			if(show){
    				exitOptionsDialog(getString(R.string.dialog_internet_title), getString(R.string.dialog_internet_content));
    			}
    			result = false;
    		}else{
    			result = true;
    		}
    	}
		return result;
    	
    }
	
}
