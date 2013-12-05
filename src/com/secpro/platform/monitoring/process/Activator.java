package com.secpro.platform.monitoring.process;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.services.FileStorageService;
import com.secpro.platform.monitoring.process.services.MCAStatusMonitoringService;
import com.secpro.platform.monitoring.process.services.ProcessChainService;
import com.secpro.platform.monitoring.process.services.PublishWebService;
import com.secpro.platform.monitoring.process.services.SMSAlarmService;
import com.secpro.platform.monitoring.process.services.ScanFWConfigurationFileService;
import com.secpro.platform.monitoring.process.services.SyslogHitPolicyService;
import com.secpro.platform.monitoring.process.services.SyslogStandardRuleService;


public class Activator implements BundleActivator {

	private static BundleContext context;
	private static PlatformLogger theLogger = PlatformLogger.getLogger(Activator.class);
	

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		theLogger.info("start the metrics process unit");
		this.context = bundleContext;
		
		registerServices();
		theLogger.info("The metrics process unit started completed");
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		unRegisterServices();
		Activator.context = null;
	}
	/**
	 * 注销服务
	 */
	private void unRegisterServices() throws Exception{
		//注销数据处理链服务
		ProcessChainService processChain=ServiceHelper.findService(ProcessChainService.class);
		if(processChain!=null){

			ServiceHelper.unregisterService(processChain);
		}
		//注销syslog标准化规则服务
		SyslogStandardRuleService syslogStandardRule=ServiceHelper.findService(SyslogStandardRuleService.class);
		if(syslogStandardRule!=null){

			ServiceHelper.unregisterService(syslogStandardRule);
		}
		//注销syslog命中服务
		SyslogHitPolicyService syslogHit=ServiceHelper.findService(SyslogHitPolicyService.class);
		if(syslogHit!=null){

			ServiceHelper.unregisterService(syslogHit);
		}
		//注销存储文件服务
		FileStorageService fileStorage=ServiceHelper.findService(FileStorageService.class);
		if(fileStorage!=null){

			ServiceHelper.unregisterService(fileStorage);
		}
		//注销发送短信告警服务
		SMSAlarmService smsAlarm=ServiceHelper.findService(SMSAlarmService.class);
		if(smsAlarm!=null){

			ServiceHelper.unregisterService(smsAlarm);
		}
		MCAStatusMonitoringService mcaMonitor=ServiceHelper.findService(MCAStatusMonitoringService.class);
		if(mcaMonitor!=null){

			ServiceHelper.unregisterService(mcaMonitor);
		}
		ScanFWConfigurationFileService scanFileService=ServiceHelper.findService(ScanFWConfigurationFileService.class);
		if(mcaMonitor!=null){

			ServiceHelper.unregisterService(scanFileService);
		}
	}

	/**
	 * 注册服务
	 * @throws Exception
	 */
	private void registerServices() throws Exception{
		//数据处理链服务
		ServiceHelper.registerService(new ProcessChainService());
		//syslog标准化规则服务
		ServiceHelper.registerService(new SyslogStandardRuleService());
		//syslog与策略信息命中服务
		ServiceHelper.registerService(new SyslogHitPolicyService());
		//文件存储服务
		ServiceHelper.registerService(new FileStorageService());
		//告警短信服务
		ServiceHelper.registerService(new SMSAlarmService());
		ServiceHelper.registerService(new PublishWebService());
		//MCA状态监控服务
		ServiceHelper.registerService(new MCAStatusMonitoringService());
		//扫描防火墙配置文件服务
		ServiceHelper.registerService(new ScanFWConfigurationFileService());
	}

}
