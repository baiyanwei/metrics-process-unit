package com.secpro.platform.monitoring.process.chains.snmp;

import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.ITaskDao;
import com.secpro.platform.monitoring.process.dao.impl.TaskDao;

/**
 * 原始数据解析 对snmp类型的json格式数据进行解析，并转换成需要的数据格式
 * 
 * @author sxf
 * 
 */
public class SNMPMetaDataParsing implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SNMPMetaDataParsing.class);
	private int chainID = 0;

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("snmp dataProcess chain ID: " + getChainID() + " name:"
				+ this.getClass().getName());
		if (rawData == null) {
			theLogger.error("invalid rawData in SNMP data processing.");
			return null;
		}
		if (rawData.getClass().equals(JSONObject.class) == false) {
			theLogger.error("need type of jsonObject in SNMP data processing.");
			return null;
		}
		JSONObject rawDataJson = (JSONObject) rawData;
		Map<String, Object> resultData = MetaDataParsing.getTaskRelatedData(
				rawDataJson, "snmp");
		if (resultData == null || resultData.size() == 0) {
			theLogger.error("analysis of the data is empty.");

			return null;
		}
		Object taskIDObj;
		if ((taskIDObj=resultData.get(MetaDataConstant.TASK_ID) )== null) {
			theLogger.error("invalid task id in SNMP data processing.");
			return null;
		}
		String taskID=(String)taskIDObj;
		long resID=resIDQuery(taskID);
		if(resID==0L){
			return null;
		}
		resultData.put(MetaDataConstant.RESOURCE_ID,resID);
		Object result = resultData.get(MetaDataConstant.EXECUTE_RESULT);
		if (result == null) {
			theLogger.error("the snmp execute result are empty!");
		} else {
			JSONTokener parser = new JSONTokener(result.toString());
			JSONObject resultJson = new JSONObject(parser);
			resultData.put(MetaDataConstant.EXECUTE_RESULT,
					MetaDataParsing.jsonObjectToMap(resultJson));
		}
		return resultData;
	}

	private long resIDQuery(String taskID) {
		ITaskDao taskDao=new TaskDao();
		return taskDao.resIDQuery(taskID);
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
