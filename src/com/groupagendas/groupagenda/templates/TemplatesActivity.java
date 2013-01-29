package com.groupagendas.groupagenda.templates;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.EventActivity;

public class TemplatesActivity extends ListActivity {
	private ArrayList<Template> content;
	private TemplatesAdapter adapter;
	private ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.templates_activity);

		list = getListView();
		content = DataManagement.getTemplateProjectionsFromLocalDb(TemplatesActivity.this);
		adapter = new TemplatesAdapter(TemplatesActivity.this, content);
		list.setAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		((TextView) findViewById(R.id.listTitle)).setText(R.string.templates);
		findViewById(R.id.clear_button).setVisibility(View.GONE);
//		new GetTemplatesTask().execute();
	}

/*	class GetTemplatesTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			adapter.notifyDataSetChanged();		
			return null;
		}
	}
*/
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id) {
		int t_id = adapter.getTemplateId(v);
		Template template = DataManagement.getTemplateFromLocalDb(TemplatesActivity.this, t_id);
		EventActivity.setTemplateData(TemplatesActivity.this, template);
		finish();
	}
}
