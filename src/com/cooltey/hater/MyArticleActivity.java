package com.cooltey.hater;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cooltey.hater.MainActivity.ArticleContentAdapter;
import com.ironsource.mobilcore.MobileCore;
import com.viewpagerindicator.TabPageIndicator;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;

public class MyArticleActivity extends SherlockFragmentActivity{

	private String storedName;
	private String storedPwd;
	private String storedIndex;
	
	// category loader
	private String categoryUrl = " "; 
	private JSONArray categoryArray = null;

	private static String[] CONTENT_ID = null;
	private static String[] CONTENT = null;
	
	private String articleUrl = " ";
	
	private FragmentPagerAdapter adapter;
	private TabPageIndicator indicator;
	private ViewPager pager;
	
	// display view
	private RelativeLayout enter_layout;
	private LinearLayout   main_layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		enter_layout 	= (RelativeLayout) findViewById(R.id.enter_layout);
		main_layout 	= (LinearLayout) findViewById(R.id.main_layout);
		
		SharedPreferences getStoredData = getSharedPreferences("account_info", 0);
		
		storedName 		= getStoredData.getString("hm_name", "");
		storedPwd 		= getStoredData.getString("hm_pwd", "");
		storedIndex 	= getStoredData.getString("hm_index", "");
		
        
        final Handler handler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		
        		main_layout.setVisibility(View.VISIBLE);
        		enter_layout.setVisibility(View.GONE);

    		    adapter = new MyArticleContentAdapter(getSupportFragmentManager());
    	
    	        pager = (ViewPager)findViewById(R.id.pager);
    	        pager.setAdapter(adapter);
    	
    	        indicator = (TabPageIndicator)findViewById(R.id.indicator);
    	        indicator.setViewPager(pager);
        	}
    	};
    	
    	new Thread
		(
	        new Runnable() 
			{
	        	@Override
				public void run() 
				{      			
	        		main_layout.setVisibility(View.GONE);
	        		enter_layout.setVisibility(View.VISIBLE);
	    			// create
	    			getCategoryList();		
					handler.sendEmptyMessage(0);
				}
			}	
         ).start(); 

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
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

	class MyArticleContentAdapter extends FragmentPagerAdapter {
        public MyArticleContentAdapter(FragmentManager fm) {
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
	        getSupportMenuInflater().inflate(R.menu.main, menu);


	        // if its login, then change button
	        if(storedIndex.equals("") || storedIndex.equals("0")){
	        	menu.removeItem(R.id.action_control_panel);
	        }else{
	        	menu.removeItem(R.id.action_login);
	        }
		return true;
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
	        case R.id.action_refresh:	
	        		        	
	        	Toast.makeText(getApplicationContext(), getString(R.string.btn_updating), 1000).show();

	        	int getCurrentID = pager.getCurrentItem();
	        	adapter = new MyArticleContentAdapter(getSupportFragmentManager());

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
}
