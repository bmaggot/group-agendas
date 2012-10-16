package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.EventManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;

public class EventsAdapter extends BaseAdapter implements Filterable{
	private List<Event> events;
	private List<Event> eventsAll;
	private LayoutInflater mInflater;
	private Context mContext;
	private DateTimeUtils dtUtils;
	private String mFilter = null;
	private int newInvitesCount;
	public EventsAdapter(List<Event> objects, Activity activity){
		events = objects;
		eventsAll = objects;
		mInflater = LayoutInflater.from(activity);
		mContext = activity;
		dtUtils = new DateTimeUtils(activity);
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

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final Event event = events.get(position);
		
		holder.title.setText(event.getActualTitle());

		if (event.getStartCalendar()!= null) {
			String temp = dtUtils.formatDateTime(event.getStartCalendar());
			holder.date.setText(temp);
		}
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
		
		// status
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
	
		holder.status_1.setText(String.valueOf(event.getAttendant_1_count()));
		holder.status_2.setText(String.valueOf(event.getAttendant_2_count()));
		
		holder.button_yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d("RESPONSE TO EVENT " + event.getEvent_id(), mContext.getString(R.string.status_attending) );
//				boolean success = dm.changeEventStatus(event.getEvent_id(), "1");
				holder.button_yes.setVisibility(View.INVISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				holder.status.setText(mContext.getString(R.string.status_attending));
				event.setStatus(Invited.ACCEPTED);
				respondToInvite(event);
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
				respondToInvite(event);
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
				respondToInvite(event);
			}
		});
		
		return convertView;
	}
	
	private void respondToInvite(Event event){
		System.out.println("response " + event.getStatus() );
		event.getMyInvite().setStatus(event.getStatus());
		
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
	
	public void setFilter(String filter){
		mFilter = filter;
	}
	@Override
	public Filter getFilter() {
		return new Filter() {
            @SuppressWarnings("unchecked")
			@Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                events = (List<Event>) results.values;
                notifyDataSetChanged();
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
                List<Event> filtereItems = new ArrayList<Event>();
                
                if(c.equals(mContext.getString(R.string.r_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		String itemType = items.get(i).getType();
                		if(itemType.equals(Event.SHARED_EVENT) || itemType.equals(Event.TELEPHONE_CALL)){
                			filtereItems.add(items.get(i));
                		}
                	}
//                }else if(c.equals(mContext.getString(R.string.t_type))){ telephone calls are now shown with shared events
//                	for(int i=0, l=items.size(); i<l; i++){
//                		if(items.get(i).getType().equals("t")){
//                			filtereItems.add(items.get(i));
//                		}
//                	}
                }else if(c.equals(mContext.getString(R.string.o_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getType().equals(Event.OPEN_EVENT)){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals(mContext.getString(R.string.n_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getType().equals(Event.SHARED_NOTE)){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals(mContext.getString(R.string.p_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getType().equals(Event.NOTE)){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals("all")){
                	filtereItems = items;
                }else{
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).getStatus() == Integer.parseInt(c.toString())){
                			filtereItems.add(items.get(i));
                		}
                	}
                }
                
                return filtereItems;
            }
        };
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public int getNewInvitesCount() {
		return newInvitesCount;
	}

}
