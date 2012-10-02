package com.groupagendas.groupagenda.events;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;

public class EventActivity extends Activity {
	public static final int DEFAULT_EVENT_DURATION_IN_MINS = 30;
	public static final String EXTRA_STRING_FOR_START_CALENDAR = "strTime";

	protected DataManagement dm;
	protected DateTimeUtils dtUtils;
	protected EditText descView;

	protected Spinner countrySpinner;
	protected String[] countryArray;
	protected EditText cityView;
	protected EditText streetView;
	protected EditText zipView;
	protected Spinner timezoneSpinner;
	protected String[] timezoneArray;

	protected EditText locationView;
	protected EditText gobyView;
	protected EditText takewithyouView;
	protected EditText costView;
	protected EditText accomodationView;
	protected Calendar startCalendar = Calendar.getInstance();
	protected Calendar endCalendar = Calendar.getInstance();
	protected EditText titleView;
	protected Button saveButton;
	protected ImageView iconView;
	protected ImageView colorView;



	protected EditText startView;
	protected Button startButton;

	protected EditText endView;
	protected Button endButton;

	protected final static int DIALOG_START = 0;
	protected final static int DIALOG_END = 1;

	protected String errorStr = "";
	protected static final int DIALOG_ERROR = 0;

	protected final static int REMINDER1 = 11;
	protected Calendar reminder1time;
	protected final static int REMINDER2 = 22;
	protected Calendar reminder2time;
	protected final static int REMINDER3 = 33;
	protected Calendar reminder3time;

	protected final static int ALARM1 = 111;
	protected Calendar alarm1time;
	protected final static int ALARM2 = 222;
	protected Calendar alarm2time;
	protected final static int ALARM3 = 333;
	protected Calendar alarm3time;

	protected Prefs prefs;

	protected ContentValues cv;

	protected RelativeLayout addressDetailsPanel;
	protected TextView addressTrigger;
	protected TextView detailsTrigger;
	protected ProgressBar pb;
	protected boolean setOwner = false;
	protected boolean setUid = false;

	protected Event setEventData(Event event) {

		if (timezoneArray != null) {
			event.timezone = timezoneArray[timezoneSpinner.getSelectedItemPosition()];
		}
		event.setIs_owner(setOwner);
		if (setUid) {
			// user_id
			event.setUser_id(prefs.getUserId());
		}
		event.description_ = descView.getText().toString();
		// title
		event.setTitle(titleView.getText().toString());

		// calendars
		event.setStartCalendar(startCalendar);
		event.setEndCalendar(endCalendar);
		event.setModifiedMillisUtc(Calendar.getInstance().getTimeInMillis());

		event.country = countryArray[countrySpinner.getSelectedItemPosition()];
		event.zip = zipView.getText().toString();
		event.city = cityView.getText().toString();
		event.street = streetView.getText().toString();
		event.location = locationView.getText().toString();
		event.go_by = gobyView.getText().toString();
		event.take_with_you = takewithyouView.getText().toString();
		event.cost = costView.getText().toString();
		event.accomodation = accomodationView.getText().toString();
		if (alarm1time != null) {
			event.setAlarm1(Utils.formatCalendar(alarm1time, DataManagement.SERVER_TIMESTAMP_FORMAT));
		}
		if (alarm2time != null) {
			event.setAlarm2(Utils.formatCalendar(alarm2time, DataManagement.SERVER_TIMESTAMP_FORMAT));
		}
		if (alarm3time != null) {
			event.setAlarm3(Utils.formatCalendar(alarm3time, DataManagement.SERVER_TIMESTAMP_FORMAT));
		}
		event.setReminder1(reminder1time);
		event.setReminder2(reminder2time);
		event.setReminder3(reminder3time);
		return event;

	}

	public String setErrorStr(int testEvent) {
		switch (testEvent) {
		case 1: // no title set
			return getString(R.string.title_is_required);
		case 2: // no timezone set
			return getString(R.string.timezone_required);
		case 3: // calendar fields are null TODO add proper error texts.
			return getString(R.string.invalid_start_end_time);
		case 4: // event start is set after end
			return getString(R.string.invalid_start_end_time);
		case 5: // event duration is 0
			return getString(R.string.invalid_start_end_time);
		default:
			return getString(R.string.unknown_event_error);
		}
	}
}
