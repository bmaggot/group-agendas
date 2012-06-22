package com.groupagendas.groupagenda.timezone;

import android.content.Context;
import android.database.Cursor;

public class TimezoneManager {
	
	public static String[] timezoneValues = null;
	public static String[] timezoneLabels = null;
	
	public static String[] getTimezones(Context context, String countryCode){
		
		Cursor result = getValues(context, countryCode);
		
		if(result.moveToFirst()){
			timezoneValues = new String[result.getCount()];
			timezoneLabels = new String[result.getCount()];
			int index = 0;
			while(!result.isAfterLast()){
				
				timezoneValues[index] = result.getString(result.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.TIMEZONE));
				timezoneLabels[index] = result.getString(result.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.ALTNAME));
				
				index++;
				result.moveToNext();
			}
		}
		
		return timezoneLabels;
	}
	
	public static String[] getTimezonesValues(Context context, String countryCode){
		if(timezoneValues != null){
			return timezoneValues;
		}else{
			Cursor result = getValues(context, countryCode);
			if(result.moveToFirst()){
				timezoneValues = new String[result.getCount()];
				int index = 0;
				while(!result.isAfterLast()){
					
					timezoneValues[index] = result.getString(result.getColumnIndex(TimezoneProvider.TMetaData.TimezoneMetaData.TIMEZONE));
					
					index++;
					result.moveToNext();
				}
			}
			return timezoneValues;
		}
	}
	
	public static Cursor getValues(Context context, String countryCode){
		String where = TimezoneProvider.TMetaData.TimezoneMetaData.COUNTRY_CODE+"= '"+countryCode+"'";
		return context.getContentResolver().query(TimezoneProvider.TMetaData.TimezoneMetaData.CONTENT_URI, null, where, null, null);
	}
}
