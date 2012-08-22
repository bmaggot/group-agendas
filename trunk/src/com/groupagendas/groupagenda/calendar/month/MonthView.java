package com.groupagendas.groupagenda.calendar.month;


import java.util.ArrayList;
import java.util.Calendar;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.AbstractCalendarView;
import com.groupagendas.groupagenda.calendar.agenda.AgendaFrame;
import com.groupagendas.groupagenda.utils.Utils;

public class MonthView extends AbstractCalendarView {
	
	private final int TABLE_ROW_HEIGHT = Math.round(50 * densityFactor);
	private Calendar selectedDate;
	private Calendar firstShownDate;
	ArrayList<TextView> daysList = new ArrayList<TextView>();
	private TableLayout monthTable;
	private ListView eventsList;


    public MonthView(Context context) {
        this(context, null);

    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WeekDayNames = getResources().getStringArray(R.array.week_days_short);
		MonthNames = getResources().getStringArray(R.array.month_names);
    }

 
   

	@Override
	protected void setTopPanel() {
		String title = MonthNames[selectedDate.get(Calendar.MONTH) - 1];
		title += " ";
		title += selectedDate.get(Calendar.YEAR);
		this.getTopPanelTitle().setText(title);
		
	}

	@Override
	public void goPrev() {
		int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedDate.add(Calendar.MONTH, -1);
		updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedDate);
		}
		setDaysTitles();
		updateEventLists();
		
	}

	@Override
	public void goNext() {
		int LastMonthWeeksCount = selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH);
		selectedDate.add(Calendar.MONTH, 1);
		updateShownDate();
		setTopPanel();
		if(LastMonthWeeksCount != selectedDate.getActualMaximum(Calendar.WEEK_OF_MONTH)){
			paintTable(selectedDate);
		}
		
		setDaysTitles();
		updateEventLists();
		
	}

	@Override
	public void setupView() {
		monthTable = (TableLayout) findViewById(R.id.month_table);
		paintTable(selectedDate);
		setDaysTitles();
		updateEventLists();
		
	}

	@Override
	protected void updateEventLists() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setupSelectedDate(Calendar initializationDate) {
		this.selectedDate = initializationDate;
		firstShownDate = updateShownDate();
		
	}

	@Override
	public Calendar getDateToResume() {
		return selectedDate;
	}
	private void setDaysTitles() {
		Calendar tmp = (Calendar) firstShownDate.clone();
		int firstDayOfWeek = tmp.getFirstDayOfWeek();
		
		for (TextView frame : daysList){
			String title = "" + tmp.get(Calendar.DATE);
			frame.setText(title);
			
			if (Utils.isToday(tmp)){
				frame.setBackgroundColor(getResources().getColor(R.color.darker_gray));
			} else{
				frame.setBackgroundColor(getResources().getColor(R.color.lighter_gray));
			}
//			TODO draw week numbers
			tmp.add(Calendar.DATE, 1);
		}
		
	}

	private void paintTable(Calendar date) {
		monthTable.removeAllViews();
		int FRAMES_PER_ROW = date.getMaximum(Calendar.DAY_OF_WEEK);
		int TABLE_ROWS_COUNT = date.getActualMaximum(Calendar.WEEK_OF_MONTH);
		
		
//	TODO	add GESTURE LISTENER
		TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
		        ViewGroup.LayoutParams.FILL_PARENT,
		        ViewGroup.LayoutParams.FILL_PARENT,
		        1.0f);		
		
		TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
		        VIEW_WIDTH / FRAMES_PER_ROW,
		        TABLE_ROW_HEIGHT, //TODO leave some space for list view
		        1.0f);		

//		Adding rows
		TableRow row;
		for (int i = 0; i < TABLE_ROWS_COUNT; i++){
			row = (TableRow) mInflater.inflate(R.layout.calendar_mm_row, null);
			for (int j = 0; j < FRAMES_PER_ROW; j++)
			addDay(row, cellLp);
			monthTable.addView(row, rowLp);
		}		
		
	}

	private void addDay(TableRow row,
			android.widget.TableRow.LayoutParams cellLp) {
			TextView dayFrame = new TextView(getContext());
			row.addView(dayFrame, cellLp);
			daysList.add(dayFrame);
	}

	private Calendar updateShownDate() {
		Calendar tmp = (Calendar) selectedDate.clone();
		Utils.setCalendarToFirstDayOfMonth(tmp);
		Utils.setCalendarToFirstDayOfWeek(tmp);
		return tmp;
	}
}