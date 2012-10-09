package com.groupagendas.groupagenda.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;


public class EventView extends View{
	protected String startTime;
	protected String title;
	protected int color;
	protected Drawable iconDrawable;
	
	protected Paint paint;
	protected int width;
	protected int height;
	protected static float density = Resources.getSystem().getDisplayMetrics().density;
	
	
	
	public EventView(Context context, String startTime, String title, String color, int iconId) {
		super(context);
		this.startTime = startTime;
		this.title = title;
		
		this.color = Color.parseColor("#" + color);
		if (iconId != 0)
		iconDrawable = context.getResources().getDrawable(iconId);
		paint = new Paint();
	}
	
	  @Override
	    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	        this.width = w;
	        this.height = h;;
	    }

	    @Override
	    protected void onDraw(Canvas canvas) {
	    	super.onDraw(canvas);
	    }
	    
	    protected float dpToPx(int DP){
	    	return density * DP;
	    }
	    
	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

	    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//	        setMeasuredDimension(measureWidth(widthMeasureSpec),
//	                measureHeight(heightMeasureSpec));
	    }

	    /**
	     * Determines the width of this view
	     * @param measureSpec A measureSpec packed into an int
	     * @return The width of the view, honoring constraints from measureSpec
	     */
	    private int measureWidth(int measureSpec) {
	    	
	    	
	    	
	        int result = measureSpec;
	        int specMode = MeasureSpec.getMode(measureSpec);
	        int specSize = MeasureSpec.getSize(measureSpec);
//
//	        if (specMode == MeasureSpec.EXACTLY) {
//	            // We were told how big to be
//	            result = specSize;
//	        } else {
//	            // Measure the text
//	            result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
//	                    + getPaddingRight();
//	            if (specMode == MeasureSpec.AT_MOST) {
//	                // Respect AT_MOST value if that was what is called for by measureSpec
//	                result = Math.min(result, specSize);
//	            }
//	        }

	        return result;
	    }

	    /**
	     * Determines the height of this view
	     * @param measureSpec A measureSpec packed into an int
	     * @return The height of the view, honoring constraints from measureSpec
	     */
	    private int measureHeight(int measureSpec) {
	    	
	        int result = measureSpec;
	        int specMode = MeasureSpec.getMode(measureSpec);
	        int specSize = MeasureSpec.getSize(measureSpec);

//	        mAscent = (int) mTextPaint.ascent();
//	        if (specMode == MeasureSpec.EXACTLY) {
//	            // We were told how big to be
//	            result = specSize;
//	        } else {
//	            // Measure the text (beware: ascent is a negative number)
//	            result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop()
//	                    + getPaddingBottom();
//	            if (specMode == MeasureSpec.AT_MOST) {
//	                // Respect AT_MOST value if that was what is called for by measureSpec
//	                result = Math.min(result, specSize);
//	            }
//	        }
	        return result;
	    }


}
