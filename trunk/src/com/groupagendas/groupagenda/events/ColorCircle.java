package com.groupagendas.groupagenda.events;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class ColorCircle extends View{
	private String mColor;
	public int mRadius = 6;
	private int cX = 0;
	private int cY = 0;
	public ColorCircle(Context context, String color) {
		super(context);
		mColor = color;
	}
	
	public ColorCircle(Context context, String color, int radius, int cx, int cy) {
		super(context);
		mColor = color;
		mRadius = radius;
		cX = cx;
		cY = cy;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Integer.parseInt(mColor, 16)+0xFF000000);
		paint.setAntiAlias(true);
		canvas.drawCircle(mRadius+cX, mRadius+cY, mRadius, paint);
		super.onDraw(canvas);
	}
}
