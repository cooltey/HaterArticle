package com.cooltey.hater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
 
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
 
import android.os.AsyncTask;
import android.util.Log;
 
public class jsonParser extends AsyncTask<String, Void, JSONObject> {
 
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
 
    // constructor
    public jsonParser() {
 
    }
 
    

	@Override
	protected JSONObject doInBackground(String... url) {
		 // Making HTTP request
        try {
        	
        	URL aURL = new URL(url[0]);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            is = conn.getInputStream();
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
 
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
 
        // return JSON String
        return jObj;
	}
	
	@Override
    protected void onPostExecute(JSONObject result) {

    }
	
    @Override
    protected void onPreExecute() {


    }
}
