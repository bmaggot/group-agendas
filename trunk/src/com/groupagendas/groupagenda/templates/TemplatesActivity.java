package com.groupagendas.groupagenda.templates;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventActivity;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.EventsProvider.DatabaseHelper;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData;
import com.groupagendas.groupagenda.events.EventsProvider.EMetaData.EventsMetaData;
import com.groupagendas.groupagenda.templates.TemplatesProvider.TMetaData.TemplatesMetaData;
import com.groupagendas.groupagenda.timezone.LatestEventStructure;
import com.makeramen.segmented.SegmentedRadioGroup;

public class TemplatesActivity extends ListActivity implements OnCheckedChangeListener, OnClickListener {
	class ActivityPrefs {
		static final String ACTIVITY_SETTINGS_KEY = "activity_settings"; 
		
		static final String ACTIVITY_MODE_KEY = "activity_mode";
		static final int ACTIVITY_MODE_TEMPLATES = 0;
		static final int ACTIVITY_MODE_RECENT = 1;
		
		static final String EDIT_MODE_KEY = "editing_mode";
		static final int EDIT_MODE_ON = 1;
		static final int EDIT_MODE_OFF = 0;
	}
	private static int edit_mode;
	private static int activity_mode;
	
	private SharedPreferences prefs;
	private Editor editor;
	
	private ArrayList<Template> content;
	private TemplatesAdapter adapter;
	private ListView list;
	
