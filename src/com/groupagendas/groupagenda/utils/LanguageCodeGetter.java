package com.groupagendas.groupagenda.utils;

public class LanguageCodeGetter {
	
	public static String getLanguageCode(String language){
		if(language.equals("english")){
			return "en";
		}
		if(language.equals("dutch")){
			return "nl";
		}
		return "en";
	}
	
}
