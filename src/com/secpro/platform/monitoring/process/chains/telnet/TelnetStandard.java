package com.secpro.platform.monitoring.process.chains.telnet;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.other.ReadRuleFile;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.standard.ConfigAndPolicyStandard;
import com.secpro.platform.monitoring.process.chains.ref.task.TaskCompleted;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.impl.ConfigAndPolicyDao;
/**
 * 标准化采集的结果
 * 对telnet采集回来的配置和策略信息进行标准化处理
 * 并更新任务状态等相关信息
 * @author sxf
 *
 */
public class TelnetStandard implements IDataProcessChain{

	private static PlatformLogger theLogger = PlatformLogger.getLogger(TelnetStandard.class);
	private int chainID=0;
	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("telnet dataProcess chain ID: "+getChainID());
		if (rawData == null) {
			theLogger.error(
					"invalid rawData in telnet data processing.");
			return null;
		}
		if(!rawData.getClass().equals(HashMap.class)){
			theLogger.error("need type of HashMap in telnet data processing.");
			return null;
		}
		Map telnetData=(Map)rawData;
//		String taskCode=(String)telnetData.get(MetaDataConstant.TASK_CODE);
//		if(Assert.isEmptyString(taskCode))
//		{
//			theLogger.error("invalid taskCode in telnet data processing.");
//			return null;
//		}
//		String executeDate=(String)telnetData.get(MetaDataConstant.EXECUTE_DATE);
		//更新任务状态
		setTaskStatus(telnetData,TaskCompleted.TASK_SUCCESS);
		String cityCode=(String) telnetData.get(MetaDataConstant.CITY_CODE);
		String targetIP=(String) telnetData.get(MetaDataConstant.TARGET_IP);
		if(Assert.isEmptyString(cityCode)||Assert.isEmptyString(targetIP)){
			theLogger.error("city code or target IP is empty.");
			return null;
		}
		String excuteResult=(String) telnetData.get(MetaDataConstant.EXECUTE_RESULT);
		if(Assert.isEmptyString(excuteResult)){
			theLogger.error("the excute result is empty");
			return null;
		}
		String rules=loadConfigAndPolicyRule(cityCode,targetIP);
		if(Assert.isEmptyString(rules)){
			theLogger.debug("the standard rules are empty");
			return null;
		}
		ConfigAndPolicyStandard config=new ConfigAndPolicyStandard(excuteResult,rules,cityCode,targetIP);
		String[] result=config.configAndPolicyStandard();
		if(result==null){
			theLogger.error("the standard data of telnet are empty!");
			return null;
		}
		telnetData.put(MetaDataConstant.EXECUTE_RESULT, result);
		return telnetData;
	}
	/**
	 * 查询策略和配置信息的标准化规则
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private String loadConfigAndPolicyRule(String cityCode, String targetIP) {
		IConfigAndPolicyDao configAndPolicyDao=new ConfigAndPolicyDao();
		String rulePath=configAndPolicyDao.standardRulePathQuery(cityCode, targetIP);
		return ReadRuleFile.readRule(rulePath);
		
	}

	@Override
	public void setChainID(int chainID) {
		this.chainID=chainID;
		
	}

	@Override
	public int getChainID() {
		// TODO Auto-generated method stub
		return this.chainID;
	}
	/**
	 * 更新任务状态等相关信息
	 * @param taskCode
	 * @param executeDate
	 */
	private void setTaskStatus(Map<String,Object> data,int executeResult) {
		Thread setTaskStatus = new TaskCompleted(data, executeResult);
		setTaskStatus.start();
	}

}