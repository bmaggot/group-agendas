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

import android.util.Log;

import com.groupagendas.groupagenda.data.Data;

public class Reporter {

	public static void reportError(String className, String methodName, String errorName) {
		String error = "Class: " + className + " Method : " + methodName + " Error Name: " + errorName;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/error_put");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			if (Data.getToken() != null) {
				reqEntity.addPart("token", new StringBody(Data.getToken()));
			} else {
				reqEntity.addPart("token", new StringBody("No token"));
			}
			reqEntity.addPart("error", new StringBody(error));
			reqEntity.addPart("app_version", new StringBody(android.os.Build.VERSION.RELEASE));
			reqEntity.addPart("phone_model", new StringBody(android.os.Build.MODEL));
			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);
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
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
