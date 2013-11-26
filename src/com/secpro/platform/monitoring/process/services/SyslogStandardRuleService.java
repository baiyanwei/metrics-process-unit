package com.secpro.platform.monitoring.process.services;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.ISyslogDao;
import com.secpro.platform.monitoring.process.dao.impl.SyslogDao;
/**
 * 加载数据库中关于syslog标准化所需的数据
 * 
 * @author sxf
 *
 */
@ServiceInfo(description = "syslog standard rule service", configurationPath = "dpu/services/SyslogStandardRuleService/")
public class SyslogStandardRuleService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(SyslogStandardRuleService.class);
	//存储标准化后元素与数据库表字段映射关系
	private Map<String,String> _dataDBMapping=new HashMap<String,String>();
	//syslog标准化规则
	private Map<String, Map<String, Map<String,String>>> _ruleMapping=new HashMap<String, Map<String, Map<String,String>>>();
	public Map<String, String> get_dataDBMapping() {
		return _dataDBMapping;
	}
	public Map<String, Map<String, Map<String, String>>> get_ruleMapping() {
		return _ruleMapping;
	}
	@Override
	public void start() throws Exception {
		//加载syslog标准化规则
		loadStandardRule();
		//加载syslog解析字段与数据库表字段对应规则
		loadDataToDBMapping();
		theLogger.info("startUp");
		
	}

	
	@Override
	public void stop() throws Exception {
		theLogger.info("stopped");
	}
	/**
	 * 从数据库中加载syslog标准化规则
	 */
	private void loadStandardRule(){
		ISyslogDao syslogDao=new SyslogDao();
		synchronized(_ruleMapping){ 
			syslogDao.syslogRuleMappingQuery(_ruleMapping);
		}
	}
	/**
	 * 加载typeCode对应的syslog标准化规则
	 * @param typeCode
	 */
	public void loadStandardRule(String typeCode){
		if (Assert.isEmptyString(typeCode)) {
			return;
		}
		ISyslogDao syslogDao=new SyslogDao();
		Map<String,Map<String,String>> ruleNameMapping=syslogDao.syslogRuleMappingQuery(typeCode);
		synchronized(_ruleMapping){ 
			_ruleMapping.put(typeCode, ruleNameMapping);
		}
	}
	/**
	 * 修改typeCode对应的syslog标准化规则
	 * @param typeCode
	 */
	public void changedStandarRule(String typeCode){
		loadStandardRule(typeCode);
	}
	/**
	 * 删除typeCode对应的syslog标准化规则
	 * @param typeCode
	 */
	public void removeStandarRule(String typeCode){
		if (Assert.isEmptyString(typeCode)) {
			return;
		}
		synchronized(_ruleMapping){ 
			_ruleMapping.remove(typeCode);
		}
	}
	/**
	 * 加载数据库中的syslog解析名称与数据库中表字段的映射关系
	 */
	public void loadDataToDBMapping(){
		ISyslogDao syslogDao=new SyslogDao();
		synchronized(_dataDBMapping){ 
			syslogDao.syslogDBMappingQuery(_dataDBMapping);
		}
	}
	

}
