package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.NavbarActivity;
import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.account.Account;
import com.groupagendas.groupagenda.alarm.AlarmsManagement;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.Group;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.settings.AutoColorItem;
import com.groupagendas.groupagenda.settings.AutoIconItem;
import com.groupagendas.groupagenda.templates.Template;
import com.groupagendas.groupagenda.timezone.CountriesAdapter;
import com.groupagendas.groupagenda.timezone.TimezonesAdapter;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.Prefs;

public class EventActivity extends Activity {
	public static final int DEFAULT_EVENT_DURATION_IN_MINS = 60;
	protected static final int COLOURED_BUBBLE_SIZE = 40;
	
	public static final String EXTRA_STRING_FOR_START_CALENDAR = "strStartTime";
	public static final String EXTRA_STRING_FOR_END_CALENDAR = "strEndTime";

	public static ArrayList<Invited> newInvites;
	public static ArrayList<Contact> selectedContacts;
	public static ArrayList<Group> selectedGroups;

	public static Calendar startCalendar = Calendar.getInstance();
	public static Calendar endCalendar = Calendar.getInstance();
	
	protected DateTimeUtils dtUtils;
	protected static EditText descView;

	protected static TextView countryView;
	protected static EditText cityView;
	protected static EditText streetView;
	protected static EditText zipView;
	protected static TextView timezoneView;

	protected static EditText locationView;
	protected static EditText gobyView;
	protected static EditText takewithyouView;
	protected static EditText costView;
	protected static EditText accomodationView;
	protected static EditText titleView;
	protected Button saveButton;
	protected static ImageView iconView;
	protected static ImageView colorView;
	
	protected static TextView alarm1View;
	protected static TextView alarm2View;
	protected static TextView alarm3View;

	protected LinearLayout addressPanel;
	protected LinearLayout addressLine;

	protected LinearLayout countrySpinnerBlock;
	protected LinearLayout cityViewBlock;
	protected LinearLayout streetViewBlock;
	protected LinearLayout zipViewBlock;
	protected LinearLayout timezoneSpinnerBlock;

	protected LinearLayout detailsPanel;
	protected LinearLayout detailsLine;

	protected LinearLayout locationViewBlock;
	protected LinearLayout gobyViewBlock;
	protected LinearLayout takewithyouViewBlock;
	protected LinearLayout costViewBlock;
	protected LinearLayout accomodationViewBlock;
	
	protected LinearLayout alarmContainer1;
	protected LinearLayout alarmContainer2;
	protected LinearLayout alarmContainer3;

	protected boolean addressPanelVisible = true;
	protected boolean detailsPanelVisible = true;
	protected boolean alarmPanelVisible = true;

	protected static TextView startView;
	protected static TextView endView;

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

	protected LinearLayout addressDetailsPanel;
	protected TextView addressTrigger;
	protected TextView detailsTrigger;
	protected RelativeLayout alarmTrigger;
	protected ProgressBar pb;
	protected static String selectedIcon = Event.DEFAULT_ICON;
	protected static String selectedColor = Event.DEFAULT_COLOR;

	public static Event event;
	public static ArrayList<StaticTimezones> countriesList = null;
	public static ArrayList<StaticTimezones> filteredCountriesList = null;
	public static CountriesAdapter countriesAdapter = null;
	public static TimezonesAdapter timezonesAdapter = null;
	protected InvitedAdapter invitedAdapter = null;
	protected LinearLayout invitedPersonList;
	protected ListView invitedPersonListView;
	public static int timezoneInUse = 0;
	protected Button inviteButton;
	protected Button inviteEditButton;
	protected LinearLayout invitesColumn;
	protected RelativeLayout invitationResponseLine;
	protected TextView invitationResponseStatus;
	protected TextView response_button_yes;
	protected TextView response_button_no;
	protected TextView response_button_maybe;

	protected Account account;

	protected View inviteDelegate1;
	protected RelativeLayout inviteDelegate2;

