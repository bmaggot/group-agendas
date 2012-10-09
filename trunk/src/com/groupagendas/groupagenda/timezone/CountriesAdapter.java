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

public class CountriesAdapter extends AbstractAdapter<StaticTimezones> implements Filterable {
	public static final int COUNTRY = 1;
	public static final int TIMEZONE = 2;
	/** All adapter's ArrayList's items. */
	private List<StaticTimezones> allItems;
	/** Displayed layout's resource ID. */
	private int textViewResourceId = 0;

	/**
	 * 
	 * Custom adapter for ArrayList of StaticTimezones objects.
	 * 
	 * @author meska.lt@gmail.com
	 * @author justinas.marcinka@gmail.com
	 * @param context
	 * @param textViewResourceId
	 * @param countries
	 * @since 2012-10-09
	 * @version 1.1
	 */
	public CountriesAdapter(Context context, int textViewResourceId, List<StaticTimezones> countries) {
		super(context, filterDuplicateEntries(countries));
		this.allItems = list;
		this.textViewResourceId = textViewResourceId;
	}

	private static List<StaticTimezones> filterDuplicateEntries(
			List<StaticTimezones> countries) {		
		ArrayList<StaticTimezones> filteredList =  new ArrayList<StaticTimezones>();
		
		if (!countries.isEmpty()) {
			for (int i = 1; i < countries.size(); i++) {
				StaticTimezones entry = countries.get(i);
				if (!entry.country.equalsIgnoreCase(countries.get(i - 1).country)) filteredList.add(entry); 
			}
		}
		return filteredList ;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String temp;
		if (convertView == null) {
			convertView = View.inflate(getContext(), textViewResourceId, null);
		}

		temp = list.get(position).country;
		((TextView) convertView).setText(temp);
		convertView.setTag(list.get(position).id);
			
//			if (position > 0 && list.get(position - 1).country.equals(temp)) {
//				convertView.setVisibility(View.GONE);
//			}

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
					if (items.get(i).country.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
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
