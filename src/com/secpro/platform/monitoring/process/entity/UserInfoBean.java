package com.secpro.platform.monitoring.process.entity;
/**
 * 用户信息bean
 * @author sxf
 *
 */
public class UserInfoBean {
	private long id;
	private String userName;
	private String mobelTel;
	private String enableAccount;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
	public String getEnableAccount() {
		return enableAccount;
	}
	public void setEnableAccount(String enableAccount) {
		this.enableAccount = enableAccount;
	}
	
}