	private static boolean editInvited = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dtUtils = new DateTimeUtils(this);
		newInvites = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	protected Event setEventData(Event event) {

		if (timezoneInUse > 0) {
			event.setTimezone(countriesList.get(timezoneInUse).timezone);
		}
		
		event.setDescription(descView.getText().toString());
		event.setTitle(titleView.getText().toString());

		// calendars
		event.setStartCalendar(startCalendar);
		event.setEndCalendar(endCalendar);
		event.setModifiedMillisUtc(Calendar.getInstance().getTimeInMillis());

		if (timezoneInUse > 0)
			event.setCountry(countriesList.get(timezoneInUse).country_code);
		event.setZip(zipView.getText().toString());
		event.setCity(cityView.getText().toString());
		event.setStreet(streetView.getText().toString());
		event.setLocation(locationView.getText().toString());
		event.setGo_by(gobyView.getText().toString());
		event.setTake_with_you(takewithyouView.getText().toString());
		event.setCost(costView.getText().toString());
		event.setAccomodation(accomodationView.getText().toString());
		if(NavbarActivity.alarmReceiver == null){
			NavbarActivity.refreshAlarmReceiver();
		}
		if (alarm1time != null || alarm2time != null || alarm3time != null) {
			long alarm1 = 0;
			long alarm2 = 0;
			long alarm3 = 0;
			if(alarm1time != null){
				alarm1time.clear(Calendar.SECOND);
				alarm1time.clear(Calendar.MILLISECOND);
				alarm1 = alarm1time.getTimeInMillis();
			}
			if (alarm2time != null) {
				alarm2time.clear(Calendar.SECOND);
				alarm2time.clear(Calendar.MILLISECOND);
				alarm2 = alarm2time.getTimeInMillis();
			}
			if (alarm3time != null) {
				alarm3time.clear(Calendar.SECOND);
				alarm3time.clear(Calendar.MILLISECOND);
				alarm3 = alarm3time.getTimeInMillis();
			}
			AlarmsManagement.setAlarmsForEvent(getApplicationContext(), alarm1, 0, alarm2, 0, alarm3, 0, event.getEvent_id());
		}

		
		event.setReminder1(reminder1time);
		event.setReminder2(reminder2time);
		event.setReminder3(reminder3time);
		event.setIcon(selectedIcon);
		event.setColor(selectedColor);

		if (event.getInvited().size() > 0) {
			event.setType(Event.SHARED_EVENT);
		} else {
			event.setType(Event.NOTE);
		}

		if (event.getColor().equals(Event.DEFAULT_COLOR)) {
			if (!event.getType().equals(Event.NOTE)) {
				if (event.getStatus() == Invited.ACCEPTED) {
					event.setDisplayColor(Event.DEFAULT_COLOR_ATTENDING);
				} else if (!event.isBirthday()) {
					event.setDisplayColor(Event.DEFAULT_COLOR_MAYBE);
				} else {
					event.setDisplayColor(Event.DEFAULT_COLOR);
				}
			} else {
				event.setDisplayColor(Event.DEFAULT_COLOR);
			}
		} else {
			if (!event.getType().equals(Event.NOTE)) {
				if (event.getStatus() == Invited.ACCEPTED) {
					event.setDisplayColor(event.getColor());
				} else if (!event.isBirthday()) {
					event.setDisplayColor(Event.DEFAULT_COLOR_MAYBE);
				} else {
					event.setDisplayColor(Event.DEFAULT_COLOR);
				}
			} else {
				event.setDisplayColor(event.getColor());
			}
		}
		event.setSelectedEventPollsTime(event.getSelectedEventPollsTime());

		return event;

	}

	public String setErrorStr(int testEvent) {
		switch (testEvent) {
		case 1: // no title set
			return getString(R.string.title_is_required);
		case 2: // no timezone set
			return getString(R.string.timezone_required);
		case 3: // calendar fields are null
			return getString(R.string.invalid_start_end_time);
		case 4: // event start is set after end
			return getString(R.string.invalid_start_end_time);
		case 5: // event duration is 0
			return getString(R.string.invalid_start_end_time);
		default:
			return getString(R.string.unknown_event_error);
		}
	}

