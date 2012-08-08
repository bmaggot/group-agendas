package com.pass_retrieve;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.error.report.Reporter;

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
public class httpRequestes2 {
    private String path="http://www.groupagendas.com/mobile/";
    private Integer TIMEOUT_MILLISEC=10000;
    private Activity con;

    private String email;
    private String code;
    private String new_password;
    private String new_password2;

    public httpRequestes2(Activity c){
        con=c;
        code= ((EditText) con.findViewById(R.id.forgot_pass2_code)).getText().toString();
        new_password= ((EditText) con.findViewById(R.id.forgot_pass2_new_pas1)).getText().toString();
        new_password2= ((EditText) con.findViewById(R.id.forgot_pass2_new_pass2)).getText().toString();
        Bundle extras =con.getIntent().getExtras();
        email=extras.getString("mail");
    }

    public   void changePass(){
        if(code.length()!=6){showMessage("Code must consist 6 numbers"); return;}
        if (!new_password.equals(new_password2)) {showMessage("Passwords don't match"); return;}
        if (new_password.length()<6){showMessage("Your now password length can't be less 6 symbols");}


        new retrievePass1Task().execute();
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


    private class retrievePass1Task extends AsyncTask<Void, Void, String> {
        public ProgressDialog waitDialog=null;


        protected void onPreExecute() {   waitDialog=ProgressDialog.show(con, "", "Loading. Please wait...", true);}

        protected String doInBackground(Void... arg0) {
            //       public void clickbutton(View v) {
            String responseBody = null;
            try {
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
                HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
                HttpParams p = new BasicHttpParams();

                // Instantiate an HttpClient
                HttpClient httpclient = new DefaultHttpClient(p);
                String url = path + "account_forgot_pass2";
                HttpPost httppost = new HttpPost(url);

                // Instantiate a GET HTTP method
                try {
                    Log.i(getClass().getSimpleName(), "send  task - start");
                    //
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);

                    nameValuePairs.add(new BasicNameValuePair("email", email));
                    nameValuePairs.add(new BasicNameValuePair("code", code));
                    nameValuePairs.add(new BasicNameValuePair("new_password", new_password));
                    nameValuePairs.add(new BasicNameValuePair("new_password2", new_password2));

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

        protected void onPostExecute(String responseBody) {
            waitDialog.dismiss();
          //  Log.d("responseBody", responseBody);
           // Log.d("responseBody", email);

            if(responseBody==null ){  showMessage("Error request, Check internet connection."); return;}
            if(responseBody.equals("-1") ){  showMessage("Request failed"); return;}

            try{
                JSONObject json = new JSONObject(responseBody);
                String  success=json.get("success").toString();
                //Log.d("http1 succ=",success);
                if (success.equals("false")) {

                    showMessage(((JSONObject) json.get("error")).get("reason").toString());

                }else {
                  //  showMessage("pass changed");
                    Toast.makeText(con,"Password changed",Toast.LENGTH_SHORT).show();



                    Intent i=new Intent(con, login2_set.class);
                    i.putExtra("mail",email);
                    i.putExtra("pass",new_password);

                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    con.startActivity(i);
                   // con.finish();
                  //  Intent intent = new Intent(con, NavbarActivity.class);
                  //  intent.putExtra("load_data", true);
                  //  con.startActivity(intent);
                 //   con.finish();
                }
            }catch (Exception e){
            	Reporter.reportError(this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
                return;
            }
            //super.onPostExecute(result);
        }

    }
}
