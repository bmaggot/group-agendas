package com.groupagendas.groupagenda.calendar.week;

import com.groupagendas.groupagenda.R;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class VerticalDaysSeparator extends TextView {
	int LINE_WIDTH_DIP = 1;
	protected final float densityFactor = getResources().getDisplayMetrics().density;

	public VerticalDaysSeparator(Context context) {
		super(context);
		LayoutParams params = new LayoutParams(Math.round(densityFactor * LINE_WIDTH_DIP), LayoutParams.MATCH_PARENT);
		this.setLayoutParams(params);
		this.setBackgroundResource(R.drawable.border_darkgray);
		this.setGravity(Gravity.CENTER_VERTICAL);
	}

}
