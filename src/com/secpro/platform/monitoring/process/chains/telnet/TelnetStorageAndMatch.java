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
import com.secpro.platform.monitoring.process.chains.ref.match.PolicyContainAndConflict;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.ConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;

public class TelnetStorageAndMatch implements IDataProcessChain{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(TelnetStorageAndMatch.class);
	private int chainID=0;
	private static final String CONTAIN_EVENT_NAME="contain";
	private static final String CONFLICT_EVENT_NAME="conflict";
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
		
		String cityCode = (String) telnetData
				.get(MetaDataConstant.CITY_CODE);
		String targetIP = (String) telnetData
				.get(MetaDataConstant.TARGET_IP);
		if (Assert.isEmptyString(cityCode)
				|| Assert.isEmptyString(targetIP)) {
			theLogger.error("city code or target IP is empty!");
			return null;
		}
		long resID=getResID(cityCode,targetIP);
		if(resID==0){
			theLogger.error("resource id is empty!");
			return null;
		}
		telnetData.put("resID", resID);
		String[] telnetStandardResult=(String[])telnetData.get(MetaDataConstant.EXECUTE_RESULT);
		if(telnetStandardResult==null){
			theLogger.error("the standard data of telnet are empty!");
			return null;
		}
		String taskCode=(String)telnetData.get(MetaDataConstant.TASK_CODE);
		if(Assert.isEmptyString(taskCode)){
			theLogger.error("invalid taskCode in SSH data processing.");
			return null;
		}
		if (MetaDataParsing.isCacheTask(taskCode)) {
			String executeDate=(String)telnetData.get(MetaDataConstant.EXECUTE_DATE);
			taskCode=taskCode+"_c"+executeDate;
		} 
		telnetData.put(MetaDataConstant.TASK_CODE,taskCode);
		//调用telnet存储数据库方法，将数据存入数据库中
		telnetDBStorage(telnetData);
		
		String[] containAndConflictResult=policyContainAndConflict(telnetStandardResult[1],loadContainAndConflictRule(cityCode,targetIP));
		if(containAndConflictResult!=null){
			if(!Assert.isEmptyString(containAndConflictResult[0])){
				EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID, CONTAIN_EVENT_NAME, containAndConflictResult[0]);
			}else{
				EventAndAlarm.isRecoveryEvent(resID, CONTAIN_EVENT_NAME);
			}
			if(!Assert.isEmptyString(containAndConflictResult[1])){
				EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID, CONFLICT_EVENT_NAME, containAndConflictResult[1]);
			}else{
				EventAndAlarm.isRecoveryEvent(resID, CONFLICT_EVENT_NAME);
			}
			if(!Assert.isEmptyString(containAndConflictResult[0])||!Assert.isEmptyString(containAndConflictResult[1])){
				containAndConflictDBStorage(containAndConflictResult,resID,taskCode);
			}
		}
		return telnetData;
	}
	/**
	 * 查询包含与冲突检查规则
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private String loadContainAndConflictRule(String cityCode, String targetIP) {
		IConfigAndPolicyDao configAndPolicyDao=new ConfigAndPolicyDao();
		return configAndPolicyDao.containAndConflictRuleQuery(cityCode, targetIP);
		
	}
	/**
	 * 进行冲突和包含检查
	 * @param policyValues
	 * @param containAndConflictRule
	 * @return
	 */
	private String[] policyContainAndConflict(String policyValues,String containAndConflictRule) {
		if(Assert.isEmptyString(policyValues)||Assert.isEmptyString(containAndConflictRule)){
			return null;
		}
		PolicyContainAndConflict containAndConflict=new PolicyContainAndConflict(policyValues,containAndConflictRule);
		return containAndConflict.policyContainAndConflict();
		
	}
	/**
	 * 查询资源ID
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private long getResID(String cityCode, String targetIP) {
		IResourceDao resDao=new ResDao();
		return resDao.ResIDQuery(cityCode, targetIP);
	}
	/**
	 * 存储标准化后的数据
	 * @param rawData
	 */
	private void telnetDBStorage(Object rawData) {
		DBStorage dbStorage=new SSHTelnetDBStorageAdapter(rawData);
		dbStorage.start();
	}
	/**
	 * 存储冲突以及包含的检查结果
	 * @param result
	 * @param resID
	 * @param taskCode
	 */
	private void containAndConflictDBStorage(String[] result,long resID,String taskCode){
		Map<String,Object> value=new HashMap<String,Object>();
		value.put("resID", resID);
		value.put(MetaDataConstant.EXECUTE_RESULT, result);
		value.put(MetaDataConstant.TASK_CODE, taskCode);
		DBStorage dbStorage=new ContainAndConflictDBStorageAdapter(value);
		dbStorage.start();
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

}

