package com.groupagendas.groupagenda;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import at.bartinger.list.item.EntryAdapter;
import at.bartinger.list.item.EntryItem;
import at.bartinger.list.item.Item;
import at.bartinger.list.item.SectionItem;
import az.mecid.android.ActionItem;
import az.mecid.android.QuickAction;

import com.bog.calendar.app.model.CalendarDay;
import com.bog.calendar.app.model.CalendarMonth;
import com.bog.calendar.app.model.CalendarYear;
import com.groupagendas.groupagenda.account.AccountProvider;
import com.groupagendas.groupagenda.calendar.day.DayView;
import com.groupagendas.groupagenda.contacts.Contact;
import com.groupagendas.groupagenda.contacts.ContactsActivity;
import com.groupagendas.groupagenda.contacts.ContactsProvider;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsActivity;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.events.NewEventActivity;
import com.groupagendas.groupagenda.utils.AgendaUtils;
import com.groupagendas.groupagenda.utils.Prefs;
import com.groupagendas.groupagenda.utils.Utils;

public class NavbarActivity extends Activity {
	

	private final int DEFAULT_FIRST_DAY_OF_WEEK = Calendar.MONDAY;
	
	private DataManagement dm;
	
	private QuickAction qa;
	
	private ActionItem list_search;
	private ActionItem go_date;
	private ActionItem year;
	private ActionItem month;
	private ActionItem week;
	private ActionItem day;
	private ActionItem agenda;
	private ActionItem mini_month;
	private ActionItem today;
		
	static final int PROGRESS_DIALOG = 0;
    private ProgressThread progressThread;
    private ProgressDialog progressDialog;
    
    private FrameLayout calendarContainer;
	private LayoutInflater mInflater; 
	
	private EditText searchView;
	private EntryAdapter entryAdapter;
	private ViewState viewState = ViewState.MONTH;

	
	
	
	private Prefs prefs;


	public static boolean showInvites = false;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.actnavbar);
		
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		calendarContainer = (FrameLayout) findViewById(R.id.calendarContainer);
		
		
		dm = DataManagement.getInstance(this);
		
		Intent intent = getIntent();
		if(intent.getBooleanExtra("load_data", false)){
			showDialog(PROGRESS_DIALOG);
			DataManagement.updateAppData(5);
		}
		
		list_search = new ActionItem();
		list_search.setTitle(getString(R.string.list_search));
		list_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				new GetAllEventsTask().execute();
			}
		});
		
		go_date = new ActionItem();
		go_date.setTitle(getString(R.string.go_to_date));
		go_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				Toast.makeText(NavbarActivity.this, getString(R.string.go_to_date), Toast.LENGTH_SHORT).show();
			}
		});
		
		year = new ActionItem();
		year.setTitle(getString(R.string.year));
		year.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				calendarContainer.removeAllViews();
				mInflater.inflate(R.layout.calendar_year, calendarContainer);
				new CalendarYear(NavbarActivity.this);
			}
		});
		
		month = new ActionItem();
		month.setTitle(getString(R.string.month));
		month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				calendarContainer.removeAllViews();
				mInflater.inflate(R.layout.calendar_month, calendarContainer);
				new CalendarMonth(NavbarActivity.this);
			}
		});
		
		week = new ActionItem();
		week.setTitle(getString(R.string.week));
		week.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				Toast.makeText(NavbarActivity.this, getString(R.string.week), Toast.LENGTH_SHORT).show();
			}
		});
		
		day = new ActionItem();
		day.setTitle(getString(R.string.day));
		day.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				calendarContainer.removeAllViews();
//				DayView view = (DayView)
				mInflater.inflate(R.layout.calendar_day, calendarContainer);
				DayView view = (DayView)calendarContainer.getChildAt(0);
				view.setupViewItems();
				view.init();
