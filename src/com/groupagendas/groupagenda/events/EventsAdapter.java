package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.EventManagement;

public class EventsAdapter extends BaseAdapter implements Filterable{
	private List<Event> events;
	private List<Event> eventsAll;
	private LayoutInflater mInflater;
	private Context mContext;
//	private DateTimeUtils dtUtils;
	private String mFilter = null;
	private Filter filter = null;
	private int newInvitesCount;
	private LinearLayout status1block;
	private LinearLayout status2block;
	private LinearLayout events_status_line;
	private RelativeLayout poll_status_line;
	public EventsAdapter(List<Event> objects, Activity activity){
		events = objects;
		eventsAll = objects;
		mInflater = LayoutInflater.from(activity);
		mContext = activity;
//		dtUtils = new DateTimeUtils(activity);
		setNewInvitesCount(events);
		
	}
	
	/**
	 * @author justinas.marcinka@gmail.com
	 * Method sets count of new events in given arrayList to counter in adapter.
	 * @param events
	 */
	private void setNewInvitesCount(List<Event> events){
		newInvitesCount = 0;
		for (Event e : events)
			if(e.getStatus() == Invited.PENDING) newInvitesCount++;
	
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.event_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.type = (TextView) convertView.findViewById(R.id.type);
			holder.status = (TextView) convertView.findViewById(R.id.status);
			holder.status_1 = (TextView) convertView.findViewById(R.id.status_1);
			holder.status_2 = (TextView) convertView.findViewById(R.id.status_2);
			holder.creator = (TextView) convertView.findViewById(R.id.creator);
			
			holder.button_yes = (TextView) convertView.findViewById(R.id.button_yes);
			holder.button_maybe = (TextView) convertView.findViewById(R.id.button_maybe);
			holder.button_no = (TextView) convertView.findViewById(R.id.button_no);
			holder.reject_poll = (TextView) convertView.findViewById(R.id.button_reject);
			holder.rejoin_poll = (TextView) convertView.findViewById(R.id.button_rejoin);
			holder.poll_status = (TextView) convertView.findViewById(R.id.poll_status);
			status1block = (LinearLayout) convertView.findViewById(R.id.status_1_linearBlock);
			status2block = (LinearLayout) convertView.findViewById(R.id.status_2_linearBlock);
			events_status_line = (LinearLayout) convertView.findViewById(R.id.events_status_line);
			poll_status_line = (RelativeLayout) convertView.findViewById(R.id.poll_status_line);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final Event event = events.get(position);
		
		holder.title.setText(event.getActualTitle());

		String temp = "";

		if (event.isBirthday() && event.is_all_day()) {
			temp = event.getEvents_day();
		} else {
			temp = event.getEvents_day() +" "+ event.getEvent_day_start(mContext);
		}
		holder.date.setText(temp);
		// type
		
		StringBuilder sb = new StringBuilder(event.getType()).append("_type");
		if(mContext.getResources().getIdentifier(sb.toString(), "string", mContext.getPackageName()) != 0){
			holder.type.setText(mContext.getResources().getIdentifier(sb.toString(), "string", mContext.getPackageName()));
		}
		
		if(event.is_owner()){
			holder.creator.setText(mContext.getString(R.string.you));
		}else{
			holder.creator.setText(event.getCreator_fullname());
		}
		
		status1block = (LinearLayout) convertView.findViewById(R.id.status_1_linearBlock);
		status2block = (LinearLayout) convertView.findViewById(R.id.status_2_linearBlock);
		events_status_line = (LinearLayout) convertView.findViewById(R.id.events_status_line);
		poll_status_line = (RelativeLayout) convertView.findViewById(R.id.poll_status_line);
		
		// status
		if(!event.isNative()){
			status1block.setVisibility(View.VISIBLE);
			status2block.setVisibility(View.VISIBLE);
			switch (event.getStatus()) {
			case 0:
				holder.status.setText(mContext.getString(R.string.status_not_attending));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.INVISIBLE);
				break;
			case 1:
				holder.status.setText(mContext.getString(R.string.status_attending));
				holder.button_yes.setVisibility(View.INVISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			case 2:
				holder.status.setText(mContext.getString(R.string.status_pending));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.INVISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			case 4:
				holder.status.setText(mContext.getString(R.string.status_new_invite));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			}
		} else {
			status1block.setVisibility(View.GONE);
			status2block.setVisibility(View.GONE);
			holder.status.setText(mContext.getString(R.string.native_type));
			holder.button_yes.setVisibility(View.GONE);
			holder.button_maybe.setVisibility(View.GONE);
			holder.button_no.setVisibility(View.GONE);
		}
		
		if(!event.isNative()){
			holder.status_1.setText(""+event.getAttendant_1_count());
			holder.status_2.setText(""+(event.getAttendant_2_count()+event.getAttendant_4_count()));
			
			holder.button_yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Log.d("RESPONSE TO EVENT " + event.getEvent_id(), mContext.getString(R.string.status_attending) );
					holder.button_yes.setVisibility(View.INVISIBLE);
					holder.button_maybe.setVisibility(View.VISIBLE);
					holder.button_no.setVisibility(View.VISIBLE);
					holder.status.setText(mContext.getString(R.string.status_attending));
					event.setStatus(Invited.ACCEPTED);
					respondToInvite(event, Invited.ACCEPTED);
				}
			});
			
			holder.button_maybe.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Log.d("RESPONSE TO EVENT " + event.getEvent_id(), mContext.getString(R.string.status_maybe) );
					holder.button_yes.setVisibility(View.VISIBLE);
					holder.button_maybe.setVisibility(View.INVISIBLE);
					holder.button_no.setVisibility(View.VISIBLE);
					holder.status.setText(mContext.getString(R.string.status_maybe));
					event.setStatus(Invited.MAYBE);
					respondToInvite(event, Invited.MAYBE);
				}
			});
			
			holder.button_no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Log.d("RESPONSE TO EVENT " + event.getEvent_id(), mContext.getString(R.string.status_not_attending) );
					holder.button_yes.setVisibility(View.VISIBLE);
					holder.button_maybe.setVisibility(View.VISIBLE);
					holder.button_no.setVisibility(View.INVISIBLE);
					holder.status.setText(mContext.getString(R.string.status_not_attending));
					event.setStatus(Invited.REJECTED);
					respondToInvite(event, Invited.REJECTED);
				}
			});
		}
		
		if(event.getType().contentEquals("v")){
			poll_status_line.setVisibility(View.VISIBLE);
			events_status_line.setVisibility(View.GONE);
			holder.date.setVisibility(View.INVISIBLE);
			
			if (event.getStatus() == Invited.REJECTED) {
				holder.reject_poll.setVisibility(View.INVISIBLE);
				holder.rejoin_poll.setVisibility(View.VISIBLE);
				holder.poll_status.setText(R.string.rejected);
			} else {
				holder.rejoin_poll.setVisibility(View.INVISIBLE);
				holder.reject_poll.setVisibility(View.VISIBLE);
				holder.poll_status.setText(R.string.joined);
			}
			
			holder.reject_poll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//saveButton.setEnabled(true);
					holder.poll_status.setText(R.string.rejected);
					holder.reject_poll.setVisibility(View.INVISIBLE);
					holder.rejoin_poll.setVisibility(View.VISIBLE);
					event.setStatus(Invited.REJECTED);
					respondToInvite(event, Invited.REJECTED);
					EventEditActivity.deleteEventFromPollList(event);
					event.setSelectedEventPollsTime("[]");
					new RejectPollTask().execute(event);
					//poll_status = Invited.REJECTED;

				}
			});

			holder.rejoin_poll.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//saveButton.setEnabled(true);
					holder.poll_status.setText(R.string.joined);
					holder.rejoin_poll.setVisibility(View.INVISIBLE);
					holder.reject_poll.setVisibility(View.VISIBLE);
					event.setStatus(Invited.ACCEPTED);
					respondToInvite(event, Invited.ACCEPTED);
					EventEditActivity.addEventToPollList(mContext, event);
					new RejoinPollTask().execute(event);
					//poll_status = Invited.ACCEPTED;

				}
			});
			
		} else {
			poll_status_line.setVisibility(View.GONE);
			events_status_line.setVisibility(View.VISIBLE);
			holder.date.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
	
	private void respondToInvite(Event event, int status) {
//		System.out.println("response " + event.getStatus() );
		event = EventManagement.getEventFromLocalDb(mContext, event.getInternalID(), EventManagement.ID_INTERNAL);

		event.setStatus(status);
		
		if(event.getMyInvite() != null){
			event.getMyInvite().setStatus(status);
		}
		
		if (newInvitesCount > 0) newInvitesCount--;
		EventManagement.respondToInvitation(mContext, event);
		notifyDataSetChanged();
	}
	
	static class ViewHolder {
		TextView title;
		TextView date;
		TextView type;
		TextView status;
		TextView creator;
		TextView status_1;
		TextView status_2;
		TextView button_yes;
		TextView button_maybe;
		TextView button_no;
		TextView reject_poll;
		TextView rejoin_poll;
		TextView poll_status;
	}
	
	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public Object getItem(int position) {
		return events.get(position);
	}

	@Override
	public long getItemId(int position) {
		return events.get(position).getEvent_id();
	}
	
	public void setItems(List<Event> items){
		events = items;
		eventsAll = items;
		setNewInvitesCount(items);
		if(mFilter != null) getFilter().filter(mFilter);
	}
	
	class RejectPollTask extends AsyncTask<Event, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... voids) {
			if(EventManagement.rejectPoll(mContext, ""+voids[0].getEvent_id())){
				voids[0].setUploadedToServer(true);
			} else {
				voids[0].setUploadedToServer(false);
			}
			EventManagement.updateEventSelectedPollsTimeInLocalDb(mContext, voids[0]);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		}
	}
	
	class RejoinPollTask extends AsyncTask<Event, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Event... voids) {
			if(EventManagement.rejoinPoll(mContext, ""+voids[0].getEvent_id())){
				voids[0].setUploadedToServer(true);
			} else {
				voids[0].setUploadedToServer(false);
			}
			EventManagement.updateEventSelectedPollsTimeInLocalDb(mContext, voids[0]);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		}
	}
	
	public void setFilter(String filter){
		mFilter = filter;
	}
	@Override
	public Filter getFilter() {
		if (filter == null)
			filter = new Filter() {
            @SuppressWarnings("unchecked")
			@Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                events = (List<Event>) results.values;
                EventsAdapter.this.notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Event> filteredResults = getFilteredResults(constraint);
                
                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
            
            private List<Event> getFilteredResults(CharSequence c) {
                List<Event> items = EventsAdapter.this.eventsAll;
              
                
                
                if (c == null) return items;
                if (c.equals("all")) return items;
                
                List<Event> filteredItems = new ArrayList<Event>();
                
                if(c.equals(mContext.getString(R.string.r_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		String itemType = items.get(i).getType();
                		if(itemType.equals(Event.SHARED_EVENT) || itemType.equals(Event.TELEPHONE_CALL)){
                			filteredItems.add(items.get(i));
                		}
                	}
                	return filteredItems;
                }
                
                if(c.equals(mContext.getString(R.string.o_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getType().equals(Event.OPEN_EVENT)){
                			filteredItems.add(items.get(i));
                		}
                	}
                	return filteredItems;
                }

                if(c.equals(mContext.getString(R.string.n_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getType().equals(Event.SHARED_NOTE)){
                			filteredItems.add(items.get(i));
                		}
                	}
                	return filteredItems;
                }

                if(c.equals(mContext.getString(R.string.p_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getType().equals(Event.NOTE)){
                			filteredItems.add(items.get(i));
                		}
                	}
                	return filteredItems;
                }
                
//                filtering by status
                for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getStatus() == Integer.parseInt(c.toString())){
                			filteredItems.add(items.get(i));
                		}
                	}
                
                
                return filteredItems;
            }
        };
        
        return filter;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public int getNewInvitesCount() {
		return newInvitesCount;
	}

}
