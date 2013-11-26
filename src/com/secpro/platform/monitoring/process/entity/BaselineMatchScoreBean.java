package com.secpro.platform.monitoring.process.entity;
/**
 * 基线比对总分bean
 * @author sxf
 *
 */
public class BaselineMatchScoreBean {
	private int totalScore;
	private String cdate;
	private long resID;
	private String taskCode;
	public int getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
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
}
