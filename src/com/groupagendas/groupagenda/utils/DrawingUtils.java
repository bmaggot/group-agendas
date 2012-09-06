package com.groupagendas.groupagenda.utils;

import com.groupagendas.groupagenda.events.Event;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class DrawingUtils {
private static int ROUND_RECTANGLE_X_RADIUS_DP = 2;	
private static int ROUND_RECTANGLE_Y_RADIUS_DP = 1;	

private static int ROUND_RECTANGLE_X_RADIUS_PX = 0;	
private static int ROUND_RECTANGLE_Y_RADIUS_PX = 0;	

public static float densityFactor = 0;

/**
 * method returns pixel value of given dp dimension for current device
 * @author justinas.marcinka@gmail.com
 * @param DP dimension in DP
 * @return dimension in pixels to particular device
 */
public static int convertDPtoPX(Context context, int DP) {
	if (densityFactor == 0) densityFactor = context.getResources().getDisplayMetrics().density;
	return Math.round(DP * densityFactor);
}

public static int getRectangleRadiusX(Context context) {
	if (ROUND_RECTANGLE_X_RADIUS_PX == 0) ROUND_RECTANGLE_X_RADIUS_PX = convertDPtoPX(context, ROUND_RECTANGLE_X_RADIUS_DP);
	return ROUND_RECTANGLE_X_RADIUS_PX;
}

	
public static ImageView drawColourRectangleForEvent(Context context, int widthPX, int heightPX, Event event ){	
		ImageView img = new ImageView(context);
		img.setBackgroundDrawable(new BitmapDrawable(getColourEventRectBitmapDrawable(context, widthPX, heightPX, event)));		
		return img;	
	}
public static Bitmap getColourEventRectBitmapDrawable(Context context, int widthPX, int heightPX, Event event){
		Bitmap bmp = Bitmap.createBitmap(widthPX, heightPX,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		Paint p = new Paint();
		p.setColor(Color.parseColor("#" + event.getColor()));
		
		final RectF rect = new RectF();
		rect.set(0, 0, widthPX, heightPX);
		c.drawRoundRect(rect, getRectangleRadiusX(context),
				getRectangleRadiusX(context), p);
		return bmp;
}
}
