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

public class StringArrayListAdapter extends AbstractAdapter<String> implements Filterable {
	/** All adapter's ArrayList's items. */
	private List<String> allItems;
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
	public StringArrayListAdapter(Context context, int textViewResourceId, List<String> countries) {
		super(context, countries);
		this.allItems = countries;
		this.textViewResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(getContext(), textViewResourceId, null);
		}

		String temp = list.get(position);
		if (position >= 0) {
			((TextView) convertView).setText(temp);
			convertView.setTag("" + position);
			if (position > 0 && list.get(position - 1).equals(temp))
				convertView.setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				list = (List<String>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<String> filteredResults = getFilteredResults(constraint);

				FilterResults results = new FilterResults();
				results.values = filteredResults;

				return results;
			}

			private List<String> getFilteredResults(CharSequence constraint) {
				List<String> items = allItems;
				List<String> filteredItems = new ArrayList<String>();

				for (int i = 0; i < items.size() - 1; i++) {
					if (items.get(i).toLowerCase().startsWith(constraint.toString().toLowerCase())) {
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
