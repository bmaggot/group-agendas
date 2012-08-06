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

import android.util.Log;

import com.groupagendas.groupagenda.data.Data;

public class Reporter {
	
	public static void reportError(String className, String methodName, String errorName){
		String error = "Class: " + className+" Method : "+methodName+" Error Name: "+ errorName;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/error_put");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			reqEntity.addPart("token", new StringBody(Data.getToken()));
			reqEntity.addPart("error", new StringBody(error));
			reqEntity.addPart("app_version", new StringBody(android.os.Build.VERSION.RELEASE));
			reqEntity.addPart("phone_model", new StringBody(android.os.Build.MODEL));
			post.setEntity(reqEntity);
			HttpResponse rp = hc.execute(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Log.e("ERROR: ",error);
				System.out.println("Error sent to DB!");
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
