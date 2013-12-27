package com.secpro.platform.monitoring.process.chains.telnet;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.db.storage.ContainAndConflictDBStorageAdapter;
import com.secpro.platform.monitoring.process.chains.ref.db.storage.DBStorage;
import com.secpro.platform.monitoring.process.chains.ref.db.storage.SSHTelnetDBStorageAdapter;
import com.secpro.platform.monitoring.process.chains.ref.event.EventAndAlarm;
import com.secpro.platform.monitoring.process.chains.ref.event.EventTypeNameConstant;
import com.secpro.platform.monitoring.process.chains.ref.match.PolicyContainAndConflict;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.ConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;

public class TelnetStorageAndMatch implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(TelnetStorageAndMatch.class);
	private int chainID = 0;

	// 包含事件名称
	// private final String CONTAIN_EVENT_NAME="contain";
	// 冲突事件名称
	// private final String CONFLICT_EVENT_NAME="conflict";
	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("telnet dataProcess chain ID: " + getChainID()
				+ " name:" + this.getClass().getName());
		if (rawData == null) {
			theLogger.error("invalid rawData in telnet data processing.");
			return null;
		}
		if (!rawData.getClass().equals(HashMap.class)) {
			theLogger.error("need type of HashMap in telnet data processing.");
			return null;
		}
		Map telnetData = (Map) rawData;
		long resID = (Long) telnetData.get(MetaDataConstant.RESOURCE_ID);
		if (resID == 0) {
			theLogger.error("resource id is empty!");
			return null;
		}
		String[] telnetStandardResult = (String[]) telnetData
				.get(MetaDataConstant.EXECUTE_RESULT);
		if (telnetStandardResult == null) {
			theLogger.error("the standard data of telnet are empty!");
			return null;
		}
		String taskCode = (String) telnetData.get(MetaDataConstant.SCHEDULE_ID);
		if (Assert.isEmptyString(taskCode)) {
			theLogger.error("invalid taskCode in SSH data processing.");
			return null;
		}
		if (MetaDataParsing.isCacheTask(taskCode)) {
			String executeDate = (String) telnetData
					.get(MetaDataConstant.EXECUTE_DATE);
			taskCode = taskCode + "_c" + executeDate;
		}
		telnetData.put(MetaDataConstant.SCHEDULE_ID, taskCode);
		// 调用telnet存储数据库方法，将数据存入数据库中
		telnetDBStorage(telnetData);
		//telnetData.put(MetaDataConstant.EXECUTE_RESULT, new String[]{telnetStandardResult[0],telnetStandardResult[1]});
		String[] containAndConflictResult = policyContainAndConflict(
				telnetStandardResult[1],
				loadContainAndConflictRule(resID));
		if (containAndConflictResult != null) {
			if (!Assert.isEmptyString(containAndConflictResult[0])) {
				EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID,
						EventTypeNameConstant.EVENT_TYEP_NAME_POLICY_CONTAIN,
						containAndConflictResult[0]);
			} else {
				EventAndAlarm.isRecoveryEvent(resID,
						EventTypeNameConstant.EVENT_TYEP_NAME_POLICY_CONTAIN);
			}
			if (!Assert.isEmptyString(containAndConflictResult[1])) {
				EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID,
						EventTypeNameConstant.EVENT_TYEP_NAME_POLICY_CONFLICT,
						containAndConflictResult[1]);
			} else {
				EventAndAlarm.isRecoveryEvent(resID,
						EventTypeNameConstant.EVENT_TYEP_NAME_POLICY_CONFLICT);
			}
			if (!Assert.isEmptyString(containAndConflictResult[0])
					|| !Assert.isEmptyString(containAndConflictResult[1])) {
				containAndConflictDBStorage(containAndConflictResult, resID,
						taskCode);
			}
		}
		return telnetData;
	}

	/**
	 * 查询包含与冲突检查规则
	 * 
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private String loadContainAndConflictRule(long resID) {
		IConfigAndPolicyDao configAndPolicyDao = new ConfigAndPolicyDao();
		return configAndPolicyDao.containAndConflictRuleQuery(resID);

	}

	/**
	 * 进行冲突和包含检查
	 * 
	 * @param policyValues
	 * @param containAndConflictRule
	 * @return
	 */
	private String[] policyContainAndConflict(String policyValues,
			String containAndConflictRule) {
		if (Assert.isEmptyString(policyValues)
				|| Assert.isEmptyString(containAndConflictRule)) {
			return null;
		}
		PolicyContainAndConflict containAndConflict = new PolicyContainAndConflict(
				policyValues, containAndConflictRule);
		return containAndConflict.policyContainAndConflict();

	}
	/**
	 * 存储标准化后的数据
	 * 
	 * @param rawData
	 */
	private void telnetDBStorage(Object rawData) {
		DBStorage dbStorage = new SSHTelnetDBStorageAdapter(rawData);
		dbStorage.start();
	}

	/**
	 * 存储冲突以及包含的检查结果
	 * 
	 * @param result
	 * @param resID
	 * @param taskCode
	 */
	private void containAndConflictDBStorage(String[] result, long resID,
			String taskCode) {
		Map<String, Object> value = new HashMap<String, Object>();
		value.put(MetaDataConstant.RESOURCE_ID, resID);
		value.put(MetaDataConstant.EXECUTE_RESULT, result);
		value.put(MetaDataConstant.SCHEDULE_ID, taskCode);
		DBStorage dbStorage = new ContainAndConflictDBStorageAdapter(value);
		dbStorage.start();
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

}

