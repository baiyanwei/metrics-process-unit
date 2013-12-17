package com.secpro.platform.monitoring.process.entity;
/**
 * kpi指标数据bean
 * @author sxf
 *
 */
public class KpiBean {
	private long resID;
	private long kpiID;
	private String cdate;
	private String kpiValue;
	//private float valueInt;
	
	public long getResID() {
		return resID;
	}
	public void setResID(long resID) {
		this.resID = resID;
	}
	public long getKpiID() {
		return kpiID;
	}
	public void setKpiID(long kpiID) {
		this.kpiID = kpiID;
	}
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	public String getKpiValue() {
		return kpiValue;
	}
	public void setKpiValue(String kpiValue) {
		this.kpiValue = kpiValue;
	}
	
}
