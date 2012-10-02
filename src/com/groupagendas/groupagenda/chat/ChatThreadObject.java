package com.groupagendas.groupagenda.chat;

public class ChatThreadObject {
	private int event_id;
	private int contact_author_id;
	private int message_count;
	private String message_last;
	private String title;
	private long timeStart;
	private int new_messages;
	private int successfully_uploaded = 0;
	private long created;
	private long modified;

	/**
	 * @return the event_id
	 */
	public int getEvent_id() {
		return event_id;
	}

	/**
	 * @param event_id
	 *            the event_id to set
	 */
	public void setEvent_id(int event_id) {
		this.event_id = event_id;
	}

	/**
	 * @return the contact_author_id
	 */
	public int getContact_author_id() {
		return contact_author_id;
	}

	/**
	 * @param contact_author_id
	 *            the contact_author_id to set
	 */
	public void setContact_author_id(int contact_author_id) {
		this.contact_author_id = contact_author_id;
	}

	/**
	 * @return the message_count
	 */
	public int getMessage_count() {
		return message_count;
	}

	/**
	 * @param message_count
	 *            the message_count to set
	 */
	public void setMessage_count(int message_count) {
		this.message_count = message_count;
	}

	/**
	 * @return the message_last
	 */
	public String getMessage_last() {
		return message_last;
	}

	/**
	 * @param message_last
	 *            the message_last to set
	 */
	public void setMessage_last(String message_last) {
		this.message_last = message_last;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the timeStart
	 */
	public long getTimeStart() {
		return timeStart;
	}

	/**
	 * @param timeStart
	 *            the timeStart to set
	 */
	public void setTimeStart(long timeStart) {
		this.timeStart = timeStart;
	}

	/**
	 * @return the new_messages
	 */
	public int getNew_messages() {
		return new_messages;
	}

	/**
	 * @param new_messages
	 *            the new_messages to set
	 */
	public void setNew_messages(int new_messages) {
		this.new_messages = new_messages;
	}

	/**
	 * @return the successfully_uploaded
	 */
	public int getSuccessfully_uploaded() {
		return successfully_uploaded;
	}

	/**
	 * @param successfully_uploaded
	 *            the successfully_uploaded to set
	 */
	public void setSuccessfully_uploaded(int successfully_uploaded) {
		this.successfully_uploaded = successfully_uploaded;
	}

	/**
	 * @return the created
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * @param created
	 *            the created to set
	 */
	public void setCreated(long created) {
		this.created = created;
	}

	/**
	 * @return the modified
	 */
	public long getModified() {
		return modified;
	}

	/**
	 * @param modified
	 *            the modified to set
	 */
	public void setModified(long modified) {
		this.modified = modified;
	}
}
