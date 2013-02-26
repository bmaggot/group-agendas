package com.groupagendas.groupagenda.events.repeated;

import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidqa.ArrayWheelAdapter;
import com.androidqa.OnWheelChangedListener;
import com.androidqa.OnWheelScrollListener;
import com.androidqa.WheelView;
import com.groupagendas.groupagenda.R;

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
	private boolean wheelScrolled;
	private OnWheelScrollListener scrolledListener;
	private final OnWheelChangedListener changedListener = new OnWheelChangedListener() {
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			if (!wheelScrolled) {
//				updateStatus();
			}
		}
	};
	
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
			loadBody();
			break;
		case R.id.button_repeat_daily:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_DAILY;
			loadBody();
			break;
		case R.id.button_repeat_weekly:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_WEEKLY;
			loadBody();
			break;
		case R.id.button_repeat_week_select:
			loadWeekList();
			break;
		case R.id.button_repeat_monthly:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_MONTHLY;
			loadBody();
			break;
		case R.id.cancel:
			finish();
			break;
		case R.id.save:
			finish();
			break;
		case R.id.button_repeat_yearly:
			ACTIVITY_MODE = ActivityPrefs.ACTIVITY_MODE_YEARLY;
			loadBody();
			break;
		case R.id.button_repeat_each:
			((View) findViewById(R.id.repeat_month_table)).setVisibility(View.VISIBLE);
			((View) findViewById(R.id.repeat_onthe_wheels)).setVisibility(View.GONE);
			break;
		case R.id.button_repeat_on:
			((View) findViewById(R.id.repeat_month_table)).setVisibility(View.GONE);
			((View) findViewById(R.id.repeat_onthe_wheels)).setVisibility(View.VISIBLE);
			break;
		default:
			Toast.makeText(RepeatActivity.this, "Boobs!", Toast.LENGTH_SHORT).show();
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
		
		super.onStop();
	}
	
	private void loadBody() {
		activityScrollWrapper = (ScrollView) findViewById(R.id.repeat_scroll_wrapper);
		activityScrollWrapper.removeAllViews();
		
		switch (ACTIVITY_MODE) {
		case ActivityPrefs.ACTIVITY_MODE_MAIN:
			activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_main_menu, null);
			((TextView) findViewById(R.id.topText)).setText(R.string.repeat); 
			activityScrollWrapper.addView(activityBody);
			break;
		case ActivityPrefs.ACTIVITY_MODE_DAILY:
			loadDailyMenu();
			break;
		case ActivityPrefs.ACTIVITY_MODE_WEEKLY:
			loadWeeklyMenu();
			break;
		case ActivityPrefs.ACTIVITY_MODE_MONTHLY:
			loadMonthlyMenu();
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
	
	private void loadDailyMenu() {
		activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_daily_menu, null);
		((TextView) findViewById(R.id.topText)).setText(R.string.daily); 
		
		for (int iterator = 0; iterator < activityBody.getChildCount(); iterator++) {
			((Button) activityBody.getChildAt(iterator)).setText((iterator+1) + " " + getString(R.string.day));
			activityBody.getChildAt(iterator).setTag(""+(iterator+1));
		}
		activityScrollWrapper.addView(activityBody);
	}
	
	private void loadWeeklyMenu() {
		activityBody = (LinearLayout) View.inflate(RepeatActivity.this, R.layout.repeat_weekly_menu, null);
		((TextView) findViewById(R.id.topText)).setText(R.string.weekly); 
		
		for (int iterator = 0; iterator < activityBody.getChildCount(); iterator++) {
			if (iterator == 0) {
				((Button) activityBody.getChildAt(iterator)).setText(getString(R.string.repeat_every) + " " + " " + getString(R.string.week).toLowerCase(Locale.ENGLISH));
			}
			
			if (iterator > 1) {
				((Button) activityBody.getChildAt(iterator)).setText(getResources().getStringArray(R.array.week_days_names)[(iterator-2)]);
				activityBody.getChildAt(iterator).setTag(""+(iterator-1));
			}
		}
		activityScrollWrapper.addView(activityBody);
	}
	
	private void loadWeekList() {
		Calendar cal = Calendar.getInstance();
		int weekAmount = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);
		int paddingSize = Math.round(10*getResources().getDisplayMetrics().density);
		((TextView) findViewById(R.id.topText)).setText(R.string.repeat_every); 
		
		activityScrollWrapper.removeAllViews();
		activityBody.removeAllViews();
		
		for (int iterator = 0; iterator < weekAmount; iterator++) {
			Button b = (Button) View.inflate(RepeatActivity.this, R.layout.repeat_week_list_entry, null);
			
			if (iterator == 0) {
				b.setBackgroundResource(R.drawable.event_invite_people_button_notalone);
			}
			if (iterator > 0) {
				b.setBackgroundResource(R.drawable.event_invited_entry_notalone_background);
				b.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
			}
			if (iterator == (weekAmount-1)) {
				b.setBackgroundResource(R.drawable.event_invited_entry_last_background);
				b.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
			}
			b.setText((iterator+1) + " " + getString(R.string.week).toLowerCase(Locale.ENGLISH));
			b.setTag("" + (iterator+1));
			
			activityBody.addView(b);
		}
		activityScrollWrapper.addView(activityBody);
	}
	
	private void loadMonthlyMenu() {
		WheelView firstLastWheel;
		WheelView weekdayWheel;
		ArrayWheelAdapter<String> wheel1Adapter;
		ArrayWheelAdapter<String> wheel2Adapter;
		
		String[] weekno = getResources().getStringArray(R.array.first_last);
		String[] weekdays = getResources().getStringArray(R.array.week_days_names);
		RelativeLayout nuActivityBody = (RelativeLayout) View.inflate(RepeatActivity.this, R.layout.repeat_monthly_menu, null);
		RelativeLayout.LayoutParams bParams =  new RelativeLayout.LayoutParams(activityScrollWrapper.getWidth(), activityScrollWrapper.getHeight());
		nuActivityBody.setLayoutParams(bParams);
		
		((TextView) findViewById(R.id.topText)).setText(R.string.monthly); 
		activityScrollWrapper.addView(nuActivityBody);
		
		firstLastWheel = (WheelView) findViewById(R.id.repeat_onthe_wheel1);
		weekdayWheel = (WheelView) findViewById(R.id.repeat_onthe_wheel2);
		wheel1Adapter = new ArrayWheelAdapter<String>(RepeatActivity.this, weekno);
		wheel2Adapter = new ArrayWheelAdapter<String>(RepeatActivity.this, weekdays);
		
		scrolledListener = new OnWheelScrollListener() {

			@Override
			public void onScrollingFinished(WheelView view) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onScrollingStarted(WheelView view) {
				// TODO Auto-generated method stub
			}
		};
		
		initWheel(firstLastWheel, wheel1Adapter);
		initWheel(weekdayWheel, wheel2Adapter);

	}

	private void initWheel(WheelView view, ArrayWheelAdapter<String> adapter) {
		view.setViewAdapter(adapter);
		view.setVisibleItems(2);
		view.setCurrentItem(0);
		view.addChangingListener(changedListener);
		view.addScrollingListener(scrolledListener);
	}
}