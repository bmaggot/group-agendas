package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import com.groupagendas.groupagenda.R;

/**
 * This class provides the widely used {@code StaticTimezone} list.<BR>
 * <BR>
 * Despite the previous code, the string resources are NOT locale dependent and thus
 * only need to be initialized once.
 * 
 * @author Tadas
 */
public final class TimezoneUtils {
	private static List<StaticTimezone> COUNTRIES_LIST = Collections.emptyList();
	
	private TimezoneUtils() {
		// utility class
	}
	
	public static List<StaticTimezone> getTimezones(Context context) {
		if (COUNTRIES_LIST.isEmpty()) {
			String[] cities = context.getResources().getStringArray(R.array.city);
			String[] countries = context.getResources().getStringArray(R.array.countries);
			String[] countries2 = context.getResources().getStringArray(R.array.countries2);
			String[] country_codes = context.getResources().getStringArray(R.array.country_codes);
			String[] timezones = context.getResources().getStringArray(R.array.timezones);
			String[] altnames = context.getResources().getStringArray(R.array.timezone_altnames);
			String[] call_codes = context.getResources().getStringArray(R.array.call_codes);
			
			List<StaticTimezone> list = new ArrayList<StaticTimezone>(cities.length);
			for (int i = 0; i < cities.length; i++) {
				StaticTimezone temp = new StaticTimezone();
	
				temp.id = StringValueUtils.valueOf(i);
				temp.city = cities[i];
				temp.country = countries[i];
				temp.country2 = countries2[i];
				temp.country_code = country_codes[i];
				temp.timezone = timezones[i];
				temp.altname = altnames[i];
				temp.call_code = call_codes[i];
	
				list.add(temp);
			}
			// prevent unauthorized activity
			COUNTRIES_LIST = Collections.unmodifiableList(list);
		}
		return COUNTRIES_LIST;
	}
	
	public static List<StaticTimezone> getTimezonesByCc(Context context, String country_code) {
		List<StaticTimezone> filtered = new LinkedList<StaticTimezone>();
		for (StaticTimezone st : getTimezones(context)) {
			if (st.country_code.equalsIgnoreCase(country_code))
				filtered.add(st);
		}
		return filtered;
	}
	
	public static class StaticTimezone {
		public String id;
		public String city;
		public String country;
		public String country2;
		public String country_code;
		public String timezone;
		public String altname;
		public String call_code;
	}
}
