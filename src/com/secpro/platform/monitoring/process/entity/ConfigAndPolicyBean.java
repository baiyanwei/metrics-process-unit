package com.secpro.platform.monitoring.process.entity;
/**
 * 策略和配置数据bean
 * @author sxf
 *
 */
public class ConfigAndPolicyBean {
	private String cdate;
	private String configAndPolicyInfo;
	private long resID;
	private String taskCode;
	public String getTaskCode() {
		return taskCode;
	}
	public void setTaskCode(String taskCode) {
		this.taskCode = taskCode;
	}
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	public String getConfigAndPolicyInfo() {
		return configAndPolicyInfo;
	}
	public void setConfigAndPolicyInfo(String configAndPolicyInfo) {
		this.configAndPolicyInfo = configAndPolicyInfo;
	}
	public long getResID() {
		return resID;
	}
	public void setResID(long resID) {
		this.resID = resID;
	}
	
}
