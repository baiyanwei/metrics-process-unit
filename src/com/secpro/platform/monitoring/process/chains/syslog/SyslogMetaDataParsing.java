package com.secpro.platform.monitoring.process.chains.syslog;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
/**
 * 数据解析
 * 对syslog类型的json数据进行解析，并转换成需要的数据类型
 * @author sxf
 *
 */
public class SyslogMetaDataParsing implements IDataProcessChain{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(SyslogMetaDataParsing.class);
	private int chainID=0;
	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("syslog dataProcess chain ID: "+getChainID());
		if(rawData==null)
		{
			theLogger.error("invalid rawData in syslog data processing.");
			return null;
		}
		if(rawData.getClass().equals(JSONObject.class)==false)
		{
			theLogger.error("need type of jsonObject in syslog data processing.");
			return null;
		}
		JSONObject rawDataJson=(JSONObject)rawData;
		List<Map<String,Object>> resultData=MetaDataParsing.getSyslogRelatedData(rawDataJson);
		return resultData;
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
