package com.pass_retrieve;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import com.groupagendas.groupagenda.error.report.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: brodjag
 * Date: 13.06.12
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
public class httpRequestes {
    private String path="http://www.groupagendas.com/mobile/";
    private Integer TIMEOUT_MILLISEC=10000;
    private Activity con;

    public httpRequestes(Activity c){
        con=c;

    }

 private String mail;
 public   void sendToEmail(String mmail){
     mail=mmail;
    new retrievePass1Task().execute(mail);
 }


 public void showMessage(String message){
     AlertDialog ad = new AlertDialog.Builder(con).create();
     ad.setCancelable(false); // This blocks the 'BACK' button
     ad.setMessage(message);
     ad.setButton("OK", new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
             dialog.dismiss();
         }
     });
     ad.show();
 }


    private class retrievePass1Task extends AsyncTask<String, Void, String> {
        public ProgressDialog waitDialog=null;


        @Override
		protected void onPreExecute() {   waitDialog=ProgressDialog.show(con, "", "Loading. Please wait...", true);}

        @Override
		protected String doInBackground(String... arg0) {
            //       public void clickbutton(View v) {
            String responseBody = null;
            try {
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
                HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
                HttpParams p = new BasicHttpParams();

                // Instantiate an HttpClient
                HttpClient httpclient = new DefaultHttpClient(p);
                String url = path + "account_forgot_pass1";
                HttpPost httppost = new HttpPost(url);

                // Instantiate a GET HTTP method
                try {
                    Log.i(getClass().getSimpleName(), "send  task - start");
                    //
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("email", arg0[0]));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    responseBody = httpclient.execute(httppost, responseHandler);

                } catch (ClientProtocolException e) {
                	Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
                } catch (IOException e) {
                	Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
                }
                // Log.i(getClass().getSimpleName(), "send  task - end");

            } catch (Throwable t) {
                responseBody="-1";
                Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), t.getMessage());
            }


            return responseBody;
        }

        @Override
		protected void onPostExecute(String responseBody) {
            waitDialog.dismiss();

            if(responseBody==null ){  showMessage("Error request, Check internet connection."); return;}
            if(responseBody.equals("-1") ){  showMessage("Request failed"); return;}

            try{
            JSONObject json = new JSONObject(responseBody);
            String  success=json.get("success").toString();
            //Log.d("http1 succ=",success);
            if (success.equals("false")) {
                showMessage("Your email not found in DB. Please, verify email's value");

            }else {
                //showMessage("Email with retrieve code was sent to your mail.");
                Intent i=new Intent(con,forgot_pass2.class);
                i.putExtra("mail",mail);
                con.startActivity(i);
                con.finish();
            }
            }catch (Exception e){
            	Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage()); 
                return;
            }
            //super.onPostExecute(result);
        }

    }
}