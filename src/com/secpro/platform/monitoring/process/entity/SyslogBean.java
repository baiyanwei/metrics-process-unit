package com.secpro.platform.monitoring.process.entity;

import java.util.Map;
/**
 * syslog bean
 * @author sxf
 *
 */
public class SyslogBean {
	private String syslogName;
	private Map<String,String> resultMapping;
	private String rdate;
	private String edate;
	private String oriSyslog;
	private long resID;
	public String getSyslogName() {
		return syslogName;
	}
	public void setSyslogName(String syslogName) {
		this.syslogName = syslogName;
	}
	public Map<String, String> getResultMapping() {
		return resultMapping;
	}
	public void setResultMapping(Map<String, String> resultMapping) {
		this.resultMapping = resultMapping;
	}
	public String getRdate() {
		return rdate;
	}
	public void setRdate(String rdate) {
		this.rdate = rdate;
	}
	public String getEdate() {
		return edate;
	}
	public void setEdate(String edate) {
		this.edate = edate;
	}
	public String getOriSyslog() {
		return oriSyslog;
	}
	public void setOriSyslog(String oriSyslog) {
		this.oriSyslog = oriSyslog;
	}
	public long getResID() {
		return resID;
	}
	public void setResID(long resID) {
		this.resID = resID;
	}
	

}
