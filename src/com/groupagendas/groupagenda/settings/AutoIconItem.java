package com.groupagendas.groupagenda.settings;

import java.util.regex.Pattern;

public class AutoIconItem {
	public int id;
	public String keyword;
	public Pattern keyPattern;
	public String context;
	public String icon;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
		this.keyPattern = Pattern.compile(keyword, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
}
