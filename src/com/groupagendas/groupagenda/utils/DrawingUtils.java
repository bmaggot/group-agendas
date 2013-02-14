package com.groupagendas.groupagenda.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.interfaces.Colored;

public class DrawingUtils {
	public static final float screenDensity = Resources.getSystem().getDisplayMetrics().density;

	/**
	 * method returns pixel value of given dp dimension for current device
	 * @author justinas.marcinka@gmail.com
	 * @param DP dimension in DP
	 * @return dimension in pixels to particular device
	 */
	public static int convertDPtoPX(int DP) {
		return Math.round(DP * screenDensity);
	}

	/**
	 * Draws a circle based on given color.
	 * 
	 * It is accessed via other methods which set various size parameters.
	 * 
	 * @author meska.lt@gmail.com
	 * @param context - method context.
	 * @param widthPX - rectangle width.
	 * @param heightPX - rectangle height.
	 * @param roundRadius - corners round radius for round rectangle.
	 * @param color - string that represents color in RGB e.g. FFFFFF
	 * @param shadow  - indicates whether to draw shadow for this rectangle.
	 * @return Bitmap that represents this rectangle.
	 */
	public static Bitmap getCircleBitmap(Context context, int widthPX, int heightPX, String color, boolean shadow) {
		int shadowRadius = 1;
		int shadowOffset = Math.round(heightPX * 0.05f); 
	
		Bitmap bmp = Bitmap.createBitmap(widthPX + shadowOffset, heightPX + shadowOffset,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		Paint p = new Paint();
	
		p.setColor(Color.parseColor("#" + color));
		
		if (shadow){
			p.setShadowLayer(shadowRadius, shadowOffset, shadowOffset, context.getResources().getColor(R.color.darker_gray));
		}
	//	c.drawRoundRect(rect, roundRadius, roundRadius, p);
	//	p.setColor(context.getResources().getColor(R.color.darker_gray));
		p.setStrokeWidth(0);
		p.setStyle(Paint.Style.FILL_AND_STROKE);
		p.setAntiAlias(true);
		p.setShadowLayer(0, 0, 0, 0);
		
		c.drawCircle(widthPX/2, heightPX/2, heightPX/2, p);
		
		return bmp;
	}
	
	/**
	 * Draws an round rectangle based on event color. It is accessed via other methods which set various size parameters.
	 * @author justinas.marcinka@gmail.com
	 * @param context - method context.
	 * @param widthPX - rectangle width.
	 * @param heightPX - rectangle height.
	 * @param roundRadius - corners round radius for round rectangle.
	 * @param color - string that represents color in RGB e.g. FFFFFF
	 * @param shadow  - indicates whether to draw shadow for this rectangle.
	 * @return Bitmap that represents this rectangle.
	 */
	private static Bitmap getRoundRectBitmap(Context context, int widthPX, int heightPX, int roundRadius, String color, boolean shadow){
		
		int shadowRadius = 1;
		int shadowOffset = Math.round(heightPX * 0.05f); 
		final RectF rect = new RectF();
		

		Bitmap bmp = Bitmap.createBitmap(widthPX + shadowOffset, heightPX + shadowOffset,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		Paint p = new Paint();
	
		p.setColor(Color.parseColor("#" + color));
		
		if (shadow){
			p.setShadowLayer(shadowRadius, shadowOffset, shadowOffset, context.getResources().getColor(R.color.darker_gray));
		}
		rect.set(0, 0, widthPX, heightPX);
		c.drawRoundRect(rect, roundRadius,
				roundRadius, p);
		
		p.setColor(context.getResources().getColor(R.color.darker_gray));
		p.setStrokeWidth(0);
		p.setStyle(Paint.Style.STROKE);
		p.setAntiAlias(true);
		p.setShadowLayer(0, 0, 0, 0);
//		rect.set(screenDensity * 1, screenDensity * 1, widthPX, heightPX);
		c.drawRoundRect(rect, roundRadius,
				roundRadius, p);

		return bmp;
	}
	
	/**
	 * Draws a round rectangle based on Colored object color. 
	 * @param context - method context.
	 * @param sizeDP - rectangle size. Rectangle height is same, and width is half of specified size.
	 * @param event - event, for which this rectangle is drawn.  
	 * @param shadow  - indicates whether to draw shadow for this rectangle
	 * @return Bitmap that represents this rectangle.
	 */
	public static Bitmap getColoredRoundRectangle (Context context, int sizeDP, Colored coloredObject, boolean shadow){
		return getColoredRoundRectangle(context, sizeDP, coloredObject.getColor(), shadow);
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * Draws a round rectangle based on given color. 
	 * @param context - method context.
	 * @param sizeDP - rectangle size. Rectangle height is same, and width is half of specified size.
	 * @param event - event, for which this rectangle is drawn.  
	 * @param shadow  - indicates whether to draw shadow for this rectangle
	 * @return Bitmap that represents this rectangle.
	 * @since 2012-10-10
	 */
	public static Bitmap getColoredRoundRectangle (Context context, int sizeDP, String color, boolean shadow){
		int heightPX = convertDPtoPX(sizeDP);
		int widthPX = heightPX / 2;
		int roundRadius = Math.round(heightPX * 0.2f);
		return getRoundRectBitmap(context, widthPX, heightPX, roundRadius, color, shadow);
	}
	
	public static Bitmap getColoredRoundSquare (Context context, int sizeDP, int roundY, String color, boolean shadow){
		int heightPX = convertDPtoPX(sizeDP);
		int widthPX = heightPX;
		int roundRadius = convertDPtoPX(roundY);
		return getRoundRectBitmap(context, widthPX, heightPX, roundRadius, color, shadow);
	}
}