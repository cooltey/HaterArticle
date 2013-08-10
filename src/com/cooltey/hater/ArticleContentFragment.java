package com.cooltey.hater;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cooltey.hater.LoginActivity.UserLoginTask;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class ArticleContentFragment extends Fragment {
	
    private static final String KEY_CATE_ID = "KEY_CATE_ID";
    private static final String KEY_URL_ID = "KEY_URL_ID";
    
    private JSONArray articleArray = null;
    
    private int totalNum;
   
    public articleListAdapter aAdapter;
    
    public static ArticleContentFragment newInstance(String cateId, String url) {
    	
        ArticleContentFragment fragment = new ArticleContentFragment();
        
        fragment.articleUrl = url;
        
        fragment.mCateId = cateId;
        return fragment;
    }
    
    
    // dialog process setting
    private AlertDialog dialog;
    private LinearLayout processView;
    private TextView processMessageView;	
    private LinearLayout contentView;
    private UserActionTask mAuthTask = null;
    private String  scoreUrl = " ";
    
    // dialog action
    private String ha_index;
    private String hs_mode;
    private String hs_score;
    
    // loading article 
    private LinearLayout article_processView;
    private TextView article_processMessageView;	
    private LinearLayout article_contentView;
    private ListView listview_layout;
    private LayoutInflater inflater;
    private ViewGroup container;
    
    private String storedName;
	private String storedPwd;
	private String storedIndex;
    
    // reply content prepare
    public static JSONObject replyContent;
    
    private String mCateId = "all";	
	private String articleUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences getStoredData = getActivity().getSharedPreferences("account_info", 0);
		
      	storedName 		= getStoredData.getString("hm_name", "");
      	storedPwd 		= getStoredData.getString("hm_pwd", "");
      	storedIndex 	= getStoredData.getString("hm_index", "");
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CATE_ID) && savedInstanceState.containsKey(KEY_URL_ID)) {
        	mCateId = savedInstanceState.getString(KEY_CATE_ID);
        	articleUrl = savedInstanceState.getString(KEY_URL_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater minflater, ViewGroup mcontainer, Bundle savedInstanceState) {
    	
    	inflater = minflater;
    	container = mcontainer;
    	
    	final View getLayout = inflater.inflate(R.layout.article_listview, null);
    	RelativeLayout layout = (RelativeLayout) getLayout.findViewById(R.id.article_listview);
    	
    	// loading listview
    	listview_layout 			= (ListView) getLayout.findViewById(R.id.listview);
    	article_processView			= (LinearLayout) getLayout.findViewById(R.id.process_status);
    	article_processMessageView 	= (TextView) getLayout.findViewById(R.id.process_message);
    	
    	listview_layout.setPadding(15, 0, 15, 0);
    	listview_layout.setDividerHeight(0);
 		listview_layout.setDrawSelectorOnTop(false);
 		listview_layout.setSelector(android.R.color.transparent);
 		listview_layout.setVisibility(View.GONE);
 		
    	final Handler handler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
	    		showProgress(false, article_processView, listview_layout);
	    		
		        aAdapter = new articleListAdapter(container, inflater, articleArray, totalNum);
		 		listview_layout.setAdapter(aAdapter);
		 		TextView emptyView = (TextView) getLayout.findViewById(R.id.emptyView);
		 		listview_layout.setEmptyView(emptyView);
        	}
    	};
    	

		showProgress(true, article_processView, listview_layout);
    	
    	new Thread
		(
	        new Runnable() 
			{
	        	@Override
				public void run() 
				{      					
			        articleLoader(1);
					handler.sendEmptyMessage(0);
				}
			}	
         ).start(); 
    	
 		// endless loading
 		EndlessScrollListener eScrollListener = new EndlessScrollListener(inflater, container, listview_layout, 1);
 		
 		listview_layout.setOnScrollListener(eScrollListener);
 		listview_layout.setOnItemClickListener(onItemClickListener);
 		
 		
 		// loading listview
 		
        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CATE_ID, mCateId);
        outState.putString(KEY_URL_ID, articleUrl);
    }

    // listview onitem click listener
    private OnItemClickListener onItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adv, final View itemView, final int position,
				long longNum) {
			// TODO Auto-generated method stub
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			/*builder.setMessage(R.string.dialog_message)
			       .setTitle(R.string.dialog_title);*/
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View dialogView = inflater.inflate(R.layout.dialog_action, null);

			// set the process action
			processView 		= (LinearLayout) dialogView.findViewById(R.id.process_status);
			processMessageView 	= (TextView) dialogView.findViewById(R.id.process_message);
			
			contentView 		= (LinearLayout) dialogView.findViewById(R.id.dialog_action);
			
			TextView pos_btn		= (TextView) dialogView.findViewById(R.id.pos_btn);
			TextView neg_btn		= (TextView) dialogView.findViewById(R.id.neg_btn);
			TextView comments_btn	= (TextView) dialogView.findViewById(R.id.comments_btn);
			
			pos_btn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ha_index = itemView.getTag().toString();
				    hs_mode  = "0";
				    hs_score = "1";
				    
				    attmpAction(itemView);
					    
				}
				
			});

			pos_btn.setOnTouchListener(onTouchListener);
			
			neg_btn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ha_index = itemView.getTag().toString();
				    hs_mode  = "1";
				    hs_score = "-1";

				    attmpAction(itemView);
				}
				
			});
			
			neg_btn.setOnTouchListener(onTouchListener);
			
			comments_btn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					ha_index = itemView.getTag().toString();
					
					try {
						replyContent = articleArray.getJSONObject(position);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Bundle bundle = new Bundle();
					Intent intent = new Intent().setClass(
							getActivity(),
							ReadArticleActivity.class);
		        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					
					dialog.dismiss();
				}
				
			});

			comments_btn.setOnTouchListener(onTouchListener);
			
			processMessageView.setText(R.string.dialog_processing);
			
			builder.setView(dialogView);
			
			dialog = builder.create();
			dialog.show();
		}
    	
    };
    
    // dialog touch event
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
    
    // only load article for json data
    private void articleLoader(int currentPage){
    	//String returnVal = "0";
    	
    	String currentUrl = articleUrl;
    	
    	currentUrl = currentUrl + "" + mCateId + "&page=" + "" +currentPage + "&hm_index=" + storedIndex;
    	
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

 			JSONArray oldArticleArray = articleArray; 	
 			articleArray = json.getJSONArray("data");
 			if(currentPage > 1){
	 			// combine
	 			articleArray = concatArray(oldArticleArray, articleArray);
 			}
 			
 			// get total article
 			if(currentPage == 1){
 				totalNum =  json.getInt("sum");			
 			}
 			
		    //Log.d("jSON Length", articleArray.length() + "");
		    //Log.d("totalNum", totalNum + "");
		} catch (JSONException e) {
		    e.printStackTrace();
		    
		}    	
 		
 		//Log.d("jSON currentUrl", currentUrl);
 		
 		//return returnVal;
    }
    
    public class EndlessScrollListener implements OnScrollListener {

        private int visibleThreshold = 5;
        private LayoutInflater einflater;
        private ViewGroup econtainer;
        private ListView elayout;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener(LayoutInflater inflater, ViewGroup container, ListView layout, int visibleThreshold) {
            this.visibleThreshold 	= visibleThreshold;
            this.einflater 			= inflater;
            this.econtainer 		= container;
            this.elayout 			= layout;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                //new LoadGigsTask().execute(currentPage + 1);
            	
            	final Handler handler = new Handler() {
    	        	@Override
    	        	public void handleMessage(Message msg) {
                        aAdapter.updateResults(articleArray);
    	        	}
            	};
            	
            	new Thread
        		(
        	        new Runnable() 
        			{
        	        	@Override
        				public void run() 
        				{      					
                            articleLoader(currentPage + 1);
    						handler.sendEmptyMessage(0);
        				}
        			}	
                 ).start();   
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
    
    // json merge function
    private JSONArray concatArray(JSONArray arr1, JSONArray arr2)
            throws JSONException {
        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (int i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }
    
    
    // action process
    private void attmpAction(View getItemView){
    	showProgress(true, processView, contentView);
		mAuthTask = new UserActionTask(getItemView);
		mAuthTask.execute(scoreUrl);
    }
    
    // action asyncTask
    public class UserActionTask extends AsyncTask<String, Void, String> {
    	
    	private View itemView;
    	
    	private UserActionTask(View v){
    		itemView = v;
    	}
    	
		@Override
		protected String doInBackground(String... url) {
			// TODO: attempt authentication against a network service.

			httpUrlConnection setConnection = new httpUrlConnection();
			
			String postName[] 	= {"ha_index", "hs_score", "hs_mode", "hm_index"};
			String postValue[]	= {ha_index, hs_score, hs_mode, storedIndex};
			
			// get result
			String finalResult = "";
			finalResult = setConnection.httpUrlConnection(url[0], postName, postValue);
			
			//Toast.makeText(getApplicationContext(), "result" + finalResult, 5000).show();
			// TODO: register the new account here.
			
			Log.d("Process Result: ", finalResult);
			return finalResult;
		}

		@Override
		protected void onPostExecute(final String success) {
			mAuthTask = null;
			//showProgress(false, processView, contentView);

			if (success.contains("success")) {
				Toast.makeText(getActivity(), R.string.dialog_success, 5000).show();	
				
				if(hs_mode.equals("0")){

			        TextView  pos_num =	(TextView)itemView.findViewById(R.id.pos_num);				        
			        int newPos_num = Integer.parseInt(pos_num.getTag().toString()) + 1;
			        pos_num.setTag(newPos_num);
			        pos_num.setText(newPos_num + getString(R.string.article_pos_string));
			        
				}else{

			        TextView  neg_num =	(TextView)itemView.findViewById(R.id.neg_num);
			        int newNeg_num = Integer.parseInt(neg_num.getTag().toString()) + 1;
			        neg_num.setTag(newNeg_num);
			        neg_num.setText(newNeg_num + getString(R.string.article_neg_string));
				}
				
				dialog.cancel();
			}else{
				Toast.makeText(getActivity(), R.string.dialog_error_voted, 5000).show();
				dialog.cancel();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			//dialog.cancel();
			//showProgress(false, processView, contentView);
		}
	}
    
    private void fakeIncreaseCounter(){
    	
    	
    	
    	JSONArray tmpArray = articleArray;
		JSONObject newJData = new JSONObject();
    	
    	try{
	    	for(int i = 0; i < articleArray.length(); i++){
	    		
	    		JSONObject jData = articleArray.getJSONObject(i);
	    		
	    		 // Storing each json item in variable
				String tha_index 		= jData.getString("ha_index");
		        String tha_content 		= jData.getString("ha_content");
		        String thm_name 		= jData.getString("hm_name");
		        String tha_date 		= jData.getString("ha_date");
		        String tha_pos_score 	= jData.getString("ha_pos_score");
		        String tha_neg_score 	= jData.getString("ha_neg_score");
		        String tha_comments_num = jData.getString("ha_comments_num");

	    		if(ha_index.equals(tha_index)){
	    			if(hs_mode.equals("0")){
	    				int new_tha_pos_score = Integer.parseInt(tha_pos_score) + 1;
	    				tha_pos_score = new_tha_pos_score + "";
	    			}else{
	    				int new_tha_neg_score = Integer.parseInt(tha_neg_score) + 1;
	    				tha_neg_score = new_tha_neg_score + "";
	    			}
	    			
	    		}
	    		
	    		// put again
	    		newJData.put("ha_index", tha_index);
	    		newJData.put("ha_content", tha_content);
	    		newJData.put("hm_name", thm_name);
	    		newJData.put("ha_date", tha_date);
	    		newJData.put("ha_pos_score", tha_pos_score);
	    		newJData.put("ha_neg_score", tha_neg_score);
	    		newJData.put("ha_comments_num", tha_comments_num);
	    	}
	    	
	    	tmpArray.put(newJData);
	    	
	    	articleArray = tmpArray;
    		// TODO Auto-generated method stub
    	    aAdapter.updateResults(articleArray);
    	}catch (Exception e){
    		
    	}
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
}
