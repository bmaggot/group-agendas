package com.groupagendas.groupagenda.calendar.day;

import com.groupagendas.groupagenda.R;

import android.app.Activity;
import android.content.Context;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HourEventView extends RelativeLayout {
	private int preferedHeight;
	private int preferedWidth;
	private Event event;
	private TextView title;
	private TextView timeText;
	private ImageView icon;
	
	
	public HourEventView(Context context){
		super (context);
		LayoutInflater.from(context).inflate(R.layout.calendar_dayview_hourevent_entry, this);
	
	}

	public HourEventView(Context context, AttributeSet attrs) {
		super(context, attrs);
	
	
		// TODO Auto-generated constructor stub
	}
//@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
//	System.out.println("ON MEASURE " + widthMeasureSpec + " " + heightMeasureSpec);
//		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//		
//		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//		
//		int chosenWidth = chooseWidth(widthMode, widthSize);
//		int chosenHeight = chooseHeight(heightMode, heightSize);
//		
//		
//		
//		setMeasuredDimension(chosenWidth, chosenHeight);
//	}

//	private int chooseWidth(int mode, int size) {
//		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
//			return size;
//		} else { // (mode == MeasureSpec.UNSPECIFIED)
//			return getPreferedWidth();
//		}
//
//	}
//	private int chooseHeight(int mode, int size) {
//		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
//			return size;
//		} else { // (mode == MeasureSpec.UNSPECIFIED)
//			return getPreferedHeight();
//		}
//
//	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		System.out.println("ASDASDASD");
//		((Activity) getContext()).getLayoutInflater().inflate(R.layout.calendar_day, this);
		

	}

//	public int getPreferedHeight() {
//		return preferedHeight;
//	}
//
//	public void setPreferedHeight(int preferedHeight) {
//		this.preferedHeight = preferedHeight;
//	}
//
//	public int getPreferedWidth() {
//		return preferedWidth;
//	}
//
//	public void setPreferedWidth(int preferedWidth) {
//		this.preferedWidth = preferedWidth;
//	}
	
}
