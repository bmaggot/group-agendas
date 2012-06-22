package com.bog.calendar.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.TypedValue;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UIBuildHelper {
    /**
     * Generate background shape with rounded corners.
     *
     * @param context     context
     * @param color       color
     * @param alpha       alpha
     * @param radiusInDip corners radius In Dip
     * @return ShapeDrawable
     */
    public static ShapeDrawable createRoundedBackgroundShape(Context context, int color, int alpha, int radiusInDip) {
        float cornerRadius = dipToPixelsConvert(context, radiusInDip);
        float[] outerR = new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
        RoundRectShape rect = new RoundRectShape(outerR, null, null);
        CustomBorderDrawable bg = new CustomBorderDrawable(rect, Color.WHITE, 2);
        Paint bgPaint = bg.getPaint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(color);
        bgPaint.setAlpha(alpha);
        return bg;
    }

    /**
     * Convert DIP units to pixels
     *
     * @param context context
     * @param dip     dip size
     * @return px size
     */
    public static float dipToPixelsConvert(Context context, float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    /**
     * Custom border with padding
     */
    public static class CustomBorderDrawable extends ShapeDrawable {
        private Paint fillPaint;
        private Paint strokePaint;

        public CustomBorderDrawable(Shape s, int strokeColor, int strokeWidth) {
            super(s);
            fillPaint = this.getPaint();
            strokePaint = new Paint(fillPaint);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(strokeWidth);
            strokePaint.setColor(strokeColor);
        }

        @Override
        protected void onDraw(Shape shape, Canvas canvas, Paint fillpaint) {
            shape.draw(canvas, fillpaint);
            shape.draw(canvas, strokePaint);
        }

        public void setFillColour(int c) {
            fillPaint.setColor(c);
        }
    }

    public static String formatTimeToString(long time, String pattern) {
        Date dat = new Date(time);
        SimpleDateFormat formatterCutDetailsDate = new SimpleDateFormat(pattern);
        return formatterCutDetailsDate.format(dat);
    }

    public static String getMonthByIndex(int m) {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (m >= 0 && m <= 11 ) {
            return months[m];
        }
        return null;
    }
}
