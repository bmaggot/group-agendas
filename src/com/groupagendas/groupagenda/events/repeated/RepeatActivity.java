package com.groupagendas.groupagenda.events.repeated;

import com.groupagendas.groupagenda.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class RepeatActivity extends Activity implements OnClickListener {
	public class ActivityPrefs {
		public static final String ACTIVITY_MODE_KEY = "activity_mode";
		public static final int ACTIVITY_MODE_MAIN = 0;
		public static final int ACTIVITY_MODE_DAILY = 1;
		public static final int ACTIVITY_MODE_WEEKLY = 2;
		public static final int ACTIVITY_MODE_MONTHLY = 3;
		public static final int ACTIVITY_MODE_YEARLY = 4;
	}
	
	private int ACTIVITY_MODE;
	private LinearLayout activityBody;
	private ScrollView activityScrollWrapper;
	private SharedPreferences prefs;
	private Editor editor;
	
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.repeat);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		prefs = RepeatActivity.this.getSharedPreferences(ActivityPrefs.ACTIVITY_MODE_KEY, MODE_PRIVATE);
		editor = prefs.edit();
		
		ACTIVITY_MODE = prefs.getInt(ActivityPrefs.ACTIVITY_MODE_KEY, ActivityPrefs.ACTIVITY_MODE_MAIN);
		loadBody();
	}

	@Override
	public void onClick(View v) {
		int v_id = v.getId();
		
		switch (v_id) {
		case R.id.button_repeat_never:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_MAIN;
			break;
		case R.id.button_repeat_daily:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_DAILY;
			break;
		case R.id.button_repeat_weekly:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_WEEKLY;
			break;
		case R.id.button_repeat_monthly:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_MONTHLY;
			break;
		case R.id.button_repeat_yearly:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_YEARLY;
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onPause() {
		editor.putInt(ActivityPrefs.ACTIVITY_MODE_KEY, ACTIVITY_MODE);
		editor.commit();
		
		super.onPause();
	}
	
	@Override
	public void onStop() {
		editor.putInt(ActivityPrefs.ACTIVITY_MODE_KEY, ActivityPrefs.ACTIVITY_MODE_MAIN);
		editor.commit();
	}
	
	private void loadBody() {
		activityScrollWrapper = (ScrollView) findViewById(R.id.repeat_scroll_wrapper);
		activityScrollWrapper.removeAllViews();
		
		switch (ACTIVITY_MODE) {
		case ActivityPrefs.ACTIVITY_MODE_MAIN:
			activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_main_menu, null);
			activityScrollWrapper.addView(activityBody);
			break;
		case ActivityPrefs.ACTIVITY_MODE_DAILY:
			activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_main_menu, null);
			activityScrollWrapper.addView(activityBody);
			break;
		case ActivityPrefs.ACTIVITY_MODE_WEEKLY:
			activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_main_menu, null);
			activityScrollWrapper.addView(activityBody);
			break;
		case ActivityPrefs.ACTIVITY_MODE_MONTHLY:
			activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_main_menu, null);
			activityScrollWrapper.addView(activityBody);
			break;
		case ActivityPrefs.ACTIVITY_MODE_YEARLY:
			activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_main_menu, null);
			activityScrollWrapper.addView(activityBody);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if (ACTIVITY_MODE == ActivityPrefs.ACTIVITY_MODE_MAIN) {
			super.onBackPressed();
		} else {
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_MAIN;
			loadBody();
		}
	}
}