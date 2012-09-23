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
	
	protected Event event;
	
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

	protected Prefs prefs;

	protected ContentValues cv;
	
	protected RelativeLayout addressDetailsPanel;
	protected TextView addressTrigger;
	protected TextView detailsTrigger;
	protected ProgressBar pb;
	protected boolean setOwner = false;
	protected boolean setUid = false;

	
	protected void setEventData(Event event) {

		if (timezoneArray != null) {
			event.timezone = timezoneArray[timezoneSpinner.getSelectedItemPosition()];
		}

		event.description_ = descView.getText().toString();

		// title
		event.setTitle(titleView.getText().toString());

		// calendars
		event.setStartCalendar(startCalendar);
		event.setEndCalendar(endCalendar);

		event.country = countryArray[countrySpinner.getSelectedItemPosition()];
		event.zip = zipView.getText().toString();
		event.city = cityView.getText().toString();
		event.street = streetView.getText().toString();
		event.location = locationView.getText().toString();
		event.go_by = gobyView.getText().toString();
		event.take_with_you = takewithyouView.getText().toString();
		event.cost = costView.getText().toString();
		event.accomodation = accomodationView.getText().toString();
		event.setReminder1(reminder1time);
		event.setReminder2(reminder2time);
		event.setReminder3(reminder3time);
		
	}
	protected void putEventContentValues(ContentValues cv) {
		cv.put(EventsProvider.EMetaData.EventsMetaData.TIMEZONE, event.getTimezone());
		cv.put(EventsProvider.EMetaData.EventsMetaData.DESC, event.getDescription());
		/* It already has been validated */
		cv.put(EventsProvider.EMetaData.EventsMetaData.TITLE, event.getActualTitle());

		cv.put(EventsProvider.EMetaData.EventsMetaData.ICON, event.icon);
		cv.put(EventsProvider.EMetaData.EventsMetaData.COLOR, event.getColor());

		cv.put(EventsProvider.EMetaData.EventsMetaData.TYPE, "Should not be such thing as event TYPE! REMOVE THIS FIELD!!!");

		// not mandatory fields
		cv.put(EventsProvider.EMetaData.EventsMetaData.COUNTRY, event.country);
		cv.put(EventsProvider.EMetaData.EventsMetaData.ZIP, event.getZip());
		cv.put(EventsProvider.EMetaData.EventsMetaData.CITY, event.getCity());
		cv.put(EventsProvider.EMetaData.EventsMetaData.STREET, event.getStreet());
		cv.put(EventsProvider.EMetaData.EventsMetaData.LOCATION, event.getLocation());
		cv.put(EventsProvider.EMetaData.EventsMetaData.GO_BY, event.getGo_by());
		cv.put(EventsProvider.EMetaData.EventsMetaData.TAKE_WITH_YOU, event.getTake_with_you());
		cv.put(EventsProvider.EMetaData.EventsMetaData.COST, event.getCost());
		cv.put(EventsProvider.EMetaData.EventsMetaData.ACCOMODATION, event.getAccomodation());
		
		
		if (setOwner) {
			// owner
			cv.put(EventsProvider.EMetaData.EventsMetaData.IS_OWNER, 1);
		}
		if (setUid) {
			// user_id
			cv.put(EventsProvider.EMetaData.EventsMetaData.USER_ID,
					prefs.getUserId());
		}
		// reminders
		
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER1, event.getReminder1());
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER2, event.getReminder2());
		cv.put(EventsProvider.EMetaData.EventsMetaData.REMINDER3, event.getReminder3());
		
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
