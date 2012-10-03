package com.groupagendas.groupagenda.events;


public class Invited {
	public static final int REJECTED = 0;
	public static final int ACCEPTED = 1;
	public static final int MAYBE = 2;
	public static final int NEW_INVITATION = 4;

	/** Invited person's fullname */
	private String name;
	
	/** Invited person's id (if person exists in user's contact list) */
	private int my_contact_id;
	
	/** Invited person's id (if person exists in other user's contact list
	 * and is not registered in GroupAgendas) */
	private int gcid;
	
	/** Invited person's GroupAgendas user id */
	private int guid;
	
	/** Invitation status */
	private int status;

	public String getName() {
		if (name != null)
			return name;
		else
			return "";
	}
	
	public int getMy_contact_id() {
		return my_contact_id;
	}

	public int getGcid() {
		return gcid;
	}

	public int getGuid() {
		return guid;
	}

	public int getStatus() {
		if (status >= 0 && status < 5 && status != 3)
			return status;
		else
			return 4;			
	}
	
	public void setName(String name) {
		if (name != null)
			this.name = name;
		else
			this.name = "";
	}

	public void setMy_contact_id(int my_contact_id) {
		if (my_contact_id > 0)
			this.my_contact_id = my_contact_id;
	}

	public void setGcid(int gcid) {
		if (gcid > 0)
			this.gcid = gcid;
	}

	public void setGuid(int guid) {
		if (guid > 0)
			this.guid = guid;
	}

	public void setStatus(int status) {
		if (status >= 0 && status < 5 && status != 3)
			this.status = status;
		else
			this.status = 4;
	}
	
	public Invited() {
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("gname").append(":").append(getName()).append(",");
		sb.append("gcid").append(":").append(getGcid()).append(",");
		sb.append("guid").append(":").append(getGuid()).append(",");
		sb.append("status").append(":").append(getStatus()).append(",");
		sb.append("my_contact_id").append(":").append(getMy_contact_id());
		
		return sb.toString();
	}
}