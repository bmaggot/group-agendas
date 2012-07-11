package com.groupagendas.groupagenda.events;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.groupagendas.groupagenda.R;
import com.groupagendas.groupagenda.data.DataManagement;
import com.groupagendas.groupagenda.utils.DateTimeUtils;

public class EventsAdapter extends BaseAdapter implements Filterable{
	private List<Event> events;
	private List<Event> eventsAll;
	private LayoutInflater mInflater;
	private Context mContext;
	private DataManagement dm;
	private String mFilter = null;
	private DateTimeUtils dtUtils;
	public EventsAdapter(List<Event> objects, Activity activity){
		events = objects;
		eventsAll = objects;
		mInflater = LayoutInflater.from(activity);
		mContext = activity;
		dm = DataManagement.getInstance(activity);
		dtUtils = new DateTimeUtils(activity);
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
		
		holder.title.setText(event.title);
		holder.date.setText(dtUtils.formatDateTime(event.my_time_start));
		// type
		StringBuilder sb = new StringBuilder(event.type).append("_type");
		holder.type.setText(mContext.getResources().getIdentifier(sb.toString(), "string", mContext.getPackageName()));
		
		if(event.is_owner){
			holder.creator.setText(mContext.getString(R.string.you));
		}else{
			holder.creator.setText(event.creator_fullname);
		}
		
		// status
		//if(event.type.equals("t") || event.type.equals("r") || event.type.equals("o")){
			switch (event.status) {
			case 0:
				holder.status.setText(mContext.getString(R.string.status_0));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.INVISIBLE);
				break;
			case 1:
				holder.status.setText(mContext.getString(R.string.status_1));
				holder.button_yes.setVisibility(View.INVISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			case 2:
				holder.status.setText(mContext.getString(R.string.status_2));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.INVISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			case 4:
				holder.status.setText(mContext.getString(R.string.new_invite));
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				break;
			}
		//}	
		
		//
		holder.status_1.setText(String.valueOf(event.attendant_1_count));
		holder.status_2.setText(String.valueOf(event.attendant_2_count));
		
		holder.button_yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean success = dm.changeEventStatus(event.event_id, "1");
				holder.button_yes.setVisibility(View.INVISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				holder.status.setText(mContext.getString(R.string.status_1));
				editDb(event.event_id, 1, success);
				event.status = 1;
				notifyDataSetChanged();
			}
		});
		
		holder.button_maybe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean success = dm.changeEventStatus(event.event_id, "2");
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.INVISIBLE);
				holder.button_no.setVisibility(View.VISIBLE);
				holder.status.setText(mContext.getString(R.string.status_2));
				editDb(event.event_id, 2, success);
				event.status = 2;
				notifyDataSetChanged();
			}
		});
		
		holder.button_no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boolean success = dm.changeEventStatus(event.event_id, "0");
				holder.button_yes.setVisibility(View.VISIBLE);
				holder.button_maybe.setVisibility(View.VISIBLE);
				holder.button_no.setVisibility(View.INVISIBLE);
				holder.status.setText(mContext.getString(R.string.status_0));
				editDb(event.event_id, 0, success);
				event.status = 0;
				notifyDataSetChanged();
			}
		});
		
		return convertView;
	}
	
	private void editDb(int event_id, int status, boolean success){
		ContentValues values = new ContentValues();
		values.put(EventsProvider.EMetaData.EventsMetaData.STATUS, status);
		if(!success){
			values.put(EventsProvider.EMetaData.EventsMetaData.NEED_UPDATE, 4);
		}
		String where = EventsProvider.EMetaData.EventsMetaData.E_ID+"="+event_id;
		mContext.getContentResolver().update(EventsProvider.EMetaData.EventsMetaData.CONTENT_URI, values, where, null);
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
		return events.get(position).event_id;
	}
	
	public void setItems(List<Event> items){
		events = items;
		eventsAll = items;
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
                		if(items.get(i).type.equals("r")){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals(mContext.getString(R.string.t_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).type.equals("t")){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals(mContext.getString(R.string.o_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).type.equals("o")){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals(mContext.getString(R.string.n_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).type.equals("n")){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals(mContext.getString(R.string.p_type))){
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).type.equals("p")){
                			filtereItems.add(items.get(i));
                		}
                	}
                }else if(c.equals("all")){
                	filtereItems = items;
                }else{
                	for(int i=0, l=items.size(); i<l; i++){
                		if(items.get(i).status == Integer.parseInt(c.toString())){
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
}
