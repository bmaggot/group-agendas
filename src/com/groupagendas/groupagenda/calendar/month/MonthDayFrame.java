package com.groupagendas.groupagenda.calendar.month;

import java.util.ArrayList;

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

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.calendar.MonthCellState;
import com.groupagendas.groupagenda.events.Event;

public class MonthDayFrame extends RelativeLayout {
	

	private static final int BUBBLE_WIDTH_DP = 4;
	private static final int BUBBLE_MARGIN_DP = 1;
	private final int BUBBLE_WIDTH_PX;
	private final int BUBBLE_MARGIN_PX;
	TextView dayTitle;
	public boolean hasBubbles =  false;
	private LinearLayout allBubblesContainer;
	private MonthCellState state = MonthCellState.DEFAULT;
	
	public MonthDayFrame(Context context) {
		this(context, null);
	}
	public MonthDayFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		BUBBLE_WIDTH_PX = Math.round(getResources().getDisplayMetrics().density * BUBBLE_WIDTH_DP);
		BUBBLE_MARGIN_PX = Math.round(getResources().getDisplayMetrics().density * BUBBLE_MARGIN_DP);
	}
	
	public void setDayTitle (String title){
		if(dayTitle == null) dayTitle = (TextView) findViewById(R.id.month_day_title);
		dayTitle.setText(title);
	}
	
	public int getDayNo() {
		if (dayTitle == null)
			return -1;
		
		return Integer.parseInt(dayTitle.getText().toString());
	}
	
	public void DrawColourBubbles (ArrayList<Event> eventColorsArray, int frameWidth){
		
		allBubblesContainer = (LinearLayout) findViewById(R.id.month_bubble_megacontainer);
		allBubblesContainer.removeAllViews();
		if (eventColorsArray == null || eventColorsArray.isEmpty()) {
//			Log.d("MDF", "No events, no bubbles on day " + dayTitle.getText() + " ... " + eventColorsArray);
			return;
		}
		
		LinearLayout line =  createBubbleContainerLine();
		allBubblesContainer.addView(line);
		
		int maxChildren = frameWidth / (BUBBLE_WIDTH_PX + BUBBLE_MARGIN_PX); 
		
//		int lines = 1;
		for (Event event : eventColorsArray) {
//			if (event.getStatus() != Invited.PENDING || event.is_owner()) {
				while (!addBubbleToLine(line, maxChildren, event)){
					line = createBubbleContainerLine();
					addBubbleToLine(line, maxChildren, event);
					allBubblesContainer.addView(line);
//					lines++;
				}
//			}
		}
//		Log.d("MDF", "Drawing " + eventColorsArray.size() + " bubbles on " + lines + " lines. Day " + dayTitle.getText());

	}
	
	private LinearLayout createBubbleContainerLine() {
		LinearLayout layout = new LinearLayout(getContext());
		layout.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		return layout;
	}
	public boolean addBubbleToLine(LinearLayout line, int maxChildren, Event event){
		if (line.getChildCount() >= maxChildren) return false;
		line.addView(drawCircle(BUBBLE_WIDTH_PX, event));
		// Log.w("MDF", "Added circle to line " + line);
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
		p.setColor(Color.parseColor("#" + event.getDisplayColor()));
		c.drawCircle(size/2, size/2, size/2, p);
		
		ImageView img = new ImageView(getContext());
		img.setBackgroundDrawable(new BitmapDrawable(bmp));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		params.setMargins(BUBBLE_MARGIN_PX, BUBBLE_MARGIN_PX, 0, 0);
		img.setLayoutParams(params);
		
		return img;
		
	}
	
//	Must be called after inflate, because dayTitle cannot be null
	private void refresh(){
		switch (state){
		case SELECTED:
			this.setBackgroundResource(R.drawable.calendar_month_day_selected);
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_selectedday);
			dayTitle.setShadowLayer(1, 0, 1, Color.BLACK);
			break;
		case TODAY:
			this.setBackgroundResource(R.drawable.calendar_month_day_today);
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_today);
			dayTitle.setShadowLayer(1, 0, 1, Color.BLACK);
			break;
		case OTHER_MONTH:
			this.setBackgroundResource(R.drawable.calendar_month_day_inactive);	
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_othermonth);
			dayTitle.setShadowLayer(0, 0, 0, Color.BLACK);
			break;
		default:
			this.setBackgroundResource(R.drawable.calendar_month_day_inactive);	
			dayTitle.setTextAppearance(getContext(), R.style.monthview_daynumber_thismonth);
			dayTitle.setShadowLayer(1, 0, 1, Color.WHITE);
			break;		
		}		
	}
	/**
	 * @author
	 */
//	public void setSelected(boolean bool) {
//		
//			this.selected = bool;
//	}
//	public void setToday(boolean bool){
//	
//			this.today = bool;
//	}
//	public void setOtherMonth(boolean bool) {
//	
//		this.otherMonth = bool;
//		if (bool) {
//			selected = false;
//			today = false;
//		}
//	}
	
	
	
	public void setState(MonthCellState state) {
		this.state = state;
		refresh();
	}
	public boolean isToday() {
		return state == MonthCellState.TODAY;
	}
	@Override
	public boolean isSelected() {
		return state == MonthCellState.SELECTED;
	}
	public boolean isOtherMonth() {
		return state == MonthCellState.OTHER_MONTH;
	}
	public boolean hasBubbles() {
		if (allBubblesContainer == null) return false;
		return allBubblesContainer.getChildCount() > 0; 
	}
	
	

}
