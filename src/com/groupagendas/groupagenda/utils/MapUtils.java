package com.groupagendas.groupagenda.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.groupagendas.groupagenda.error.report.Reporter;

public class MapUtils {
	public static String mapToString(Context context, Map<String, String> map) {
		
		if(map == null){
			return null;
		}
		
		StringBuilder stringBuilder = new StringBuilder();

		for (String key : map.keySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
			}
			String value = map.get(key);
			try {
				stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
				stringBuilder.append("=");
				stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				Reporter.reportError(context, MapUtils.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}
		return stringBuilder.toString();
	}

	public static Map<String, String> stringToMap(Context context, String input) {
		Map<String, String> map = new HashMap<String, String>();
		String[] nameValuePairs = input.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
			try {
				if (nameValue.length > 1)
					map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				Reporter.reportError(context, MapUtils.class.toString(), Thread.currentThread().getStackTrace()[2].getMethodName().toString(), e.getMessage());
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}

		return map;
	}
}
