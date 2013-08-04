package com.cooltey.hater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class articleListAdapter extends BaseAdapter {
    
    private static LayoutInflater inflater = null;
    private ArrayList<HashMap<String, String>> dataArray;
    private JSONArray jsonData;
    private ViewGroup container;
    private int totalNum;
    private int[] animationCounter;
   
    // dialog view
    private LinearLayout process_status;							
	private ScrollView dialog_action;
	private LinearLayout score_list;
	private TextView score_title;
	private TextView score_btn;
	private View score_dialog_vi;
    
    private String scoreListUrl = "";
    private JSONArray scoreArray = null;
    
    public articleListAdapter(ViewGroup c, LayoutInflater layoutInflater, JSONArray j, int t) {
        jsonData	= j;
        inflater 	= layoutInflater;
        container   = c;
        totalNum	= t;
        

    	//animationCounter = new int[jsonData.length()];
    }

    public int getCount() {
    	return jsonData.length();
    } 

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        //Log.d("getCount=>totalNum", getCount() + "=>" + totalNum);
        if(totalNum > 0){
	        if((position+1) != getCount() || totalNum <= 10){
	        
		        vi = inflater.inflate(R.layout.article_list_item, null);
		        try {
		        	
		        	JSONObject jData 		= jsonData.getJSONObject(position);
			         
			        // Storing each json item in variable
					final String ha_index 		= jData.getString("ha_index");
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
			        name.setText(container.getResources().getString(R.string.article_name_prefix) + hm_name);
			        date.setText(container.getResources().getString(R.string.article_date_prefix) + ha_date);
			        pos_num.setText(ha_pos_score + container.getResources().getString(R.string.article_pos_string));
			        pos_num.setTag(ha_pos_score);
			        neg_num.setText(ha_neg_score + container.getResources().getString(R.string.article_neg_string));
			        neg_num.setTag(ha_neg_score);
			        comments_num.setText(ha_comments_num + container.getResources().getString(R.string.article_comment_string));
			        
			        name.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Bundle bundle = new Bundle();
							bundle.putString("hm_name", hm_name);
							Intent intent = new Intent().setClass(
									container.getContext(),
									UserArticleActivity.class);
				        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				        	intent.putExtras(bundle);
				        	container.getContext().startActivity(intent);
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
			        
			        /*
			        	if(animationCounter[position] == 0){
					        Animation animation = null;
					        animation = AnimationUtils.loadAnimation(container.getContext(), R.anim.push_up_in);
		
					        animation.setDuration(1000);
					        vi.startAnimation(animation);
							animationCounter[position] = 1;
					        animation = null;
			        	}*/
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }else{        	
	 	        vi = inflater.inflate(R.layout.progress_view, null); 	        
	        }
        }else{
 	        vi = inflater.inflate(R.layout.no_article_view, null); 	          	
        }
        return vi;
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
				
				View dialog_item_vi 			= inflater.inflate(R.layout.score_list_item, null);
				
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
								container.getContext(),
								UserArticleActivity.class);
			        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        	intent.putExtras(bundle);
			        	container.getContext().startActivity(intent);
					}
		        	
		        });
				
				hm_name_view.setOnTouchListener(onTouchListener);
				
				//add view into main view
				score_list.addView(dialog_item_vi);
			}
		
	    	if(scoreArray.length() < 1){
	    		TextView tv = new TextView(container.getContext());
	    		tv.setText(container.getContext().getString(R.string.dialog_no_score_member));
	    		score_list.addView(tv);
	    	}
	    	
    	}catch (Exception e){
    		TextView tv = new TextView(container.getContext());
    		tv.setText(container.getContext().getString(R.string.dialog_no_score_member));
    		score_list.addView(tv);
    	}
    }
    
    private void checkScoreList(final String ha_index, final String hs_mode){

		score_dialog_vi = inflater.inflate(R.layout.dialog_score_list, null);				 			
		process_status 	= (LinearLayout) score_dialog_vi.findViewById(R.id.process_status);							
		dialog_action 	= (ScrollView) score_dialog_vi.findViewById(R.id.dialog_action);
		score_list 		= (LinearLayout) score_dialog_vi.findViewById(R.id.score_list);
		score_title		= (TextView) score_dialog_vi.findViewById(R.id.score_title);
		score_btn		= (TextView) score_dialog_vi.findViewById(R.id.score_btn);

		if(hs_mode.equals("0")){
			score_title.setText(container.getContext().getString(R.string.dialog_score_title_prefix) + container.getContext().getString(R.string.dialog_pos_btn) + container.getContext().getString(R.string.dialog_score_title_lastfix));
			score_btn.setText(container.getContext().getString(R.string.dialog_give_score_btn) + container.getContext().getString(R.string.dialog_pos_btn));
		}else{
			score_title.setText(container.getContext().getString(R.string.dialog_score_title_prefix) + container.getContext().getString(R.string.dialog_neg_btn) + container.getContext().getString(R.string.dialog_score_title_lastfix));
			score_btn.setText(container.getContext().getString(R.string.dialog_give_score_btn) + container.getContext().getString(R.string.dialog_neg_btn));
		}
		
		score_btn.setVisibility(View.GONE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(container.getContext());
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
    
    public void updateResults(JSONArray newArray) {
    	jsonData = newArray;
        //Triggers the list update
    	/*
    	int[] newAnimationCounter = new int[jsonData.length()];
    	
    	for(int i = 0; i < animationCounter.length; i++){
    		newAnimationCounter[i] = animationCounter[i];
    	}
    	
    	animationCounter = new int[jsonData.length()];
    	animationCounter = newAnimationCounter;
    	*/
        notifyDataSetChanged();
    }
    
    /**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show, final View processView, final View contentView) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = container.getResources().getInteger(
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
}