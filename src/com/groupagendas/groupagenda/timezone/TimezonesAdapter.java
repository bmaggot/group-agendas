package com.groupagendas.groupagenda.timezone;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.groupagendas.groupagenda.calendar.adapters.AbstractAdapter;
import com.groupagendas.groupagenda.events.EventActivity.StaticTimezones;

public class TimezonesAdapter extends AbstractAdapter<StaticTimezones> implements Filterable {
	public static final int COUNTRY = 1;
	public static final int TIMEZONE = 2;
	/** All adapter's ArrayList's items. */
	private List<StaticTimezones> allItems;
	/** Displayed layout's resource ID. */
	private int textViewResourceId = 0;

	/**
	 * 
	 * Custom adapter for ArrayList of String objects.
	 * 
	 * @author meska.lt@gmail.com
	 * @param context
	 * @param textViewResourceId
	 * @param countries
	 * @since 2012-10-04
	 * @version 1.0
	 */
	public TimezonesAdapter(Context context, int textViewResourceId, List<StaticTimezones> countries) {
		super(context, countries);
		this.allItems = countries;
		this.textViewResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String temp;
		if (convertView == null) {
			convertView = View.inflate(getContext(), textViewResourceId, null);
		}

		temp = list.get(position).altname;
		if (position >= 0) {
			TextView view = (TextView) convertView;
			view.setText(temp);
			convertView.setTag(list.get(position).id);
		}

		return convertView;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				list = (List<StaticTimezones>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<StaticTimezones> filteredResults = getFilteredResults(constraint);

				FilterResults results = new FilterResults();
				results.values = filteredResults;

				return results;
			}

			private List<StaticTimezones> getFilteredResults(CharSequence constraint) {
				List<StaticTimezones> items = allItems;
				List<StaticTimezones> filteredItems = new ArrayList<StaticTimezones>();

				for (int i = 0; i < items.size() - 1; i++) {
					if (items.get(i).altname.toLowerCase().contains(constraint.toString().toLowerCase())) {
						filteredItems.add(items.get(i));
					}
				}

				if (constraint.length() < 1)
					filteredItems = items;

				return filteredItems;
			}
		};
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

}