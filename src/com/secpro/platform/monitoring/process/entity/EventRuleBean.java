package com.secpro.platform.monitoring.process.entity;
/**
 * 事件规则bean
 * @author sxf
 *
 */
public class EventRuleBean {
	private long id;
	private int enentLevel;
	private String thresholdValue;
	private String thresholdOpr;
	private String setMsg;
	private String recoverSetMsg;
	private String repeat;
	private long resID;
	private long eventTypeID;
	private String eventRecover;
	public String getEventRecover() {
		return eventRecover;
	}
	public void setEventRecover(String eventRecover) {
		this.eventRecover = eventRecover;
	}
	
	
	public int getEnentLevel() {
		return enentLevel;
	}
	public void setEnentLevel(int enentLevel) {
		this.enentLevel = enentLevel;
	}
	public String getThresholdValue() {
		return thresholdValue;
	}
	public void setThresholdValue(String thresholdValue) {
		this.thresholdValue = thresholdValue;
	}
	public String getThresholdOpr() {
		return thresholdOpr;
	}
	public void setThresholdOpr(String thresholdOpr) {
		this.thresholdOpr = thresholdOpr;
	}
	public String getSetMsg() {
		return setMsg;
	}
	public void setSetMsg(String setMsg) {
		this.setMsg = setMsg;
	}
	public String getRecoverSetMsg() {
		return recoverSetMsg;
	}
	public void setRecoverSetMsg(String recoverSetMsg) {
		this.recoverSetMsg = recoverSetMsg;
	}
	public String getRepeat() {
		return repeat;
	}
	public void setRepeat(String repeat) {
		this.repeat = repeat;
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
