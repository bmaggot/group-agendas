package com.groupagendas.groupagenda.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;

public class StartEndDateTimeSelectDialog extends Dialog implements OnClickListener {
	public static final int SECTION_DATE = 0;
	public static final int SECTION_TIME = 1;
	public static final int SECTION_ALL = 2;
	
	private RadioButton timeButton;
	private RadioButton dateButton;
	
	private Button incYear;
	private Button incMonth;
	private Button incDay;
	private Button incHour;
	private Button incMinute;

	private Button decYear;
	private Button decMonth;
	private Button decDay;
	private Button decHour;
	private Button decMinute;
	
	private Button okButton;
	private Button resetButton;
	private Button cancelButton;

	private TextView yearView;
	private TextView monthView;
	private TextView dayView;
	private TextView hourView;
	private TextView minuteView;
	
	private Calendar mCalendar;
	private Calendar mDestination;
	private SimpleDateFormat sdf;
	
	private OnDateSetListener mOnDateSetListener;
	private Context mContext;
	private Account acc;

	@Deprecated
	public StartEndDateTimeSelectDialog(Context context) {
		super(context);
		setContentView(R.layout.start_end_time_picker);
	}
	
	public StartEndDateTimeSelectDialog(Context context, int state, Calendar cal, OnDateSetListener listener) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.start_end_time_picker);
		
		mContext = context;
		mCalendar = (Calendar) cal.clone();
		mDestination = cal;
		sdf = new SimpleDateFormat();
		mOnDateSetListener = listener;
		acc = new Account(mContext);
		
		this.initViewItems();
		this.updateShownDate();
		this.attachClickListener();
		
		switch (state) {
		case SECTION_DATE:
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			dateButton.performClick();
			break;
		case SECTION_TIME:
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			timeButton.performClick();
			break;
		case SECTION_ALL:
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			dateButton.setVisibility(View.GONE);
			timeButton.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		
	}
	
	private void showDate() {
		incYear.setVisibility(View.VISIBLE);
		incMonth.setVisibility(View.VISIBLE);
		incDay.setVisibility(View.VISIBLE);
		incHour.setVisibility(View.GONE);
		incMinute.setVisibility(View.GONE);
		
		yearView.setVisibility(View.VISIBLE);
		monthView.setVisibility(View.VISIBLE);
		dayView.setVisibility(View.VISIBLE);
		hourView.setVisibility(View.GONE);
		minuteView.setVisibility(View.GONE);
		
		decYear.setVisibility(View.VISIBLE);
		decMonth.setVisibility(View.VISIBLE);
		decDay.setVisibility(View.VISIBLE);
		decHour.setVisibility(View.GONE);
		decMinute.setVisibility(View.GONE);
	}
	
	private void showTime() {
		incYear.setVisibility(View.GONE);
		incMonth.setVisibility(View.GONE);
		incDay.setVisibility(View.GONE);
		incHour.setVisibility(View.VISIBLE);
		incMinute.setVisibility(View.VISIBLE);
		
		yearView.setVisibility(View.GONE);
		monthView.setVisibility(View.GONE);
		dayView.setVisibility(View.GONE);
		hourView.setVisibility(View.VISIBLE);
		minuteView.setVisibility(View.VISIBLE);
		
		decYear.setVisibility(View.GONE);
		decMonth.setVisibility(View.GONE);
		decDay.setVisibility(View.GONE);
		decHour.setVisibility(View.VISIBLE);
		decMinute.setVisibility(View.VISIBLE);
	}

	@SuppressWarnings("unused")
	private void showAll() {
		incYear.setVisibility(View.VISIBLE);
		incMonth.setVisibility(View.VISIBLE);
		incDay.setVisibility(View.VISIBLE);
		incHour.setVisibility(View.VISIBLE);
		incMinute.setVisibility(View.VISIBLE);
		
		yearView.setVisibility(View.VISIBLE);
		monthView.setVisibility(View.VISIBLE);
		dayView.setVisibility(View.VISIBLE);
		hourView.setVisibility(View.VISIBLE);
		minuteView.setVisibility(View.VISIBLE);
		
		decYear.setVisibility(View.VISIBLE);
		decMonth.setVisibility(View.VISIBLE);
		decDay.setVisibility(View.VISIBLE);
		decHour.setVisibility(View.VISIBLE);
		decMinute.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
		case R.id.incYear:
			mCalendar.add(Calendar.YEAR, 1);
			sdf = new SimpleDateFormat("yyyy");
			yearView.setText(sdf.format(mCalendar.getTime()));
			sdf = new SimpleDateFormat("d E");
			dayView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.incMonth:
			mCalendar.add(Calendar.MONTH, 1);
			if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMinimum(Calendar.MONTH)) {
				mCalendar.add(Calendar.YEAR, -1);
			}
			sdf = new SimpleDateFormat("MMM");
			monthView.setText(sdf.format(mCalendar.getTime()));
			sdf = new SimpleDateFormat("d E");
			dayView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.incDay:
			mCalendar.add(Calendar.DAY_OF_MONTH, 1);
			sdf = new SimpleDateFormat("d E");
			if (mCalendar.get(Calendar.DAY_OF_MONTH) == mCalendar.getActualMinimum(Calendar.DAY_OF_MONTH)) {
				mCalendar.add(Calendar.MONTH, -1);
			}
			dayView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.incHour:
			if (mCalendar.get(Calendar.HOUR_OF_DAY) == 23) {
				mCalendar.add(Calendar.DAY_OF_MONTH, -1);
			}
			mCalendar.add(Calendar.HOUR_OF_DAY, 1);
			if (acc.getSetting_ampm() == 1) {
				sdf = new SimpleDateFormat("hh a");
			} else {
				sdf = new SimpleDateFormat("HH");
			}
			hourView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.incMinute:
			mCalendar.add(Calendar.MINUTE, 1);
			if ((mCalendar.get(Calendar.HOUR_OF_DAY) == 0) && (mCalendar.get(Calendar.MINUTE) == 0)) {
				mCalendar.add(Calendar.HOUR_OF_DAY, -1);
				if (acc.getSetting_ampm() == 1) {
					sdf = new SimpleDateFormat("hh a");
				} else {
					sdf = new SimpleDateFormat("HH");
				}
				hourView.setText(sdf.format(mCalendar.getTime()));
			}
			sdf = new SimpleDateFormat("mm");
			minuteView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.decYear:
			mCalendar.add(Calendar.YEAR, -1);
			sdf = new SimpleDateFormat("yyyy");
			yearView.setText(sdf.format(mCalendar.getTime()));
			sdf = new SimpleDateFormat("d E");
			dayView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.decMonth:
			mCalendar.add(Calendar.MONTH, -1);
			if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMaximum(Calendar.MONTH)) {
				mCalendar.add(Calendar.YEAR, 1);
			}
			sdf = new SimpleDateFormat("MMM");
			monthView.setText(sdf.format(mCalendar.getTime()));
			sdf = new SimpleDateFormat("d E");
			dayView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.decDay:
			mCalendar.add(Calendar.DAY_OF_MONTH, -1);
			sdf = new SimpleDateFormat("d E");
			if (mCalendar.get(Calendar.DAY_OF_MONTH) == mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
				mCalendar.add(Calendar.MONTH, 1);
			}
			dayView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.decHour:
			if (mCalendar.get(Calendar.HOUR_OF_DAY) == 0) {
				mCalendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			mCalendar.add(Calendar.HOUR_OF_DAY, -1);
			if (acc.getSetting_ampm() == 1) {
				sdf = new SimpleDateFormat("hh a");
			} else {
				sdf = new SimpleDateFormat("HH");
			}
			hourView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.decMinute:
			mCalendar.add(Calendar.MINUTE, -1);
			if ((mCalendar.get(Calendar.HOUR_OF_DAY) == 23) && (mCalendar.get(Calendar.MINUTE) == 59)) {
				mCalendar.add(Calendar.HOUR_OF_DAY, 1);
				if (acc.getSetting_ampm() == 1) {
					sdf = new SimpleDateFormat("hh a");
				} else {
					sdf = new SimpleDateFormat("HH");
				}
				hourView.setText(sdf.format(mCalendar.getTime()));
			}
			sdf = new SimpleDateFormat("mm");
			minuteView.setText(sdf.format(mCalendar.getTime()));
			break;
		case R.id.SetDateTime:
			this.updateCalendar();
			this.dismiss();
			break;
		case R.id.ResetDateTime:
			this.resetShownDate();
			break;
		case R.id.CancelDialog:
			this.dismiss();
			break;
		case R.id.startTime:
			showDate();
			break;
		case R.id.endTime:
			showTime();
			break;
		default:
			break;
		}
	}
	
	private void initViewItems() {
		incYear = (Button) findViewById(R.id.incYear);
		incMonth = (Button) findViewById(R.id.incMonth);
		incDay = (Button) findViewById(R.id.incDay);
		incHour = (Button) findViewById(R.id.incHour);
		incMinute = (Button) findViewById(R.id.incMinute);
		
		yearView = (TextView) findViewById(R.id.yearView);
		monthView = (TextView) findViewById(R.id.monthView);
		dayView = (TextView) findViewById(R.id.dayView);
		hourView = (TextView) findViewById(R.id.hourView);
		minuteView = (TextView) findViewById(R.id.minuteView);
		
		decYear = (Button) findViewById(R.id.decYear);
		decMonth = (Button) findViewById(R.id.decMonth);
		decDay = (Button) findViewById(R.id.decDay);
		decHour = (Button) findViewById(R.id.decHour);
		decMinute = (Button) findViewById(R.id.decMinute);
		
		okButton = (Button) findViewById(R.id.SetDateTime);
		resetButton = (Button) findViewById(R.id.ResetDateTime);
		cancelButton = (Button) findViewById(R.id.CancelDialog);
		
		timeButton = (RadioButton) findViewById(R.id.endTime);
		dateButton = (RadioButton) findViewById(R.id.startTime);
	}

	private void attachClickListener() {
		incYear.setOnClickListener(this);
		incMonth.setOnClickListener(this);
		incDay.setOnClickListener(this);
		incHour.setOnClickListener(this);
		incMinute.setOnClickListener(this);
		
		decYear.setOnClickListener(this);
		decMonth.setOnClickListener(this);
		decDay.setOnClickListener(this);
		decHour.setOnClickListener(this);
		decMinute.setOnClickListener(this);
		
		okButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		
		timeButton.setOnClickListener(this);
		dateButton.setOnClickListener(this);
	}
	
	private void updateShownDate() {
		sdf = new SimpleDateFormat("yyyy");
		yearView.setText(sdf.format(mCalendar.getTime()));
		sdf = new SimpleDateFormat("MMM");
		monthView.setText(sdf.format(mCalendar.getTime()));
		sdf = new SimpleDateFormat("d E");
		dayView.setText(sdf.format(mCalendar.getTime()));
		if (acc.getSetting_ampm() == 1) {
			sdf = new SimpleDateFormat("hh a");
		} else {
			sdf = new SimpleDateFormat("HH");
		}
		hourView.setText(sdf.format(mCalendar.getTime()));
		sdf = new SimpleDateFormat("mm");
		minuteView.setText(sdf.format(mCalendar.getTime()));
	}
	
	private void updateCalendar() {
		mDestination.setTime(mCalendar.getTime());
		mOnDateSetListener.onDateSet(
				null, 
				mCalendar.get(Calendar.YEAR), 
				mCalendar.get(Calendar.MONTH), 
				mCalendar.get(Calendar.DAY_OF_MONTH)
		);
	}
	
	private void resetShownDate() {
		mCalendar.setTime(mDestination.getTime());
		this.updateShownDate();
	}
}