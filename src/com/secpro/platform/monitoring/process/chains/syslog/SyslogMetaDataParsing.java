package com.secpro.platform.monitoring.process.chains.syslog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;
import com.secpro.platform.monitoring.process.utils.CollectionRemoveUtil;
/**
 * 数据解析
 * 对syslog类型的json数据进行解析，并转换成需要的数据类型
 * @author sxf
 *
 */
public class SyslogMetaDataParsing implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SyslogMetaDataParsing.class);
	private int chainID = 0;

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("syslog dataProcess chain ID: " + getChainID()
				+ " name:" + this.getClass().getName());
		if (rawData == null) {
			theLogger.error("invalid rawData in syslog data processing.");
			return null;
		}
		if (rawData.getClass().equals(JSONObject.class) == false) {
			theLogger
					.error("need type of jsonObject in syslog data processing.");
			return null;
		}
		JSONObject rawDataJson = (JSONObject) rawData;
		List<Map<String, Object>> resultData = MetaDataParsing.getSyslogRelatedData(rawDataJson);
		if (resultData == null || resultData.size() == 0) {
			theLogger.error("analysis of the data is empty.");

			return null;
		}
		List<Map<String, Object>> deleteList = new ArrayList();
		for (Map<String, Object> syslogData : resultData) {
			String cityCode = (String) syslogData.get(MetaDataConstant.CITY_CODE);
			String targetIP = (String) syslogData.get(MetaDataConstant.TARGET_IP);
			if(Assert.isEmptyString(cityCode)||Assert.isEmptyString(targetIP)){
				deleteList.add(syslogData);
			}
			long resID=getResIDByTaskRegion(cityCode,targetIP);
			if(resID==0L){
				deleteList.add(syslogData);
			}
			syslogData.put(MetaDataConstant.RESOURCE_ID, resID);
		}
		if (deleteList.size() > 0) {
			CollectionRemoveUtil.listRemove(resultData, deleteList);
		}
		return resultData;
	}

	private long getResIDByTaskRegion(String taskRegion, String targetIP) {
		IResourceDao resDao=new ResDao();
		return resDao.resIDQueryByTaskRegion(taskRegion, targetIP);
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
