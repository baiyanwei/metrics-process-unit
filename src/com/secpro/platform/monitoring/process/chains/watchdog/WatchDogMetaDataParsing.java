package com.secpro.platform.monitoring.process.chains.watchdog;

import java.util.Map;

import org.json.JSONObject;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
/**
 * 数据解析
 * 对watchdog类型的json数据格式进行解析，并转换成需要的数据格式
 * @author sxf
 *
 */
public class WatchDogMetaDataParsing implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(WatchDogMetaDataParsing.class);
	private int chainID = 0;

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("watchdog dataProcess chain ID: " + getChainID());
		if (rawData == null) {
			theLogger.error("invalid rawData in watchdog data processing.");
			return null;
		}
		if (rawData.getClass().equals(JSONObject.class) == false) {
			theLogger
					.error("need type of jsonObject in watchdog data processing.");
			return null;
		}
		JSONObject rawDataJson = (JSONObject) rawData;
		// 解析元数据
		Map<String, Object> result = MetaDataParsing
				.getWatchdogRelatedData(rawDataJson);
		return result;
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
