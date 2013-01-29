package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.events.Event;
import com.groupagendas.groupagenda.events.EventEditActivity;

public class SelectPollForCopyingDialog extends Dialog {

	public SelectPollForCopyingDialog(final Context context, ArrayList<String> dates_list, final Event event) {
		super(context);
		this.setContentView(R.layout.select_poll_for_copying_dialog_layout);
		this.setTitle(R.string.select_poll_to_copy);
		LinearLayout layout = (LinearLayout) findViewById(R.id.select_poll_for_copying);
		for(final String string : dates_list){
			Button button = new Button(context);
			button.setText(string);
			button.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.event_invite_people_button_standalone));
			button.setTextColor(Color.BLACK);
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismiss();
					((EventEditActivity) context).finish();
					Intent intent = new Intent(context, EventEditActivity.class);
					intent.putExtra("event_id", event.getInternalID());
					intent.putExtra("type", event.getType());
					intent.putExtra("isNative", event.isNative());
					intent.putExtra("copy", true);
					intent.putExtra("times", string);
					context.startActivity(intent);
				}
			});
			layout.addView(button);
		}
	}

}
