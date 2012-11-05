package com.groupagendas.groupagenda.utils;

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

import android.os.AsyncTask;

import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.error.report.Reporter;
import com.groupagendas.groupagenda.https.MySSLSocketFactory;

public class PrefixReceiver extends AsyncTask <String, Void, String> {
	
	@Override
	protected String doInBackground (String... country) {
		HttpClient hc = MySSLSocketFactory.getNewHttpClient();
		HttpPost post = new HttpPost(Data.getServerUrl() + "mobile/get_country_code");
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		String phonePrefix = null;
		try {
			reqEntity.addPart("country_name", new StringBody(country[0]));
		} catch (UnsupportedEncodingException e1) {
			Reporter.reportError(DataManagement.getContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e1.getMessage());
		}
		post.setEntity(reqEntity);
		HttpResponse rp;
		try {
			rp = hc.execute(post);
			if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String response = EntityUtils.toString(rp.getEntity());
				JSONObject js = new JSONObject(response);
				if (js.getBoolean("success")) {
					phonePrefix = js.getString("country_code");
				}
			}
		} catch (ClientProtocolException e) {
			Reporter.reportError(DataManagement.getContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		} catch (IOException e) {
			Reporter.reportError(DataManagement.getContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		} catch (JSONException e) {
			Reporter.reportError(DataManagement.getContext(), this.getClass().toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
		}
		Data.localPrefix = phonePrefix;
		return phonePrefix;
	}
	
	protected void onPostExecute (String... countryCode) {
		Data.localPrefix = countryCode[0];
	}
}