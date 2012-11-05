package com.groupagendas.groupagenda.error.report;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.https.MySSLSocketFactory;

public class Reporter {

	private static HttpClient hc = MySSLSocketFactory.getNewHttpClient();
	private static HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/error_put");
	private static MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	private static HttpResponse rp;
	private static Context context;

	public static void reportError(Context context, String className, String methodName, String errorName) {
		Reporter.context = context;
		reportError(context, className, methodName, errorName, false);
	}

	public static void reportError(Context context, String className, String methodName, String errorName, boolean sendToDb) {
		if (sendToDb) {
			if (!methodName.equals("getAccountFromRemoteDb") && !methodName.equals("getContactsFromRemoteDb")
					&& !methodName.equals("getGroupsFromRemoteDb") && !methodName.equals("getEventsFromRemoteDb")
					&& !methodName.equals("doInBackground")) {
				String error = "Class: " + className + " Method : " + methodName + " Error Name: " + errorName;
				Reporter reporter = new Reporter();
				reporter.report(error);
			}
		}
	}

	public void report(String error) {
		new ErrorReporter().execute(error);
	}

	class ErrorReporter extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String error = params[0];
			try {
				if (Data.getToken(context) != null) {
					reqEntity.addPart("token", new StringBody(Data.getToken(context)));
				} else {
					reqEntity.addPart("token", new StringBody("No token"));
				}
				reqEntity.addPart("error", new StringBody(error));
				reqEntity.addPart("app_version", new StringBody(android.os.Build.VERSION.RELEASE));
				reqEntity.addPart("phone_model", new StringBody(android.os.Build.MODEL));
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					rp = hc.execute(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						Log.e("ERROR: ", error);
						if (resp != null) {
							JSONObject object = null;
							try {
								object = new JSONObject(resp);
							} catch (JSONException e) {
								Log.e("reportError: error while creating JSONObject ", e.getMessage());
							}
							try {
								if (object.getBoolean("success")) {
									System.out.println("Reporter: Error sent to DB!");
								} else {
									Log.e("Reporter: ", "error while sending error to DB ");
								}
							} catch (JSONException e) {
								Log.e("reportError: error while sending error to DB ", e.getMessage());
							}
						}
					}
				} else {
					Log.e("Reporter", "I HAS NO INTERNET!");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
