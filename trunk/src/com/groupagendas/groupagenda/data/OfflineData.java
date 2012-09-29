package com.groupagendas.groupagenda.data;

import java.util.Calendar;

import org.apache.http.entity.mime.MultipartEntity;

import com.groupagendas.groupagenda.utils.Utils;

public class OfflineData {
	private String location = "";
	private MultipartEntity unuploadedEntity = new MultipartEntity();
	private long created;
	
	public OfflineData(String location, MultipartEntity unuploadedEntity) {
		this.location = location;
		this.unuploadedEntity = unuploadedEntity;
		this.created = Utils.millisToUnixTimestamp(Calendar.getInstance().getTimeInMillis());
	}
	
	public OfflineData(String location, MultipartEntity unuploadedEntity, long created) {
		this.location = location;
		this.unuploadedEntity = unuploadedEntity;
		this.created = created;
	}

	public String getLocation() {
		return location;
	}
	
	public MultipartEntity getRequest() {
		return unuploadedEntity;
	}
	
	public long getCreated() {
		return created;
	}
}
