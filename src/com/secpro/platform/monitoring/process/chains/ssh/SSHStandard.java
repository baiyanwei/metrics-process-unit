package com.secpro.platform.monitoring.process.chains.ssh;

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
 * 对SSH采集回来的配置和策略信息进行标准化处理
 * 并更新任务状态等相关信息
 * @author sxf
 *
 */
public class SSHStandard implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SSHStandard.class);
	private int chainID = 0;

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("ssh dataProcess chain ID: " + getChainID() + " name:"
				+ this.getClass().getName());
		if (rawData == null) {
			theLogger.error("invalid rawData in ssh data processing.");
			return null;
		}
		if (!rawData.getClass().equals(HashMap.class)) {
			theLogger.error("need type of HashMap in SSH data processing.");
			return null;
		}
		Map sshData = (Map) rawData;
		// String taskCode=(String)sshData.get(MetaDataConstant.TASK_CODE);
		// if(Assert.isEmptyString(taskCode))
		// {
		// theLogger.error("invalid taskCode in SSH data processing.");
		// return null;
		// }
		// String
		// executeDate=(String)sshData.get(MetaDataConstant.EXECUTE_DATE);
		// 更新任务状态
		setTaskStatus(sshData, TaskCompleted.TASK_SUCCESS);
		long resID = (Long) sshData.get(MetaDataConstant.RESOURCE_ID);
		if (resID==0L) {
			theLogger.error("res id is empty.");
			return null;
		}
		String excuteResult = (String) sshData
				.get(MetaDataConstant.EXECUTE_RESULT);
		if (Assert.isEmptyString(excuteResult)) {
			theLogger.error("the excute result is empty");
			return null;
		}
		String rules = loadConfigAndPolicyRule(resID);
		if (Assert.isEmptyString(rules)) {
			theLogger.debug("the standard rules are empty");
			return null;
		}
		// 对ssh采集回来的信息进行标准化
		ConfigAndPolicyStandard config = new ConfigAndPolicyStandard(
				excuteResult, rules, resID);
		String[] result = config.configAndPolicyStandard();
		if (result == null
				|| (Assert.isEmptyString(result[0]) && Assert
						.isEmptyString(result[1]))) {
			theLogger.error("the standard data of ssh are empty!");
			return null;
		}
		sshData.put(MetaDataConstant.EXECUTE_RESULT, result);
		return sshData;
	}

	/**
	 * 查询策略和配置信息的标准化规则
	 * 
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private String loadConfigAndPolicyRule(long resID) {
		IConfigAndPolicyDao configAndPolicyDao = new ConfigAndPolicyDao();
		return configAndPolicyDao.standardRulePathQuery(resID);
	}

	@Override
	public void setChainID(int chainID) {
		this.chainID = chainID;

	}

	@Override
	public int getChainID() {
		// TODO Auto-generated method stub
		return this.chainID;
	}

	/**
	 * 更新任务状态等相关信息
	 * 
	 * @param taskCode
	 * @param executeDate
	 */
	private void setTaskStatus(Map<String, Object> data, int executeResult) {
		Thread setTaskStatus = new TaskCompleted(data, executeResult);
		setTaskStatus.start();
	}

}
