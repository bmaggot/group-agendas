package com.groupagendas.groupagenda.address;

public class Address {
	private int id;
	private int id_internal;
	private int user_id;
	private String title;
	private String street;
	private String city;
	private String zip;
	private String state;
	private String country;
	private String timezone;
	private String country_name;
	private boolean uploadedToServer = true;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIdInternal() {
		return id_internal;
	}
	public void setIdInternal(int id) {
		this.id_internal = id;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public String getCountry_name() {
		return country_name;
	}
	public void setCountry_name(String country_name) {
		this.country_name = country_name;
	}
	public boolean isUploadedToServer() {
		return uploadedToServer;
	}
	public void setUploadedToServer(boolean bool) {
		uploadedToServer = bool;
	}
}
