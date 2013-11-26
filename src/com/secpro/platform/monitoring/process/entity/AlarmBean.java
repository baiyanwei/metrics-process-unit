package com.secpro.platform.monitoring.process.entity;
/**
 * 告警bean
 * @author sxf
 *
 */
public class AlarmBean {
	private long id;
	private String cdate;
	private String userName;
	private String mobelTel;
	private String messgae;
	private String sendMsgStatus;
	private String sendDate;
	public String getSendDate() {
		return sendDate;
	}
	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSendMsgStatus() {
		return sendMsgStatus;
	}
	public void setSendMsgStatus(String sendMsgStatus) {
		this.sendMsgStatus = sendMsgStatus;
	}
	public String getCdate() {
		return cdate;
	}
	public void setCdate(String cdate) {
		this.cdate = cdate;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getMobelTel() {
		return mobelTel;
	}
	public void setMobelTel(String mobelTel) {
		this.mobelTel = mobelTel;
	}
	public String getMessgae() {
		return messgae;
	}
	public void setMessgae(String messgae) {
		this.messgae = messgae;
	}
}
