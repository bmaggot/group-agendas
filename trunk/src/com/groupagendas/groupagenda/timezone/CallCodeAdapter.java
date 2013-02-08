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
import com.groupagendas.groupagenda.utils.TimezoneUtils.StaticTimezone;

public class CallCodeAdapter extends AbstractAdapter<StaticTimezone> implements
		Filterable {
	public static final int COUNTRY = 1;
	public static final int TIMEZONE = 2;
	/** All adapter's ArrayList's items. */
	private List<StaticTimezone> allItems;
	/** Displayed layout's resource ID. */
	private int textViewResourceId = 0;

	/**
	 * 
	 * Custom adapter for ArrayList of StaticTimezone objects.
	 * 
	 * @author meska.lt@gmail.com
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param textViewResourceId
	 * @param countries
	 * @since 2012-10-09
	 * @version 1.1
	 */
	public CallCodeAdapter(Context context, int textViewResourceId,
			List<StaticTimezone> countries) {
		super(context, filterDuplicateEntries(countries));
		this.allItems = list;
		this.textViewResourceId = textViewResourceId;
	}

	private static List<StaticTimezone> filterDuplicateEntries(
			List<StaticTimezone> countries) {
		ArrayList<StaticTimezone> filteredList = new ArrayList<StaticTimezone>();

		if (!countries.isEmpty()) {
			for (int i = 1; i < countries.size(); i++) {
				StaticTimezone entry = countries.get(i);
				if (!entry.call_code
						.equalsIgnoreCase(countries.get(i - 1).call_code))
					filteredList.add(entry);
			}
		}
		return filteredList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String temp;
		if (convertView == null) {
			convertView = View.inflate(getContext(), textViewResourceId, null);
		}

		temp = list.get(position).call_code;
		((TextView) convertView).setText("+" + temp);
		convertView.setTag(list.get(position).id);

		return convertView;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				if (constraint.length() < 1) {
					list = allItems;
				} else {
					list = (List<StaticTimezone>) results.values;
				}
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<StaticTimezone> filteredResults = getFilteredResults(constraint);

				FilterResults results = new FilterResults();
				results.values = filteredResults;

				return results;
			}

			private List<StaticTimezone> getFilteredResults(CharSequence constraint) {
				List<StaticTimezone> items = allItems;
				List<StaticTimezone> filteredItems = new ArrayList<StaticTimezone>();

				if (constraint.length() < 1) {
					filteredItems = allItems;
				} else {

					for (int i = 0; i < items.size() - 1; i++) {
						if (constraint.charAt(0) == '+') {
							constraint = constraint.subSequence(1, constraint.length());
						}
						if (items.get(i).call_code.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
							filteredItems.add(items.get(i));
						}
					}
				}

				return filteredItems;
			}
		};
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

}