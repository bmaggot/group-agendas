package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;
import com.groupagendas.groupagenda.utils.DrawingUtils;
import com.groupagendas.groupagenda.utils.Utils;

public class PollResultsActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pollresults);
		
		ArrayList<Invited> invited = EventEditActivity.event.getInvited();
		int invitedSize = invited.size();
		int guid = 0;
		for(int y = 0; y < invitedSize+2; y++){
			Invited invite = null;
			if(y > 0 && y < invitedSize+1){
				invite = invited.get(y-1);
			}
			TableLayout table = (TableLayout) findViewById(R.id.table);
			TableRow row1 = new TableRow(this);
			TextView date1 = new TextView(this);
			TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.FILL_PARENT);
			int padding = DrawingUtils.convertDPtoPX(5);
	        date1.setPadding(padding, padding, padding, padding-1);
	        date1.setGravity(Gravity.CENTER_VERTICAL);
			date1.setLayoutParams(params);
			date1.setBackgroundDrawable(getResources().getDrawable(R.drawable.cell_shape));
			date1.setTextColor(Color.BLACK);
			if(y == invitedSize+1){
				date1.setText("Total");
			} else {
				if(/*y > 0 && y < invitedSize+1*/invite != null){
					date1.setText(invite.getName());
					guid = invite.getGuid();
				}
			}
			row1.addView(date1);
			String jsonArrayString = getIntent().getStringExtra("pollTime");
			try {
				if (jsonArrayString != null && !jsonArrayString.contentEquals("null")) {
					final JSONArray jsonArray = new JSONArray(jsonArrayString);
					for (int i = 0; i < jsonArray.length(); i++) {
						final JSONObject pollThread = jsonArray.getJSONObject(i);
						
						String temp = "";
						try {
							temp = pollThread.getString("start");
						} catch (JSONException e) {
							Log.e("PollAdapter", "Failed getting poll time.");
						}
	
						DateTimeUtils dateTimeUtils = new DateTimeUtils(PollResultsActivity.this);
						
						final Calendar tempCal = Utils.stringToCalendar(PollResultsActivity.this, temp,
								DataManagement.SERVER_TIMESTAMP_FORMAT);
						String startTime = dateTimeUtils.formatDate(tempCal) + " " + dateTimeUtils.formatTime(tempCal);
						
						try {
							temp = pollThread.getString("end");
						} catch (JSONException e) {
							Log.e("PollAdapter", "Failed getting poll time.");
						}
	
						final Calendar tempCal2 = Utils.stringToCalendar(PollResultsActivity.this, temp,
								DataManagement.SERVER_TIMESTAMP_FORMAT);
						String endTime = dateTimeUtils.formatDate(tempCal2) + " " + dateTimeUtils.formatTime(tempCal2);
						
						JSONArray list = pollThread.getJSONArray("voted");
						
				        TextView date = new TextView(this);
//				        if(i == 0){
//				        	date.setLayoutParams(new TableRow.LayoutParams(Math.round(143 * Resources.getSystem().getDisplayMetrics().density), Math.round(36 * Resources.getSystem().getDisplayMetrics().density)));
//						} else {
				        //params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				        date.setPadding(padding, padding, padding, padding-1);
						date.setLayoutParams(params);
						date.setTextColor(Color.BLACK);
						if(y == 0){
							date.setText(startTime+"\n"+endTime);
						} else {
							date.setGravity(Gravity.CENTER);
							if(y == invitedSize+1){
								date.setText(""+list.length());
							} else {
								for (int a = 0; a < list.length(); a++) {
									int votedThread = list.getInt(a);
									if(guid == votedThread){
										date.setText("+");
									}
								}
								
							}
						}
//						}
				        date.setBackgroundDrawable(getResources().getDrawable(R.drawable.cell_shape));
				        
				        row1.addView(date);
				       
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			table.addView(row1);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}

}
