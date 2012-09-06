package com.groupagendas.groupagenda.calendar;
/**
 * @author justinas.marcinka@gmail.com
 */

import java.util.Calendar;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.CalendarSettings;
import com.groupagendas.groupagenda.data.DataManagement;

import android.app.Activity;
import android.content.Context;
import android.graphics.Path.FillType;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class AbstractCalendarView extends LinearLayout {
	
	protected GestureDetector swipeGestureDetector;
	
	protected Calendar selectedDate;
	
	ImageButton prevButton;
	ImageButton nextButton;
	Rect prevButtonBounds;
	Rect nextButtonBounds;
	protected TouchDelegate prevButtonDelegate;
	protected TouchDelegate nextButtonDelegate;
	TextView topPanelTitle;
	private FrameLayout topPanelBottomLineFrame;
	
	protected final int DISPLAY_WIDTH = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getWidth();
	protected final float densityFactor = getResources().getDisplayMetrics().density;
	protected final int VIEW_WIDTH = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getWidth();

	protected final int VIEW_HEIGHT = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getHeight()
			- Math.round((getResources().getInteger(R.integer.CALENDAR_TOPBAR_HEIGHT) + getResources().getInteger(R.integer.NAVBAR_HEIGHT)) * densityFactor);
	protected LayoutInflater mInflater;
	
	
	protected boolean am_pmEnabled;
	protected String[] HourNames;
	protected String[] WeekDayNamesShort;
	protected String[] WeekDayNames;
	protected String[] MonthNames;
	
	
	protected abstract void setTopPanel(); 	//Sets up top panel title text in every view differently

	public abstract void goPrev();					//switch to prev View	
	public abstract void goNext();				//switch to next View	
	public abstract void setupView();			//setup specific part of view
	protected abstract void updateEventLists();
	protected abstract void setupSelectedDate(Calendar initializationDate); //method to set up date that will be used for calendar
	public abstract Calendar getDateToResume(); //returns date that should be saved in Activity instance state
	

	
	/**
	 * @return puts Top Panel Bottom Line view into frame. Child classes which need it, must override this method;
	 **/	 
	protected void instantiateTopPanelBottomLine(){
		return;
	}	
	
	
	public AbstractCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = (LayoutInflater)((Activity)context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		am_pmEnabled = CalendarSettings.isUsing_AM_PM();
		if(am_pmEnabled){
			HourNames = getResources().getStringArray(R.array.hour_names_am_pm);
		}
		else{
			HourNames = getResources().getStringArray(R.array.hour_names);
		}
//		TODO Set calendar_top_bar height in code
//		RelativeLayout topPanel = (RelativeLayout) this.findViewById(R.layout.calendar_top_bar);
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, Math.round(getResources().getInteger(R.integer.CALENDAR_TOPBAR_HEIGHT)* densityFactor));
//		topPanel.setLayoutParams(params);
//		topP
	}

	public AbstractCalendarView(Context context) {
		this(context, null);
		
	}
	
	public TextView getTopPanelTitle() {
		return topPanelTitle;
	}
	
	
	public FrameLayout getTopPanelBottomLine() {
		return topPanelBottomLineFrame;
	}

	public void init(Calendar initializationDate){
		setupSelectedDate(initializationDate);
		setupTopPanel();
		setUpSwipeGestureListener();
		setupView();
		
	}
	

	private final void setupTopPanel() {
		
		prevButton = (ImageButton) findViewById(R.id.prevView);
		nextButton = (ImageButton) findViewById(R.id.nextView);
		topPanelTitle = (TextView) findViewById(R.id.top_panel_title);
		topPanelBottomLineFrame = (FrameLayout) findViewById(R.id.top_bar_bottom_line_frame);
		instantiateTopPanelBottomLine();
		
		prevButtonBounds = new Rect();
		nextButtonBounds = new Rect();
		
		
		
		setTopPanel();
		
		prevButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goPrev();
			}
		});
		
		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goNext();
			}
		});
		
		setupDelegates();
		
	}
	

	private void setupDelegates() {
		int[] tmpCoords = new int[2];
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		View calNavbar = (View) findViewById(R.id.calendar_navbar);
		calNavbar.getLocationOnScreen(tmpCoords);
		prevButton.getHitRect(prevButtonBounds);
		prevButtonBounds.right = tmpCoords[0]+50;
		prevButtonBounds.left = tmpCoords[0];
		prevButtonBounds.top = tmpCoords[1];
		prevButtonBounds.bottom = tmpCoords[1]+50;
		prevButtonDelegate = new TouchDelegate(prevButtonBounds, prevButton);
		
		nextButton.getHitRect(nextButtonBounds);
		nextButtonBounds.right = tmpCoords[0]+screenWidth;
		nextButtonBounds.left = tmpCoords[0]+screenWidth-50;
		nextButtonBounds.top = tmpCoords[1];
		nextButtonBounds.bottom = tmpCoords[1]+50;		
		nextButtonDelegate = new TouchDelegate(nextButtonBounds, nextButton);

		if (View.class.isInstance(calNavbar)) {
			calNavbar.setTouchDelegate(prevButtonDelegate);
			calNavbar.setTouchDelegate(nextButtonDelegate);
		}

	}
	
	
	protected void setUpSwipeGestureListener(){
		if (swipeGestureDetector == null) {
			swipeGestureDetector = new GestureDetector(
					new SwipeOnGestureListener(this));
			this.setOnTouchListener(createListener(swipeGestureDetector));
		}
	}
	
	protected OnTouchListener createListener(final GestureDetector swipeGestureDetector) {
		return new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
					if (swipeGestureDetector.onTouchEvent(event)) {
					     return false;
					    } else {
					     return true;
					    }
			}
		};
	}

	public GestureDetector getSwipeGestureDetector() {
		return swipeGestureDetector;
	}
	
}
