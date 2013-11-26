package com.secpro.platform.monitoring.process.entity;
/**
 * 事件bean
 * @author sxf
 *
 */
public class EventBean {
	private long id;
	private int eventLevel;
	private String message;
	private String cdate;
	private String confirmUser;
	private String confirmDate;
	private String clearUser;
	private String clearDate;
	private long resID;
	private long eventTypeID;
	
	public String getClearUser() {
		return clearUser;
	}
	public void setClearUser(String clearUser) {
		this.clearUser = clearUser;
	}
	public String getClearDate() {
		return clearDate;
	}
	public void setClearDate(String clearDate) {
		this.clearDate = clearDate;
	}
	
	
	
	public int getEventLevel() {
		return eventLevel;
	}
	public void setEventLevel(int eventLevel) {
		this.eventLevel = eventLevel;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	public String getConfirmUser() {
		return confirmUser;
	}
	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}
	public String getConfirmDate() {
		return confirmDate;
	}
	public void setConfirmDate(String confirmDate) {
		this.confirmDate = confirmDate;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getResID() {
		return resID;
	}
	public void setResID(long resID) {
		this.resID = resID;
	}
	public long getEventTypeID() {
		return eventTypeID;
	}
	public void setEventTypeID(long eventTypeID) {
		this.eventTypeID = eventTypeID;
	}
	
}
