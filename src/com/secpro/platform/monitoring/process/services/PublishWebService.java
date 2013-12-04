package com.secpro.platform.monitoring.process.services;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.ws.Endpoint;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
@ServiceInfo(description = "publish webservice", configurationPath = "dpu/services/PublishWebService/")
public class PublishWebService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(PublishWebService.class);
	@XmlElement(name = "publishURL", defaultValue ="")
	public String publishURL="";
	@Override
	public void start() throws Exception {
		publishWebService();
		theLogger.info("startUp");
	}

	private void publishWebService() {
		if(Assert.isEmptyString(publishURL)){
			return;
		}
		Endpoint.publish(publishURL, new SyslogWebService()); 
		theLogger.info("pulishSuccess",publishURL);
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