//				int test = view.getMeasuredHeight();
//				calendarContainer.addView(new DayView(NavbarActivity.this));
				System.out.println("test");
			}
		});
		
		agenda = new ActionItem();
		agenda.setTitle(getString(R.string.agenda));
		agenda.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				Toast.makeText(NavbarActivity.this, getString(R.string.agenda), Toast.LENGTH_SHORT).show();
			}
		});
		
		mini_month = new ActionItem();
		mini_month.setTitle(getString(R.string.mini_month));
		mini_month.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				Toast.makeText(NavbarActivity.this, getString(R.string.mini_month), Toast.LENGTH_SHORT).show();
			}
		});
		
		today = new ActionItem();
		today.setTitle(getString(R.string.today));
		today.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				qa.dismiss();
				Toast.makeText(NavbarActivity.this, getString(R.string.today), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	protected void initMonthView() {
		
//		selectedDate.setTimeInMillis(currentDate.getTimeInMillis());
//		setMonthViewTitle(currentDate);
//		
//		ImageView prevButton = (ImageView) this.findViewById(R.id.prev_month_button);
//		ImageView nextButton = (ImageView) this.findViewById(R.id.next_month_button);
//		
//		prevButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				selectedDate.add(Calendar.MONTH, -1);
//				setMonthViewTitle(selectedDate);
////				TODO get events
////				TODO update calendar view
//				
//			}
//		});
//		nextButton.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				selectedDate.add(Calendar.MONTH, 1);
//				setMonthViewTitle(selectedDate);
////				TODO get events
////				TODO update calendar view
//			}
//		});
//		
//		CalendarViewRewrite calendarView = (CalendarViewRewrite)this.findViewById(R.id.calendar_view_rewrite);
		
	}

	

	public void onResume() {
		super.onResume();
		RadioButton radioButton;
		radioButton = (RadioButton) findViewById(R.id.btnCalendar);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnCalendars);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnContacts);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnEvents);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		radioButton = (RadioButton) findViewById(R.id.btnNewevent);
		radioButton.setChecked(false);
		radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
		
		
		// settings month view
		prefs = new Prefs(this);
//		TODO issiaiskinti kaip ten su tais accountais
		String defaultCalendarView = prefs.getValue(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, "m");
		
//		switchToView();
		if(defaultCalendarView.equals("d")){
			calendarContainer.removeAllViews();
			mInflater.inflate(R.layout.calendar_day, calendarContainer);
			new CalendarDay(NavbarActivity.this);
		}else{
//			qa.dismiss();
//			
			mInflater.inflate(R.layout.calendar_month, calendarContainer);
			new CalendarMonth(NavbarActivity.this);
//			calendarContainer.removeAllViews();
//			mInflater.inflate(R.layout.calendar_day_view_container, calendarContainer);

		}
	}
	
	
