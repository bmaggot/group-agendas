package com.groupagendas.groupagenda.alarm;

public class Alarm {
	private String alarm_id;
	private int userId;
	private int eventId;
	private long alarmTimestamp;
	private long offset;
	private boolean sent;
	
	public String getAlarm_id() {
		return alarm_id;
	}
	public void setAlarm_id(String alarm_id) {
		this.alarm_id = alarm_id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getEventId() {
		return eventId;
	}
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	public long getAlarmTimestamp() {
		return alarmTimestamp;
	}
	public void setAlarmTimestamp(long alarmTimestamp) {
		this.alarmTimestamp = alarmTimestamp;
	}
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	public boolean isSent() {
		return sent;
	}
	public void setSent(boolean sent) {
		this.sent = sent;
	}
}