	public void showAddressPanel() {
		addressPanelVisible = true;
		
		timezoneSpinnerBlock.setVisibility(View.VISIBLE);
		countrySpinnerBlock.setVisibility(View.VISIBLE);
		cityViewBlock.setVisibility(View.VISIBLE);
		streetViewBlock.setVisibility(View.VISIBLE);
		zipViewBlock.setVisibility(View.VISIBLE);
	}

	public void hideAddressPanel() {
		addressPanelVisible = false;

		countrySpinnerBlock = (LinearLayout) findViewById(R.id.countrySpinnerBlock);
		countrySpinnerBlock.setVisibility(View.GONE);
		
		cityViewBlock = (LinearLayout) findViewById(R.id.cityViewBlock);
		cityViewBlock.setVisibility(View.GONE);
		
		streetViewBlock = (LinearLayout) findViewById(R.id.streetViewBlock);
		streetViewBlock.setVisibility(View.GONE);
		
		zipViewBlock = (LinearLayout) findViewById(R.id.zipViewBlock);
		zipViewBlock.setVisibility(View.GONE);
	}

	public void showAlarmPanel() {
		alarmPanelVisible = true;
		
		alarmContainer1 = (LinearLayout) findViewById(R.id.alarm_container1);
		alarmContainer1.setVisibility(View.VISIBLE);
		
		alarmContainer2 = (LinearLayout) findViewById(R.id.alarm_container2);
		alarmContainer2.setVisibility(View.VISIBLE);
		
		alarmContainer3 = (LinearLayout) findViewById(R.id.alarm_container3);
		alarmContainer3.setVisibility(View.VISIBLE);
	}

	public void hideAlarmPanel() {
		alarmPanelVisible = false;

		alarmContainer1 = (LinearLayout) findViewById(R.id.alarm_container1);
		alarmContainer1.setVisibility(View.GONE);
		
		alarmContainer2 = (LinearLayout) findViewById(R.id.alarm_container2);
		alarmContainer2.setVisibility(View.GONE);
		
		alarmContainer3 = (LinearLayout) findViewById(R.id.alarm_container3);
		alarmContainer3.setVisibility(View.GONE);
	}

	public void showDetailsPanel() {
		detailsPanelVisible = true;
		
		locationViewBlock.setVisibility(View.VISIBLE);
		gobyViewBlock.setVisibility(View.VISIBLE);
		takewithyouViewBlock.setVisibility(View.VISIBLE);
		costViewBlock.setVisibility(View.VISIBLE);
		accomodationViewBlock.setVisibility(View.VISIBLE);
	}

	public void hideDetailsPanel() {
		detailsPanelVisible = false;

		locationViewBlock = (LinearLayout) findViewById(R.id.locationViewBlock);
		locationViewBlock.setVisibility(View.GONE);
		
		gobyViewBlock = (LinearLayout) findViewById(R.id.gobyViewBlock);
		gobyViewBlock.setVisibility(View.GONE);
		
		takewithyouViewBlock = (LinearLayout) findViewById(R.id.takewithyouViewBlock);
		takewithyouViewBlock.setVisibility(View.GONE);

		costViewBlock = (LinearLayout) findViewById(R.id.costViewBlock);
		costViewBlock.setVisibility(View.GONE);
		
		accomodationViewBlock = (LinearLayout) findViewById(R.id.accomodationViewBlock);
		accomodationViewBlock.setVisibility(View.GONE);
	}

	public class StaticTimezones {
		public String id;
		public String city;
		public String country;
		public String country2;
		public String country_code;
		public String timezone;
		public String altname;
		public String call_code;
	}