//	TODO UZBAIGS JUSTAS M JEI KAS NORS PRIMINS ;)
	private void switchToView() {
		switch (viewState) {
		case DAY:
			
			break;

		default:
			String defaultCalendarView = prefs.getValue(AccountProvider.AMetaData.AccountMetaData.SETTING_DEFAULT_VIEW, "m");
			
//			TODO set default state accordingly to what is got
			if (defaultCalendarView.equalsIgnoreCase("m")) viewState = ViewState.MONTH;
			else viewState = ViewState.MONTH;
			switchToView();
			break;
		}
		
	}



	class GetAllEventsTask extends AsyncTask<Void, ArrayList<Item>, ArrayList<Item>>{

		@Override
		protected ArrayList<Item> doInBackground(Void... arg0) {
			ArrayList<Item> items = new ArrayList<Item>();
			ArrayList<Event> events = AgendaUtils.getActualEvents(NavbarActivity.this, dm.getEventsFromLocalDb());
			
			String time = "1970-01-01";
			
			for(int i=0, l=events.size(); i<l; i++){
				final Event event = events.get(i);
				
				final String newtime = Utils.formatDateTime(event.my_time_start, Utils.date_format, "EEE, dd MMMM yyyy");
				if(!time.equals(newtime)){
					time = newtime;
					items.add(new SectionItem(time));
				}
				
				items.add(new EntryItem(event));
			}
			
			return items;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Item> items) {
			calendarContainer.removeAllViews();
			View view = mInflater.inflate(R.layout.calendar_all, calendarContainer);
			ListView listView = (ListView) view.findViewById(R.id.listView);
			entryAdapter = new EntryAdapter(NavbarActivity.this, items);
			listView.setAdapter(entryAdapter);
	        
			searchView = (EditText) view.findViewById(R.id.search);
			searchView.addTextChangedListener(filterTextWatcher);
			super.onPostExecute(items);
		}
		
	}
	
	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s !=null && entryAdapter != null){
				entryAdapter.getFilter().filter(s);
			}
		}

	};
	
	private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				switch (buttonView.getId()) {
				case R.id.btnCalendar:
					qa = new QuickAction(buttonView);
					qa.addActionItem(list_search);
					qa.addActionItem(go_date);
					qa.addActionItem(year);
					qa.addActionItem(month);
					qa.addActionItem(week);
					qa.addActionItem(day);
					qa.addActionItem(agenda);
					qa.addActionItem(mini_month);
					qa.addActionItem(today);
					
					
					qa.show();
					buttonView.setChecked(false);
					break;
				case R.id.btnCalendars:
					startActivity(new Intent(NavbarActivity.this, CalendarsActivity.class));
					break;
				case R.id.btnContacts:
					startActivity(new Intent(NavbarActivity.this, ContactsActivity.class));
					break;
				case R.id.btnEvents:
					showInvites = true; 
					startActivity(new Intent(NavbarActivity.this, EventsActivity.class));
					break;
				case R.id.btnNewevent:
					startActivity(new Intent(NavbarActivity.this, NewEventActivity.class));
					break;
				}
			}
		}
	};
	
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog = new ProgressDialog(NavbarActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getString(R.string.loading_data));
            progressDialog.setCancelable(false);
            return progressDialog;
        default:
            return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog.setProgress(0);
            progressThread = new ProgressThread(handler);
            progressThread.start();
        }
    }
	
	// Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            progressDialog.setProgress(total);
            
            switch(total){
            case 20:
            	progressDialog.setMessage(getString(R.string.loading_accaunt));
            	break;
            case 40:
            	progressDialog.setMessage(getString(R.string.loading_contacts));
            	break;
            case 60:
            	progressDialog.setMessage(getString(R.string.loading_groups));
            	break;
            case 80:
            	progressDialog.setMessage(getString(R.string.loading_events));
            	break;
            case 100:
            	dismissDialog(PROGRESS_DIALOG);
                progressThread.setState(ProgressThread.STATE_DONE);
            	break;
            }
        }
    };

    /** Nested class that performs progress calculations (counting) */
    private class ProgressThread extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        
        final static int STATE_RUNNING = 1;
        
        @SuppressWarnings("unused")
		int mState;
        int total;
       
        ProgressThread(Handler h) {
            mHandler = h;
        }
       
        public void run() {
            mState = STATE_RUNNING;   
            total = 0;
            
            // Delete old data
            getContentResolver().delete(AccountProvider.AMetaData.AccountMetaData.CONTENT_URI, "", null);
            getContentResolver().delete(ContactsProvider.CMetaData.ContactsMetaData.CONTENT_URI, "", null);
            getContentResolver().delete(ContactsProvider.CMetaData.GroupsMetaData.CONTENT_URI, "", null);
            getContentResolver().delete(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, "", null);
            getContentResolver().getType(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI);
            Data.clearData();
            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            total = 20;
            Message msg = mHandler.obtainMessage();
            msg.arg1 = total;
            mHandler.sendMessage(msg);
            
            // Load account
            dm.getAccountFromRemoteDb();
            total = 40;
            msg = mHandler.obtainMessage();
            msg.arg1 = total;
            mHandler.sendMessage(msg);
            
            // Load contacts
            dm.getContactsFromRemoteDb(null);
            total = 60;
            msg = mHandler.obtainMessage();
            msg.arg1 = total;
            mHandler.sendMessage(msg);
            
            // Load groups
            dm.getGroupsFromRemoteDb();
            total = 80;
            msg = mHandler.obtainMessage();
            msg.arg1 = total;
            mHandler.sendMessage(msg);
            
            // Load events
            dm.getEventsFromRemoteDb("");
            total = 100;
            msg = mHandler.obtainMessage();
            msg.arg1 = total;
            mHandler.sendMessage(msg);

        }
        
        class initContactsScreen extends AsyncTask<Void, ArrayList<Contact>, ArrayList<Item>>{

			@Override
			protected ArrayList<Item> doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}

        	
        }
        
        class initEventsScreen extends AsyncTask<Void, ArrayList<Event>, ArrayList<Item>>{

			@Override
			protected ArrayList<Item> doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}

        	
        }
        
        /* sets the current state for the thread,
         * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}