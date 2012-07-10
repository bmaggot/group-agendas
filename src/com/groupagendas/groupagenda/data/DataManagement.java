package com.groupagendas.groupagenda.data;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;

public class DataManagement {

	private static DataManagement _instance = null;
	private static final HashMap<String, Integer> states = new HashMap<String, Integer>();

	private DataManagement(Activity c) {
		Data.set_prefs(c.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE));
		Data.setmContext(c);
	}

	private DataManagement(Context c) {
		Data.set_prefs(c.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE));
		Data.setmContext(c);
	}

	public static synchronized DataManagement getInstance(Activity c) {
		if (_instance == null)
			_instance = new DataManagement(c);
		return _instance;
	}

	public static synchronized DataManagement getInstance(Context c) {
		if (_instance == null)
			_instance = new DataManagement(c);
		return _instance;
	}

	public void fillStates() {
		states.put("mobile/login", 1);
	}

	/**
	 * Returns an boolean that determines if connection to server was
	 * successful. The path argument must specify a relative link to project's
	 * API. The parts argument contains request data.
	 * 
	 * This method always returns immediately, whether or not the connection was
	 * successful.
	 * 
	 * @param path
	 *            an absolute URL giving the base location of the image
	 * @param parts
	 *            the location of the image, relative to the url argument
	 * @return request state (successful or not)
	 * @see MultipartEntity
	 */
	public boolean connect(String path, ArrayList<String[]> parts) {

		boolean success = false;
		HttpClient hc = new DefaultHttpClient();
		HttpPost post = new HttpPost(Data.getDEFAULT_SERVER_URL() + path);

		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		for (String[] part : parts) {
			try {
				reqEntity.addPart(part[1], new StringBody(part[2]));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		post.setEntity(reqEntity);

		HttpResponse rp = null;
		try {
			rp = hc.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			success = true;
			try{
				executeTask(path);
			} catch (Exception e) {
				e.getMessage();
			}
		} else {
			success = false;
		}
		return success;
	}
	
	private void executeTask (String path) throws Exception{
		switch (states.get(path)) {
		case 1:
			loginTask();
			break;
		}
	}

	private void loginTask() {

	}

}
