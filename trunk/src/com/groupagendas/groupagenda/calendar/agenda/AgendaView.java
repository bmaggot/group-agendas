package com.groupagendas.groupagenda.calendar.agenda;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.month.MonthDayFrame;
import com.groupagendas.groupagenda.calendar.month.MonthView;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.Data;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventsProvider;
import com.groupagendas.groupagenda.utils.Utils;

public class AgendaView extends AbstractCalendarView {
	
	private static final int FRAMES_PER_ROW = 2;
	private Calendar shownDate;
	private static final int TABLE_ROWS_COUNT = 3;
	private static final int SHOWN_DAYS_COUNT = 7;
	
	ArrayList<AgendaFrame> daysList = new ArrayList<AgendaFrame>();
	
	private TableLayout agendaTable;

	public AgendaView(Context context) {
		this(context, null);
	}

	public AgendaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names_short);
	}

	@Override
	protected void setTopPanel() {
		String title = getResources().getString(R.string.week);
		title += " ";
		title += shownDate.get(Calendar.WEEK_OF_YEAR);
		this.getTopPanelTitle().setText(title);

	}

	@Override
	public void goPrev() {
		shownDate.add(Calendar.DATE, -1 * SHOWN_DAYS_COUNT);
		setTopPanel();
		setDaysTitles();
		updateEventLists();

	}

	@Override
	public void goNext() {
		shownDate.add(Calendar.DATE, SHOWN_DAYS_COUNT);
		setTopPanel();
		setDaysTitles();
		updateEventLists();

	}

	@Override
	public void setupView() {
		agendaTable = (TableLayout) findViewById(R.id.agenda_table);
		agendaTable.setOnTouchListener(createListener(swipeGestureDetector));
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
		        ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);
		
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
		        VIEW_WIDTH / FRAMES_PER_ROW,
		        VIEW_HEIGHT/TABLE_ROWS_COUNT,
		        1.0f);
		

//		Adding rows
		TableRow row;
		for (int i = 0; i < TABLE_ROWS_COUNT - 1; i++){
			row = (TableRow) mInflater.inflate(R.layout.calendar_agenda_row, null);
			addWorkingDay(row, cellLp);
			addWorkingDay(row, cellLp);	
			agendaTable.addView(row, rowLp);
		}
//		Add last row
		row = (TableRow) mInflater.inflate(R.layout.calendar_agenda_row, null);
		addWorkingDay(row, cellLp);
		addWeekend(row, cellLp);
		agendaTable.addView(row, rowLp);
		
		setDaysTitles();
		updateEventLists();

	}

	private void setDaysTitles() {
		int day = 0;
		for (AgendaFrame frame : daysList){
			TextView dayTitle = (TextView) frame.getDayContainer().findViewById(R.id.agenda_day_title);
			Calendar tmp = (Calendar) shownDate.clone();
			tmp.add(Calendar.DATE, day);
			day++;
			
			String title = WeekDayNames[tmp.get(Calendar.DAY_OF_WEEK) -1];
			title += ", ";
			title += MonthNames[tmp.get(Calendar.MONTH)];
			title += " ";
			title += tmp.get(Calendar.DATE);
			title += ", ";
			title += tmp.get(Calendar.YEAR);
			
			dayTitle.setText(title);
			if (Utils.isToday(tmp)){
				dayTitle.setBackgroundColor(getResources().getColor(R.color.darker_gray));
			} else{
				dayTitle.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
			}
		}
		
	}

	private void addWeekend(TableRow row,
			android.widget.TableRow.LayoutParams cellLp) {
		LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);
		LinearLayout weekEndFrame = new LinearLayout(getContext());
		weekEndFrame.setOrientation(VERTICAL);
		
		LinearLayout saturday = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_day_container, null);
		weekEndFrame.addView(saturday, params);
		daysList.add(new AgendaFrame(saturday, getContext()));
		
		LinearLayout sunday = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_day_container, null);
		weekEndFrame.addView(sunday, params);
		daysList.add(new AgendaFrame(sunday, getContext()));
		
		row.addView(weekEndFrame, cellLp);	
	}

	private void addWorkingDay(TableRow row, android.widget.TableRow.LayoutParams cellLp) {
		LinearLayout workingDayFrame = (LinearLayout) mInflater.inflate(R.layout.calendar_agenda_day_container, null);
		row.addView(workingDayFrame, cellLp);
		daysList.add(new AgendaFrame(workingDayFrame, getContext()));
		
	}

	@Override
	protected void updateEventLists() {
		new UpdateEventsInfoTask().execute();
		
	}

	@Override
	public Calendar getDateToResume() {
		return shownDate;
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDate = initializationDate;
		this.shownDate = (Calendar)selectedDate.clone();
		Utils.setCalendarToFirstDayOfWeek(this.shownDate);
		
	}
	
	private class UpdateEventsInfoTask extends AsyncTask<Void, Integer, Void> {
		private Context context = AgendaView.this.getContext();
		private DataManagement dm = DataManagement.getInstance(context);

		/**
		 * @author justinas.marcinka@gmail.com Returns event projection in: id,
		 *         color, icon, title, start and end calendars. Other fields are
		 *         not initialized
		 * @param date
		 * @return
		 */
		private ArrayList<Event> getEventProjectionsForDisplay(Calendar dateStart) {
			ArrayList<Event> list = new ArrayList<Event>();
			String[] projection = {
					EventsProvider.EMetaData.EventsMetaData.E_ID,
					EventsProvider.EMetaData.EventsMetaData.COLOR,
					EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS,
					EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS,
					EventsProvider.EMetaData.EventsMetaData.ICON,
					EventsProvider.EMetaData.EventsMetaData.TITLE};
			
			Cursor result = dm.createEventProjectionByDateFromLocalDb(
					projection, dateStart, 7,
					DataManagement.TM_EVENTS_FROM_GIVEN_DATE, null);
			if (result.moveToFirst()) {
				while (!result.isAfterLast()) {
					Event eventProjection = new Event();
					eventProjection
							.setEvent_id(result.getInt(result
									.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.E_ID)));
					eventProjection
							.setTitle(result.getString(result
									.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TITLE)));
					eventProjection
							.setIcon(result.getString(result
									.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.ICON)));
					eventProjection
							.setColor(result.getString(result
									.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.COLOR)));
					String user_timezone = CalendarSettings.getTimeZone();
					long timeinMillis = result
							.getLong(result
									.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_START_UTC_MILLISECONDS));
					eventProjection.setStartCalendar(Utils.createCalendar(
							timeinMillis, user_timezone));
					timeinMillis = result
							.getLong(result
									.getColumnIndexOrThrow(EventsProvider.EMetaData.EventsMetaData.TIME_END_UTC_MILLISECONDS));
					eventProjection.setEndCalendar(Utils.createCalendar(
							timeinMillis, user_timezone));
					list.add(eventProjection);
					result.moveToNext();
				}
			}
			result.close();
			return list;

		}

		@Override
		protected Void doInBackground(Void... params) {
			sortedEvents = dm
					.sortEvents(getEventProjectionsForDisplay(shownDate));
			return null;
		}

		protected void onPostExecute(Void result) {
			Calendar tmp = (Calendar) shownDate.clone();
			for (AgendaFrame frame : daysList){	
				frame.setEventList(Utils.getEventsFromTreemap(tmp, sortedEvents));
				tmp.add(Calendar.DATE, 1);
				frame.UpdateList();
			}

		}

	}
	

}
