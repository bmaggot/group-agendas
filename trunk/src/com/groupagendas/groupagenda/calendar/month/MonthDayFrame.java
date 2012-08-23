package com.groupagendas.groupagenda.calendar.month;

import java.util.ArrayList;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MonthDayFrame extends RelativeLayout {
	

	private static final int BUBBLE_WIDTH_DP = 4;
	private static final int BUBBLE_MARGIN_DP = 1;
	private final int BUBBLE_WIDTH_PX;
	private final int BUBBLE_MARGIN_PX;
	TextView dayTitle;
	private boolean today;
	private boolean selected;
	private boolean otherMonth;
	public boolean hasBubbles =  false;
	private LinearLayout allBubblesContainer;
	
	public MonthDayFrame(Context context) {
		this(context, null);
	}
	public MonthDayFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		selected = false;
		today = false;
		otherMonth = false;
		BUBBLE_WIDTH_PX = Math.round(getResources().getDisplayMetrics().density * BUBBLE_WIDTH_DP);
		BUBBLE_MARGIN_PX = Math.round(getResources().getDisplayMetrics().density * BUBBLE_MARGIN_DP);
	}
	
	public void setDayTitle (String title){
		if(dayTitle == null) dayTitle = (TextView) findViewById(R.id.month_day_title);
		dayTitle.setText(title);
	}
	
	public void DrawColourBubbles (ArrayList<Event> eventColorsArray, int frameWidth){
		allBubblesContainer = (LinearLayout) findViewById(R.id.month_bubble_megacontainer);
		allBubblesContainer.removeAllViews();
		if (eventColorsArray.isEmpty()) return;
		
		LinearLayout line =  createBubbleContainerLine();
		allBubblesContainer.addView(line);
		
		int maxChildren = frameWidth / (BUBBLE_WIDTH_PX + BUBBLE_MARGIN_PX); 
		System.out.println("max vaiku: " + maxChildren);
		
		for (Event event : eventColorsArray){
			while (!addBubbleToLine(line, maxChildren, event)){
				line = createBubbleContainerLine();
				addBubbleToLine(line, maxChildren, event);
				allBubblesContainer.addView(line);
			}
		}

	}
	
	private LinearLayout createBubbleContainerLine() {
		LinearLayout layout = new LinearLayout(getContext());
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		return layout;
	}
	public boolean addBubbleToLine(LinearLayout line, int maxChildren, Event event){
		if (line.getChildCount() >= maxChildren) return false;
		line.addView(drawCircle(BUBBLE_WIDTH_PX, event));
		return true;
	}
	
	/**
	 * 
	 * @param size
	 * @param event
	 * @return created imageView circle or NULL if there is no such color circle png
	 */
	private ImageView drawCircle(int size, Event event){
		
		Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		Paint p = new Paint();
		p.setColor(Color.parseColor("#" + event.getColor()));
		c.drawCircle(size/2, size/2, size/2, p);
		
		ImageView img = new ImageView(getContext());
		img.setBackgroundDrawable(new BitmapDrawable(bmp));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		params.setMargins(BUBBLE_MARGIN_PX, BUBBLE_MARGIN_PX, 0, 0);
		img.setLayoutParams(params);
		
		return img;
		
	}
	
//	Must be called after inflate, because dayTitle cannot be null
	public void refreshStyle(){
		
		if (selected){
			this.setBackgroundResource(R.drawable.calendar_month_day_selected);
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_selectedday);
			return;
		}
		if (today){
			this.setBackgroundResource(R.drawable.calendar_month_day_today);
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_today);
			return;
		}
		
		this.setBackgroundResource(R.drawable.calendar_month_day_inactive);		
		if (otherMonth){
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_othermonth);
		}else dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_thismonth);
		
	}
	/**
	 * @author
	 */
	public void setSelected(boolean bool) {
		
			this.selected = bool;
	}
	public void setToday(boolean bool){
	
			this.today = bool;
	}
	public void setOtherMonth(boolean bool) {
	
		this.otherMonth = bool;
		if (bool) {
			selected = false;
			today = false;
		}
	}
	public boolean isToday() {
		return today;
	}
	public boolean isSelected() {
		return selected;
	}
	public boolean isOtherMonth() {
		return otherMonth;
	}
	public boolean hasBubbles() {
		if (allBubblesContainer == null) return false;
		return allBubblesContainer.getChildCount() > 0; 
	}
	
	

}
