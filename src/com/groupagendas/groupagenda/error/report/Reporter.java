package com.groupagendas.groupagenda.error.report;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.https.WebService;
import com.groupagendas.groupagenda.utils.CharsetUtils;

public class Reporter {
	private Reporter() {
		// utility class
	}

	public static void reportError(Context context, String className, String methodName, String errorName) {
		reportError(context, className, methodName, errorName, false);
	}

	public static void reportError(Context context, String className, String methodName, String errorName, boolean sendToDb) {
		if (sendToDb) {
			if (!methodName.equals("getAccountFromRemoteDb") && !methodName.equals("getContactsFromRemoteDb")
					&& !methodName.equals("getGroupsFromRemoteDb") && !methodName.equals("getEventsFromRemoteDb")
					&& !methodName.equals("doInBackground")) {
				StringBuilder sb = new StringBuilder("Class: ");
				sb.append(className);
				sb.append(" Method : ").append(methodName);
				sb.append(" Error Name: ");
				sb.append(errorName);
				report(context, sb.toString());
			}
		}
	}

	public static void report(Context context, String error) {
		new ErrorReporter(context).execute(error);
	}

	static class ErrorReporter extends AsyncTask<String, Void, Void> {
		private final Context context;

		ErrorReporter(Context context) {
			this.context = context;
		}

		@Override
		protected Void doInBackground(String... params) {
			String error = params[0];
			WebService webService = new WebService(context);
			try {
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				
				final String token = Data.getToken(context);
				CharsetUtils.addAllParts(reqEntity, "token", token != null ? token : "No token",
						"error", error, "app_version", android.os.Build.VERSION.RELEASE,
						"phone_model", android.os.Build.MODEL);
				
				HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/error_put");
				post.setEntity(reqEntity);
				if (DataManagement.networkAvailable) {
					HttpResponse rp = webService.getResponseFromHttpPost(post);
					if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						String resp = EntityUtils.toString(rp.getEntity());
						Log.e("ERROR: ", error);
						if (resp != null) {
							JSONObject object = null;
							try {
								object = new JSONObject(resp);
							} catch (JSONException e) {
								Log.e("reportError: error while creating JSONObject ", e.getMessage());
								return null;
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
