package com.groupagendas.groupagenda.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

import com.groupagendas.groupagenda.timezone.TimezoneProvider;

public class CountryManager {
	private CountryManager(){}
	
	public static ArrayList<String> countryValues = new ArrayList<String>();
	public static ArrayList<String> countryLabels = new ArrayList<String>();
	
	public static String[] getCountries(Context context) {
		Cursor result = getValues(context);
		
		if(result.moveToFirst()){
			countryValues.add("");
			countryLabels.add("");
			
			String c = "";
			while(!result.isAfterLast()){
				final String country = result.getString(result.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY));
				if(!c.equals(country)){
					c = country;
					countryValues.add(result.getString(result.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY_CODE)));
					countryLabels.add(country);
				}
				result.moveToNext();
			}
		}
		return countryLabels.toArray(new String[countryLabels.size()]);
	}
	
	public static String[] getCountryValues(Context context){
		
		if(countryValues.size() > 1){
			return countryValues.toArray(new String[countryValues.size()]);
		}else{
			Cursor result = getValues(context);
			
			if(result.moveToFirst()){
				countryValues.add("");
				countryLabels.add("");
				
				String c = "";
				while(!result.isAfterLast()){
					final String country = result.getString(result.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY));
					if(!c.equals(country)){
						c = country;
						countryValues.add(result.getString(result.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY_CODE)));
						countryLabels.add(country);
					}
					result.moveToNext();
				}
			}
			return countryValues.toArray(new String[countryValues.size()]);
		}
	}
	
	public static Cursor getValues(Context context){
		String order = TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY+" ASC";
		String[] projection = {TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY_CODE, TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY};
		return context.getContentResolver().query(TimezoneProvider.TMetaData.TimezoneMetaData.CONTENT_URI, projection, "", null, order);
	}
	
	public static String getCountryByTimezone(Context context, String timezone){
		String where = TimezoneProvider.TMetaData.TimezoneMetaData.TIMEZONE+"='"+timezone+"'";
		String[] projection = {TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY_CODE};
		
		Cursor cursor = context.getContentResolver().query(TimezoneProvider.TMetaData.TimezoneMetaData.CONTENT_URI, projection, where, null, null);
		if(cursor.moveToFirst()){
			return cursor.getString(cursor.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY_CODE));
		}else{
			return "";
		}
	}
		
}
