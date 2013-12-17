package com.secpro.platform.monitoring.process.chains.watchdog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.event.EventAndAlarm;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
/**
 * 产生与恢复事件，告警信息等
 * 根据事件规则，产生或恢复相应watchdog数据类型事件
 * @author sxf
 *
 */
public class WatchDogEvent implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(WatchDogEvent.class);
	private int chainID = 0;

	// private static final String DISK_SPECIAL="disk usage";
	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("watchdog dataProcess chain ID: " + getChainID());
		if (rawData == null) {
			theLogger.error("invalid rawData in watchdog data processing.");
			return null;
		}
		if (!rawData.getClass().equals(HashMap.class)) {
			theLogger
					.error("need type of HashMap in watchdog data processing.");
			return null;
		}
		Map<String, Object> watchdogData = (Map<String, Object>) rawData;
		Map<String, String> watchdogResult = (Map<String, String>) watchdogData
				.get(MetaDataConstant.WATCHDOG_EXECUTE_RESULT);
		if (watchdogResult == null || watchdogResult.size() == 0) {
			theLogger.error("watchdog result is empty");
			return null;
		}
		Set<String> resultKeys = watchdogResult.keySet();
		long resID = (Long) watchdogData.get("resID");
		for (String resultName : resultKeys) {
			String value = watchdogResult.get(resultName);
			// 判断是否会产生事件以及恢复事件等
			EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID, resultName,
					value);
		}
		return watchdogData;
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
