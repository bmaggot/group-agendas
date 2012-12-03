package com.groupagendas.groupagenda.events;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.calendar.adapters.ResponsesAdapter;

public class ResponsesActivity extends Activity {

	ResponsesAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.responses_threads);

		ListView responses_thread_list = (ListView) findViewById(R.id.responses_list);

		boolean success = false;
		String error = null;
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		Account acc = new Account(this);
		try {
			JSONObject object = new JSONObject(acc.getResponses());
			success = object.getBoolean("success");

			if (success == false) {
				error = object.getString("error");
				Log.e("getResponsesList - error: ", error);
			} else {
				JSONArray gs = object.getJSONArray("items");
				int count = gs.length();
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						JSONObject g = gs.getJSONObject(i);
						list.add(g);
					}
				}
			}
		} catch (Exception ex) {
			Log.e("JSON err", ex.getMessage());
		}
		adapter = new ResponsesAdapter(ResponsesActivity.this, list);
		responses_thread_list.setAdapter(adapter);

	}

}
