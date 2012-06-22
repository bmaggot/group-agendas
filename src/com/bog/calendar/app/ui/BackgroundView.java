package com.bog.calendar.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.groupagendas.groupagenda.R;

public class BackgroundView extends LinearLayout {
    private final int rowsCount = 25;
    private int rowHeight = 10;
    private int timeColumnWidth = 10;
    private int componentWidth;
    private int componentHeight;
    private Paint bkPaint = null;
    private Paint bkTimePaint = null;
    private Paint mainLinePaint = null;
    private Paint secondaryLinePaint = null;

    public BackgroundView(Context context) {
        super(context);
        initView();
    }

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    private void initView() {
        this.setClickable(false);
//        this.setOrientation(VERTICAL);
        this.setBackgroundColor(Color.WHITE);
        rowHeight = getContext().getResources().getDimensionPixelSize(R.dimen.cell_height);
        timeColumnWidth = getContext().getResources().getDimensionPixelSize(R.dimen.time_panel_width);
        componentHeight = rowHeight * rowsCount;
        bkPaint = new Paint();
        bkPaint.setColor(Color.WHITE);
        bkPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bkTimePaint = new Paint(bkPaint);
        bkTimePaint.setColor(getContext().getResources().getColor(R.color.lighter_gray));
        mainLinePaint = new Paint();
        mainLinePaint.setColor(getContext().getResources().getColor(R.color.mainBgLineColor));
        mainLinePaint.setAntiAlias(true);
        secondaryLinePaint = new Paint();
        secondaryLinePaint.setColor(getContext().getResources().getColor(R.color.secondaryBgLineColor));
        secondaryLinePaint.setAntiAlias(true);
        secondaryLinePaint.setStyle(Paint.Style.STROKE);
        secondaryLinePaint.setPathEffect(new DashPathEffect(new float[]{2, 2}, 0));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawRect(0, 0, componentWidth, componentHeight, bkPaint);//clear view
        canvas.drawRect(0, 0, timeColumnWidth, componentHeight, bkTimePaint);
        float yPos = rowHeight;
        for (int i = 0; i < rowsCount; i++) {
            canvas.drawLine(0, yPos - rowHeight / 2, componentWidth, yPos - rowHeight / 2, secondaryLinePaint);
            canvas.drawLine(0, yPos, componentWidth, yPos, mainLinePaint);
            yPos += rowHeight;
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.setMinimumHeight(componentHeight);
        componentWidth = getWidth();
//        LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, componentHeight);
//        setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, componentHeight);
    }
}
