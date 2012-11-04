package com.groupagendas.groupagenda.calendar.dayandweek;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.groupagendas.groupagenda.calendar.EventView;



public class HourEventRectangle extends EventView {
	
	private int roundRadiusDP = 5; //also is used as padding from left and right
	
	private int topPaddingDP = 1;
	//private int bottomPaddingDP = 2;
	
	private int defaultSpaceDP = 3;
	
	private final int textSizeDP = 10;
	private int iconSizeDP = 13;
	
	private RectF rect;


	
	public HourEventRectangle(Context context, String startTime, String title, String textColor, String displayColor, int iconId) {
		super(context, startTime, title, textColor, displayColor, iconId); //2012-10-24
		this.textColor = Color.parseColor("#" + textColor); //2012-10-24
		this.displayColor = Color.parseColor("#BF" + displayColor); //2012-10-24
        rect = new RectF();
	}
	

	
	@Override
	protected void onDraw(Canvas canvas) {
		float roundRadius = dpToPx(roundRadiusDP);
		float topPadding = dpToPx(topPaddingDP);
		//float bottomPadding = dpToPx(bottomPaddingDP);
		
		float defaultSpace = dpToPx(defaultSpaceDP);
		float x = roundRadius;

        paint.setColor(displayColor);
		rect.set(0, 0, width, height);
		canvas.drawRoundRect(rect, roundRadius, roundRadius, paint);
		//preparing to draw text
		paint.setTextAlign(Align.LEFT);
		paint.setTextSize(dpToPx(textSizeDP));
		
		
		//DRAWING START TIME:
			paint.setAntiAlias(true);
			paint.setColor(textColor);
//			paint.setTypeface(Typeface.SANS_SERIF);
			canvas.drawText(startTime, x,  topPadding - paint.ascent(), paint);
			x += paint.measureText(startTime);
			x += defaultSpace;
			
			boolean drawItem;
	
		
        //DRAWING Icon:
		if (iconDrawable != null){
			int iconSize = (int) dpToPx(iconSizeDP);
			drawItem = x + iconSize <= width - roundRadius;
			if (drawItem) {
				iconDrawable.setBounds((int) x, (int) topPadding, iconSize + (int) x, (int) topPadding + iconSize);
				iconDrawable.draw(canvas);
				x += iconSize; //"x + iconSize <=...h - roundRadius"	 <error(s)_during_the_evaluation>	
				x += defaultSpace;
			}
		}
		
		
		//DRAWING Title:
			paint.setTypeface(Typeface.DEFAULT_BOLD);
			drawItem = x + paint.measureText(title) <= width - roundRadius;
			if (drawItem) {
			paint.setColor(textColor);//2012-10-24
			paint.setTextAlign(Align.RIGHT);
			canvas.drawText(title, width - roundRadius,  topPadding - paint.ascent(), paint);
			paint.setTypeface(Typeface.DEFAULT);
			x += paint.measureText(title);
			}

		}	
	}

