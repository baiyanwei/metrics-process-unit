package com.secpro.platform.monitoring.process.entity;
/**
 * 基线比对结果bean
 * @author sxf
 *
 */
public class BaselineMatchBean {
	private String matchResult;
	private String result;
	private String cdate;
	private long resID;
	private long baselineID;
	private String taskCode;
	public String getMatchResult() {
		return matchResult;
	}
	public void setMatchResult(String matchResult) {
		this.matchResult = matchResult;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
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
	public long getBaselineID() {
		return baselineID;
	}
	public void setBaselineID(long baselineID) {
		this.baselineID = baselineID;
	}
	public String getTaskCode() {
		return taskCode;
	}
	public void setTaskCode(String taskCode) {
		this.taskCode = taskCode;
	}
}
