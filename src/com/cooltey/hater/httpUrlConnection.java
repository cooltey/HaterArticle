package com.cooltey.hater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class httpUrlConnection {
	private String 	returnValue = null;
	// httpURLConnection by POST (url, name, value)
    public String httpUrlConnection(String pathUrl, String[] postName, String[] postValue){
        try{        
         // Creating HTTP client
         HttpClient httpClient = new DefaultHttpClient();          
         // Creating HTTP Post
         HttpPost httpPost = new HttpPost(pathUrl);
         
         int valueSize = postName.length;
         
         // Building post parameters, key and value pair
         List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(valueSize);
         
         // Push values into array map
         for(int i = 0; i < valueSize; i++)
         {
             nameValuePair.add(new BasicNameValuePair(postName[i], postValue[i]));
         }         
         
         // Url Encoding the POST parameters
         try {
        	 // prevent string decode error
             httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair,"UTF-8"));
         }
         catch (UnsupportedEncodingException e) {
             // writing error to Log
             e.printStackTrace();
         }
         
         // Making HTTP Request
         try {
             HttpResponse response = httpClient.execute(httpPost);
             // get the response content
             InputStream 	in 		= response.getEntity().getContent();
             // convert string
             BufferedReader reader 	= new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
 			 StringBuilder 	str 	= new StringBuilder();
 			 String 		line 	= null;
 			 while((line = reader.readLine()) != null)
 			 {
 			    str.append(line);
 			 }
 			 in.close();
 			 returnValue	= str.toString(); 			 
          
         } catch (ClientProtocolException e) {
             // writing exception to log
             e.printStackTrace();
          
         } catch (IOException e) {
             // writing exception to log
             e.printStackTrace();
         }
        }catch(Exception ex){
        	ex.toString();
        }
		return returnValue;
       }
}
