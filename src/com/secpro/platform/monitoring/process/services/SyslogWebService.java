package com.secpro.platform.monitoring.process.services;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class SyslogWebService {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(SyslogWebService.class);
	private static final String FAILED="0";
	private static final String SUCCESS="1";
	private static final String splitter="%%";
	public String reloadSyslogRule(@WebParam(name="value", targetNamespace = "http://demo/", mode = WebParam.Mode.IN)String value) {
		if(Assert.isEmptyString(value)){
			theLogger.error("the value is empty");
			return FAILED;
		}
		SyslogStandardRuleService syslogRuleService=ServiceHelper.findService(SyslogStandardRuleService.class);
		if(syslogRuleService==null){
			theLogger.error("Can't find the SyslogStandardRuleService!");
			return FAILED;
		}
		int separateIndex;
		if((separateIndex=value.indexOf(splitter))==-1){
			theLogger.error("Data format error");
			return FAILED;
		}
		String typeCode=value.substring(0, separateIndex);
		String oper=value.substring(separateIndex+2, value.length());
		if(oper.length()!=1){
			theLogger.error("the opertion format error");
			return FAILED;
		}
		if(syslogRuleService.reloadStandardRule(typeCode, oper)){
			theLogger.info("reload or remove the syslog standard rule success");
			return SUCCESS;
		}
		theLogger.info("reload or remove the syslog standard rule failed");
		return FAILED;
	}
}