	protected void showInvitesView(Context context) {
		if (newInvites != null) {
			event.getInvited().addAll(newInvites);
			newInvites = null;
		}

		int invitedListSize = event.getInvited().size();
		invitedPersonList.removeAllViews();
		if (invitedListSize == 0) {
			((View) inviteButton.getParent()).setBackgroundResource(R.drawable.event_invite_people_button_standalone);
		} else {
			boolean contains = false;
			account = new Account(context);
			String fullname = account.getFullname();

			for (Invited i : event.getInvited()) {
				if (i.getName().equals(fullname)) {
					contains = true;
				}
			}

			if (!contains) {
				Invited me = new Invited();
				me.setGuid(account.getUser_id());
				me.setName(fullname);
				me.setStatus(Invited.ACCEPTED);
				event.setStatus(Invited.ACCEPTED);
				event.getInvited().add(me);
				invitedListSize++;
			}

			((View) inviteButton.getParent()).setBackgroundResource(R.drawable.event_invite_people_button_notalone);
			invitedAdapter = new InvitedAdapter(this, event.getInvited(), event.getEvent_id(), this);
			for (int i = 0; i < invitedListSize; i++) {
				View view = invitedAdapter.getView(i, null, null);
				invitedPersonList.addView(view);
			}
//			invitedPersonListView.setAdapter(invitedAdapter);
			
//			invitedAdapter.notifyDataSetChanged();
		}

	}

