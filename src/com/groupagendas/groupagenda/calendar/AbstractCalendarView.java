package com.groupagendas.groupagenda.calendar;


import com.groupagendas.groupagenda.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class AbstractCalendarView extends LinearLayout {
	protected GestureDetector swipeGestureDetector;
	ImageButton prevButton;
	ImageButton nextButton;
	Rect prevButtonBounds;
	Rect nextButtonBounds;
	protected TouchDelegate prevButtonDelegate;
	protected TouchDelegate nextButtonDelegate;
	TextView topPanelTitle;
	
	protected final float densityFactor = getResources().getDisplayMetrics().density;
	protected LayoutInflater mInflater;
	
	protected abstract void setTopPanelTitle(); 	//Sets up top panel title text in every view differently

	public abstract void goPrev();					//switch to prev View
	
	public abstract void goNext();				//switch to next View
	
	public abstract void setupView();			//setup specific part of view
	
	
	
	
	
	
	public AbstractCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = (LayoutInflater)((Activity)context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public AbstractCalendarView(Context context) {
		this(context, null);
		
	}
	
	public TextView getTopPanelTitle() {
		return topPanelTitle;
	}
	
	public void init(){
		setupTopPanel();
		setUpSwipeGestureListener();
		setupView();
		
	}
	

	protected void setupTopPanel() {
		
		prevButton = (ImageButton) findViewById(R.id.prevView);
		nextButton = (ImageButton) findViewById(R.id.nextView);
		topPanelTitle = (TextView) findViewById(R.id.top_panel_title);
		
		prevButtonBounds = new Rect();
		nextButtonBounds = new Rect();
		
		
		
		setTopPanelTitle();
		
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
		swipeGestureDetector = new GestureDetector(new SwipeOnGestureListener(this));
		
		this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (swipeGestureDetector.onTouchEvent(event)) {
				     return false;
				    } else {
				     return true;
				    }
			}
		});
	}
}
