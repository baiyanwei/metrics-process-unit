package com.secpro.platform.monitoring.process.sms;

public interface ISMSAlarm {
	public boolean sendSMS(String telNum,String sendMess);

}