	// TODO write a javadoc for respondToInvitation(int response)
	protected void respondToInvitation(int response) {
		TextView myStatus;
		invitesColumn = (LinearLayout) findViewById(R.id.invitesLine);
		LinearLayout myInvitation = (LinearLayout) invitesColumn.findViewWithTag(Invited.OWN_INVITATION_ENTRY);
		if (myInvitation != null) {
			myStatus = (TextView) myInvitation.findViewById(R.id.invited_status);
		} else {
			myStatus = new TextView(EventActivity.this);
		}

		switch (response) {
		case 0:
			event.setStatus(0);
			invitationResponseStatus.setText(this.getString(R.string.status_not_attending));
			myStatus.setText(this.getString(R.string.status_not_attending));
			myStatus.setBackgroundColor(Color.parseColor("#5d5d5d")); // TODO hardcoded color-code
			response_button_yes.setVisibility(View.VISIBLE);
			response_button_maybe.setVisibility(View.VISIBLE);
			response_button_no.setVisibility(View.INVISIBLE);
			break;
		case 1:
			event.setStatus(1);
			invitationResponseStatus.setText(this.getString(R.string.status_attending));
			myStatus.setText(this.getString(R.string.status_attending));
			myStatus.setBackgroundColor(Color.parseColor("#66AEBA")); // TODO hardcoded color-code
			response_button_yes.setVisibility(View.INVISIBLE);
			response_button_maybe.setVisibility(View.VISIBLE);
			response_button_no.setVisibility(View.VISIBLE);
			break;
		case 2:
			event.setStatus(2);
			invitationResponseStatus.setText(this.getString(R.string.status_maybe));
			myStatus.setText(this.getString(R.string.status_maybe));
			myStatus.setBackgroundColor(Color.parseColor("#b5b5b5")); // TODO hardcoded color-code
			response_button_yes.setVisibility(View.VISIBLE);
			response_button_maybe.setVisibility(View.INVISIBLE);
			response_button_no.setVisibility(View.VISIBLE);
			break;
		case 4:
			event.setStatus(4);
			invitationResponseStatus.setText(this.getString(R.string.status_new_invite));
			response_button_yes.setVisibility(View.VISIBLE);
			response_button_maybe.setVisibility(View.VISIBLE);
			response_button_no.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	protected void setAutoIcon(Context context) {
		ArrayList<AutoIconItem> autoIcons = DataManagement.getAutoIcons(context);

		for (AutoIconItem autoIcon : autoIcons) {
			if (event.getTitle().contains(autoIcon.keyword)) {
				event.setIcon(autoIcon.icon);
				break;
			}
		}
	}

	protected void setAutoColor(Context context) {
		ArrayList<AutoColorItem> autoColors = DataManagement.getAutoColors(context);

		for (AutoColorItem autoColor : autoColors) {
			if (event.getTitle().contains(autoColor.keyword)) {
				event.setColor(autoColor.color);
				break;
			}
		}
	}
	
	public void sendSms(){
		if(selectedContacts != null && selectedContacts.size() > 0){
			Account acc = new Account(getApplicationContext());
			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");
			String tempNumber = "";
			DateTimeUtils dateTimeUtils = new DateTimeUtils(getApplicationContext());
			for(Contact con : selectedContacts){
				//if(!con.getColor().contentEquals("00a759")){
				if(con.registered != null){
					if(!con.registered.contentEquals("true")){
						if(con.email != null && con.email.contentEquals("")){
							tempNumber += con.phone1_code + con.phone1 + ",";
						}
					}
				} else {
					if(con.email != null && con.email.contentEquals("")){
						tempNumber += con.phone1_code + con.phone1 + ",";
					}
				}
			}
			if(!tempNumber.contentEquals("")){
				String smsBody = acc.getFullname() + " " + getString(R.string.sms_invited) + "\n\n"
						+ event.getTitle() + "\n\n"
						+ getString(R.string.sms_begins) + " " +dateTimeUtils.formatDate(event.getStartCalendar()) + " " 
						+ dateTimeUtils.formatTime(event.getStartCalendar()) + " "
						+ getString(R.string.sms_till) + " " + dateTimeUtils.formatDate(event.getEndCalendar()) + " "
						+ dateTimeUtils.formatTime(event.getEndCalendar()) + "\n\n"
						+ getString(R.string.sms_end_1) + " " + acc.getFullname() + " " + getString(R.string.sms_end_2);
				smsIntent.putExtra("address", tempNumber);
				smsIntent.putExtra("sms_body", smsBody);
				startActivity(smsIntent);
			}
		}
	}
	
	public static void setCalendar (Calendar startTime, Calendar endTime) {
		Calendar tmpCal;
		
		startCalendar.setTime(startTime.getTime());
		tmpCal = (Calendar) startCalendar.clone();
		tmpCal.add(Calendar.HOUR_OF_DAY, 1);
		if (endTime.after(tmpCal)) {
			endCalendar.setTime(endTime.getTime());
		} else {
			endCalendar.setTime(tmpCal.getTime());
		}
		
	}

	public static void setTimezone(int timezoneInUse) {
		EventActivity.timezoneInUse = timezoneInUse;
	}
	
	public void setEditInvited (boolean changed) {
		editInvited = changed;
	}
	
	public boolean getEditInvited () {
		return editInvited;
	}
	
	public static void setTemplateData(Context context, Template template) {
		titleView.setText(template.getTitle());
		
		EventActivity.selectedColor = template.getColor();
		EventActivity.colorView.setBackgroundDrawable(new BitmapDrawable(DrawingUtils
				.getColoredRoundSquare(context,
						COLOURED_BUBBLE_SIZE, 5,
						selectedColor, false)));
		EventActivity.selectedIcon = template.getIcon();
		EventActivity.iconView.setImageResource(template.getIconId(context));		
//		startCalendar = template.getStartCalendar();
//		endCalendar = template.getEndCalendar();
		EventActivity.timezoneView.setText(template.getTimezone());
		
		EventActivity.descView.setText(template.getDescription_());
		
//		countryView.setText(template.getCountry());
		EventActivity.cityView.setText(template.getCity());
		EventActivity.streetView.setText(template.getStreet());
		EventActivity.zipView.setText(template.getZip());
		
		EventActivity.locationView.setText(template.getLocation());
		EventActivity.gobyView.setText(template.getGo_by());
		EventActivity.takewithyouView.setText(template.getTake_with_you());
		EventActivity.costView.setText(template.getCost());
		EventActivity.accomodationView.setText(template.getAccomodation());
		
		EventActivity.newInvites = template.getInvited();
		
		setTimezone(template.getTimezoneInUse());
	}
}