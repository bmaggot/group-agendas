package com.groupagendas.groupagenda.data;

import org.apache.http.entity.mime.MultipartEntity;

public class OfflineData {
	private String location = "";
	private MultipartEntity unuploadedEntity = new MultipartEntity();
	
	public OfflineData (String location, MultipartEntity unuploadedEntity) {
		this.location = location;
		this.unuploadedEntity = unuploadedEntity;
	}
	
	public String getLocation () {
		return this.location;
	}
	
	public MultipartEntity getRequest () {
		return this.unuploadedEntity;
	}
}
