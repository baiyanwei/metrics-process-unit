package com.secpro.platform.monitoring.process.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.match.PolicyMatchUtil;
import com.secpro.platform.monitoring.process.chains.ref.standard.StandardConstant;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.ISyslogDao;
import com.secpro.platform.monitoring.process.dao.impl.ConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.impl.SyslogDao;
import com.secpro.platform.monitoring.process.utils.DateFormat;
/**
 * 根据配置文件定义的间隔时间，计算syslog与策略信息的命中
 * @author sxf
 *
 */
@ServiceInfo(description = "syslog hit policy service", configurationPath = "dpu/services/SyslogHitPolicyService/")
public class SyslogHitPolicyService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(SyslogHitPolicyService.class);
		//syslog与策略信息命中间隔时间
		@XmlElement(name = "executeHitTimer",type=Long.class, defaultValue ="86400000")
		public long executeHitTimer=24*60*60*1000L;
		//进行syslog命中时，预设的syslog数据格式
		@XmlElement(name = "syslogHitDataFormat", defaultValue ="")
		public String syslogHitDataFormat="";
		//syslog与策略信息命中的规则
		@XmlElement(name = "syslogHitMatchRule", defaultValue ="action::=&srcip::<=&dstip::<=&service::<=")
		public String syslogHitMatchRule="";
		private Thread syslogHitThread;
		//syslog数据格式中的各名称顺序
		private List<String> hitRuleOrder;
		//行分隔符
		private String splitter="%%";
	@Override
	public void start() throws Exception {
		//启动syslog与策略信息命中
		startUpSyslogHit();
		theLogger.info("startUp");
	}
	@Override
	public void stop() throws Exception {
		if(syslogHitThread!=null){
			syslogHitThread.interrupt();
		}
		theLogger.info("stopped");
	}
	/**
	 * 启动syslog命中率计算
	 */
	private void startUpSyslogHit() {
		//没有配置syslog数据格式，或者syslog命中规则，则不启动命中线程
		if(Assert.isEmptyString(syslogHitDataFormat)||Assert.isEmptyString(syslogHitMatchRule)||executeHitTimer==0){
			return;
		}
		if(analyzeSyslogHitDataFormat()){
			syslogHitThread=new Thread() {
				public void run() {
					try {
						while(true){
							sleep(executeHitTimer);
							//进行syslog命中
							excuteSyslogHit();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						theLogger.exception(e);
					}

				}


			};
			//启动线程
			syslogHitThread.start();
		}
	}
	/**
	 * 进行syslog命中
	 */
	private void excuteSyslogHit() {
		//查询数据中需要进行命中计算的syslog日志
		List<String[]> syslogs=loadNeedHitSyslogs();
		if(syslogs==null||syslogs.size()==0){
			theLogger.debug("syslog data no need to hit!");
			return;
		}
		else{
			long endDate=System.currentTimeMillis();
			//此次命中的起始与终止时间
			long startDate=endDate-executeHitTimer;
			long resIDBefore=0;
			String policyValues ="";
			/*
			 * 外层Map：Long类型存储resid
			 * 内层Map：String类型存储命中后原始策略数据。Long类型存储此策略数据被命中次数
			 */
			Map<Long,Map<String,Long>> hitResult=new HashMap<Long,Map<String,Long>>();
			for(String[] syslog:syslogs){
				if(syslog==null){
					continue;
				}
				String resIDStr=syslog[syslog.length-1];
				if(!resIDStr.matches("\\d+")){
					continue;
				}
				long resID=Long.parseLong(resIDStr);
				if(resIDBefore==0||resID!=resIDBefore){
					//查询resid对应的策略信息
					policyValues=getPolicyInfo(resID);
					resIDBefore=resID;
				}
				if(Assert.isEmptyString(policyValues)){
					continue;
				}
				String[] policys=policyValues.split(splitter);
				//将数据库查询到的syslog数据格式化成需要的比对格式类型
				String syslogValue=MessageFormat.format(syslogHitDataFormat, syslog);
				for(int i=0;i<policys.length;i++){
					int matchResult=PolicyMatchUtil.matchTwoValue(syslogValue,policys[i],syslogHitMatchRule);
					//syslog命中某条策略信息
					if(matchResult==1){
						String policyOrigin=PolicyMatchUtil.getNameValue(policys[i],StandardConstant.ORIGIN_NAME);
						if(Assert.isEmptyString(policyOrigin)){
							continue;
						}
						//未创建此resid的结果，则创建
						if(hitResult.get(resID)==null){
							Map<String,Long> resIDHitResult=new HashMap<String,Long>();
							resIDHitResult.put(policyOrigin,1l);
							hitResult.put(resID, resIDHitResult);
						}else{
							Map<String ,Long> resIDHitResult=hitResult.get(resID);
							if(resIDHitResult.get(policyOrigin)==null){
								resIDHitResult.put(policyOrigin, 1l);
							}else{
								resIDHitResult.put(policyOrigin, resIDHitResult.get(policyOrigin)+1l);
							}
						}
						break;
					}
				}

			}
			//计算完命中后，如若有结果，则存入数据库中
			if(hitResult.size()>0){
				syslogHitStorage(DateFormat.timestampFormat(startDate),DateFormat.timestampFormat(endDate),hitResult);
			}

		}
		
	}
	/**
	 * 将命中结果存入数据库
	 * @param startDate 此次命中时间区间，起始时间
	 * @param endDate 此次命中时间区间，结束时间
	 * @param hitResult 命中结果
	 */
	private void syslogHitStorage(String startDate,
			String endDate, Map<Long, Map<String, Long>> hitResult) {
		ISyslogDao syslogDao=new SyslogDao();
		syslogDao.syslogHitSave(startDate, endDate,hitResult);
		
	}
	/**
	 * 查询resID对应的策略数据
	 * @param resID
	 * @return
	 */
	private String getPolicyInfo(long resID) {
		
		IConfigAndPolicyDao configAndPolicyDao=new ConfigAndPolicyDao();
		String configAndPolicyInfo=configAndPolicyDao.configAndPolicyQuery(resID);
		if(Assert.isEmptyString(configAndPolicyInfo)){
			return null;
		}
		/*
		 * 查询回来的为配置以及策略信息，
		 * 格式为#configuration#防火墙配置信息#configuration##policy#防火墙策略信息#policy#
		 */
		String policyRulesRegex="#policy#(.*?)#policy#";
		Pattern pattern = Pattern.compile(policyRulesRegex);
		Matcher mat = pattern.matcher(configAndPolicyInfo);
		if (mat.find()) {
			return mat.group(1);
		 }
		return null;
	}
	/**
	 * 查询需要进行命中计算的syslog数据
	 * @return
	 */
	private List<String[]> loadNeedHitSyslogs() {
		ISyslogDao syslogDao=new SyslogDao();
		return syslogDao.syslogQueryAndUpdate(hitRuleOrder);
	}
	/**
	 * 解析syslog命中数据格式，返回false解析失败，true解析成功
	 * @return
	 */
	private boolean analyzeSyslogHitDataFormat() {
		String analyzeRegex="\\{(\\w+)\\}";
		Pattern patt=Pattern.compile(analyzeRegex);
		Matcher mat=patt.matcher(syslogHitDataFormat);
		int i=0;
		StringBuilder hitRule=new StringBuilder();
		List<String> order=new ArrayList<String>();
		int start=0;
		int end=0;
		SyslogStandardRuleService syslogStandard=ServiceHelper.findService(SyslogStandardRuleService.class);
		if(syslogStandard==null){
			theLogger.error("Can't find the Syslog Standard Rule Service!");
			return false;
		}
		Map<String, String> dataDBMapping=syslogStandard.get_dataDBMapping();
		if(dataDBMapping==null||dataDBMapping.size()==0){
			theLogger.error("the syslog database mapping are empty!");
			return false;
		}
		while(mat.find()){
			start=mat.start();
			String value=mat.group(1);
			if(Assert.isEmptyString(value)){
				return false;
			}
			
			String mappingValue=dataDBMapping.get(value);
			if(Assert.isEmptyString(mappingValue)){
				return false;
			}
			order.add(mappingValue);
			
			hitRule.append(syslogHitDataFormat.substring(end,start)+"{"+i+"}");
			end=mat.end();
			i++;
		}
		if(!Assert.isEmptyString(hitRule.toString())){
			hitRule.append(syslogHitDataFormat.substring(end,syslogHitDataFormat.length()));
			hitRuleOrder=order;
			syslogHitDataFormat=hitRule.toString();
			return true;
		}
		return false;
		
	}
	

}