	private RadioButton section1Button;
	private RadioButton section2Button;
	private SegmentedRadioGroup segmentedButtons;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.templates_activity);

		list = getListView();
		segmentedButtons = (SegmentedRadioGroup) findViewById(R.id.segmentedButtons);
		section1Button = (RadioButton) findViewById(R.id.sectionButton1);
		section2Button = (RadioButton) findViewById(R.id.sectionButton2);

		segmentedButtons.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		prefs = getSharedPreferences(ActivityPrefs.ACTIVITY_SETTINGS_KEY, MODE_PRIVATE);
		editor = prefs.edit();
		edit_mode = prefs.getInt(ActivityPrefs.EDIT_MODE_KEY, ActivityPrefs.EDIT_MODE_OFF);
		activity_mode = prefs.getInt(ActivityPrefs.ACTIVITY_MODE_KEY, ActivityPrefs.ACTIVITY_MODE_TEMPLATES);
		
		switch (activity_mode) {
		case ActivityPrefs.ACTIVITY_MODE_TEMPLATES:
			section1Button.performClick();
			break;
		case ActivityPrefs.ACTIVITY_MODE_RECENT:
			section2Button.performClick();
		}		
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (content == null) {
			content = new ArrayList<Template>();
		}
		
		if (adapter != null) {
			adapter.notifyDataSetInvalidated();
		} else {
			if (content == null) {
				content = new ArrayList<Template>();
				adapter.setList(content);
			}
			adapter = new TemplatesAdapter(TemplatesActivity.this, content);
		}
		
		switch (checkedId) {
		case R.id.sectionButton1:
			activity_mode = ActivityPrefs.ACTIVITY_MODE_TEMPLATES;
			((TextView) findViewById(R.id.listTitle)).setText(R.string.templates);
			findViewById(R.id.clear_button).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.clear_button)).setText(R.string.edit);
			
			ArrayList<Template> nuContent = DataManagement.getTemplateProjectionsFromLocalDb(TemplatesActivity.this);
			if (content == null) {
				content = new ArrayList<Template>();
				adapter.setList(content);
			}
			
			content.clear();
			content.addAll(nuContent);
			list.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			
			break;
			
		case R.id.sectionButton2:
			activity_mode = ActivityPrefs.ACTIVITY_MODE_RECENT;
			((TextView) findViewById(R.id.listTitle)).setText(R.string.recent);
			findViewById(R.id.clear_button).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.clear_button)).setText(R.string.clear);
			
			new GetEventStackTask().execute();
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id) {
		Template template;
		long t_id = adapter.getTemplateId(v);
		switch (activity_mode) {
		case ActivityPrefs.ACTIVITY_MODE_TEMPLATES:
			template = DataManagement.getTemplateFromLocalDb(TemplatesActivity.this, t_id, DataManagement.ID_INTERNAL);
			
			if (template != null) {
				EventActivity.setTemplateData(TemplatesActivity.this, template);
			}
			
			break;
			
		case ActivityPrefs.ACTIVITY_MODE_RECENT:
			Event e = EventManagement.getEventFromLocalDb(TemplatesActivity.this, t_id, EventManagement.ID_INTERNAL);
			if (e != null) {
				template = e.toTemplate(TemplatesActivity.this);
				EventActivity.setTemplateData(TemplatesActivity.this, template);
			}
			break;
			
		default:
			break;
		}
		finish();
	}
	
	class GetEventStackTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<Template> eventStack = new ArrayList<Template>();
			LatestEventStructure les = new LatestEventStructure(TemplatesActivity.this);
			StringBuilder query = new StringBuilder();
			
			if (les.getPresentItemCount() > 0) {
				query.append("SELECT ");
				query.append(TemplatesMetaData._ID + ", ");
				query.append(EventsMetaData.TITLE + ", ");
				query.append(EventsMetaData.COLOR + " ");
				query.append("FROM ");
				query.append(EMetaData.EVENTS_TABLE + " ");
				query.append("WHERE ");
				query.append(TemplatesMetaData._ID + " ");
				query.append("IN (");
				query.append(les.toString());
				query.append(")");
				
				if (EventsProvider.mOpenHelper == null) {
					EventsProvider.mOpenHelper = new DatabaseHelper(TemplatesActivity.this);
				}
				
				Cursor c = EventsProvider.mOpenHelper.getReadableDatabase().rawQuery(query.toString(), null);
				
				if (c.moveToFirst()) {
					while (!c.isAfterLast()) {
						Template temp = new Template();
						temp.setInternalID(c.getLong(c.getColumnIndex(TemplatesMetaData._ID)));
						temp.setTemplate_title(c.getString(c.getColumnIndex(EventsMetaData.TITLE)));
						temp.setColor(c.getString(c.getColumnIndex(EventsMetaData.COLOR)));
						
						eventStack.add(temp);
						
						c.moveToNext();
					}
				}
				
				c.close();
			}
			
			if (content == null) {
				content = new ArrayList<Template>();
				adapter.setList(content);
			}
			
			content.clear();
			content.addAll(eventStack);

			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			adapter.notifyDataSetChanged(); 
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.clear_button:
			if (activity_mode == ActivityPrefs.ACTIVITY_MODE_TEMPLATES) {
				switch (edit_mode) {
				case ActivityPrefs.EDIT_MODE_ON:
					edit_mode = ActivityPrefs.EDIT_MODE_OFF;
					adapter.toggleEdit(edit_mode);
					break;
				case ActivityPrefs.EDIT_MODE_OFF:
					edit_mode = ActivityPrefs.EDIT_MODE_ON;
					adapter.toggleEdit(edit_mode);
					break;
				default:
					break;
				}
			} else {
				LatestEventStructure les = new LatestEventStructure(TemplatesActivity.this);
				if (les.clearItems()) {
					content.clear();
					adapter.notifyDataSetChanged();
					Toast.makeText(TemplatesActivity.this, getString(R.string.templates_were_cleared), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(TemplatesActivity.this, getString(R.string.templates_werent_cleared), Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.template_remove:
			long removal_id = adapter.getTemplateId((View) v.getParent());
			if (removal_id > 0) {
				Template template = DataManagement.getTemplateFromLocalDb(TemplatesActivity.this, removal_id, DataManagement.ID_INTERNAL);
				
				if (template != null) {
					new DeleteTemplateTask().execute(template);
				}
			}
			break;
		default:
			break;
		}	
	}
	
	@Override
	public void onPause() {
		editor.putInt(ActivityPrefs.ACTIVITY_MODE_KEY, activity_mode);
		editor.putInt(ActivityPrefs.EDIT_MODE_KEY, edit_mode);
		editor.commit();
		
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		editor.putInt(ActivityPrefs.ACTIVITY_MODE_KEY, ActivityPrefs.ACTIVITY_MODE_TEMPLATES);
		editor.putInt(ActivityPrefs.EDIT_MODE_KEY, ActivityPrefs.EDIT_MODE_OFF);
		editor.commit();
		
		super.onDestroy();
	}
	
	class DeleteTemplateTask extends AsyncTask<Template, Void, Void> {

		@Override
		protected void onPreExecute() {
			Toast.makeText(TemplatesActivity.this, R.string.deleting_template, Toast.LENGTH_SHORT).show();
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Template... params) {
			ArrayList<Template> nuContent = new ArrayList<Template>();
			
			if (params[0] != null) {
				DataManagement.deleteTemplate(TemplatesActivity.this, params[0]);
			}
			
			nuContent = DataManagement.getTemplateProjectionsFromLocalDb(TemplatesActivity.this);
			
			content.clear();
			content.addAll(nuContent);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			adapter.notifyDataSetChanged(); 
		}
	}
}
