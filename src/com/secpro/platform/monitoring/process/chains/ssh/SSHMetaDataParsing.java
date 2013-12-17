package com.secpro.platform.monitoring.process.chains.ssh;

import java.util.Map;

import org.json.JSONObject;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;

/**
 * 数据解析 
 * 对ssh类型Json数据进行解析，并转换成需要的数据格式
 * 
 * @author sxf
 * 
 */
public class SSHMetaDataParsing implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SSHMetaDataParsing.class);
	private int chainID = 0;

	@Override
	public Object dataProcess(Object rawData) throws PlatformException {
		theLogger.debug("ssh dataProcess chain ID: " + getChainID() + " name:"
				+ this.getClass().getName());
		if (rawData == null) {
			theLogger.error("invalid rawData in SSH data processing.");
			return null;
		}
		if (rawData.getClass().equals(JSONObject.class) == false) {
			theLogger.error("need type of jsonObject in SSH data processing.");
			return null;
		}
		JSONObject rawDataJson = (JSONObject) rawData;
		Map<String, Object> resultData = MetaDataParsing.getTaskRelatedData(
				rawDataJson, "ssh");
		if (resultData == null || resultData.size() == 0) {
			theLogger.error("analysis of the data is empty.");
			return null;
		}
		// if(resultData.get(MetaDataConstant.TASK_CODE)==null)
		// {
		// throw new
		// PlatformException("invalid taskCode in SSH data processing.");
		// }
		return resultData;

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
