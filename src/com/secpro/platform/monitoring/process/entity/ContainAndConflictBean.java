package com.secpro.platform.monitoring.process.entity;
/**
 * 策略包含和冲突检查结果bean
 * @author sxf
 *
 */
public class ContainAndConflictBean {
	private String cdate;
	private String containAndConflictInfo;
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	public String getContainAndConflictInfo() {
		return containAndConflictInfo;
	}
	public void setContainAndConflictInfo(String containAndConflictInfo) {
		this.containAndConflictInfo = containAndConflictInfo;
	}
	public long getResID() {
		return resID;
	}
	public void setResID(long resID) {
		this.resID = resID;
	}
	public String getTaskCode() {
		return taskCode;
	}
	public void setTaskCode(String taskCode) {
		this.taskCode = taskCode;
	}
	private long resID;
	private String taskCode;
}
