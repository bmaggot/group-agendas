package com.groupagendas.groupagenda.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;

public class DrawingUtils {
	

/**
 * method returns pixel value of given dp dimension for current device
 * @author justinas.marcinka@gmail.com
 * @param DP dimension in DP
 * @return dimension in pixels to particular device
 */
public static int convertDPtoPX(Context context, int DP) {
	return Math.round(DP * context.getResources().getDisplayMetrics().density);
}

	/**
	 * Draws an round rectangle based on event color. It is accessed via other methods which set various size parameters.
	 * @author justinas.marcinka@gmail.com
	 * @param context - method context.
	 * @param widthPX - rectangle width.
	 * @param heightPX - rectangle height.
	 * @param roundRadius - corners round radius for round rectangle.
	 * @param event - event, for which this rectangle is drawn.
	 * @param shadow  - indicates whether to draw shadow for this rectangle.
	 * @return Bitmap that represents this rectangle.
	 */
private static Bitmap getRoundRectBitmap(Context context, int widthPX, int heightPX, int roundRadius, Event event, boolean shadow){
		
		int shadowRadius = convertDPtoPX(context, 2);
		int shadowOffset = convertDPtoPX(context, Math.round(heightPX * 0.1f)); 
		
		
		Bitmap bmp = Bitmap.createBitmap(widthPX + shadowRadius, heightPX + shadowRadius,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		Paint p = new Paint();
		p.setColor(Color.parseColor("#" + event.getColor()));
		if (shadow){
			p.setShadowLayer(shadowRadius, shadowOffset, shadowOffset, context.getResources().getColor(R.color.darker_gray));
		}
		final RectF rect = new RectF();
		rect.set(0, 0, widthPX, heightPX);
		c.drawRoundRect(rect, roundRadius,
				roundRadius, p);
		return bmp;
}
/**
 * Draws an round rectangle based on event color. 
 * @param context - method context.
 * @param sizeDP - rectangle size. Rectangle height is same, and width is half of specified size.
 * @param event - event, for which this rectangle is drawn.  
 * @param shadow  - indicates whether to draw shadow for this rectangle
 * @return Bitmap that represents this rectangle.
 */
public static Bitmap getEventRoundRectangle (Context context, int sizeDP, Event event, boolean shadow){
	int heightPX = convertDPtoPX(context, sizeDP);
	int widthPX = heightPX / 2;
	int roundRadius = Math.round(heightPX * 0.2f);
	return getRoundRectBitmap(context, widthPX, heightPX, roundRadius, event, shadow);
}

}
