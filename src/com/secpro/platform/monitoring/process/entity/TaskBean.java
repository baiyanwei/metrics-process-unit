package com.secpro.platform.monitoring.process.entity;
/**
 * 任务 状态更新bean
 * @author sxf
 *
 */
public class TaskBean {
	
	private String scheduleID;
	private long executeAt;
	private long executeCost;
	private int executeStatus;
	private String executeDes;
	
	public String getScheduleID() {
		return scheduleID;
	}
	public void setScheduleID(String scheduleID) {
		this.scheduleID = scheduleID;
	}
	
	public long getExecuteAt() {
		return executeAt;
	}
	public void setExecuteAt(long executeAt) {
		this.executeAt = executeAt;
	}
	public long getExecuteCost() {
		return executeCost;
	}
	public void setExecuteCost(long executeCost) {
		this.executeCost = executeCost;
	}
	public int getExecuteStatus() {
		return executeStatus;
	}
	public void setExecuteStatus(int executeStatus) {
		this.executeStatus = executeStatus;
	}
	public String getExecuteDes() {
		return executeDes;
	}
	public void setExecuteDes(String executeDes) {
		this.executeDes = executeDes;
	}
	
}
